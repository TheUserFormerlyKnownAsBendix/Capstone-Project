package at.dingbat.type.activity;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.model.Document;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.TextBlockItem;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleApiClient googleClient;
    private Document doc;
    private DriveFile file;

    private String path;

    private CollapsingToolbarLayout ctl;
    private TextView word_count;
    private TextView character_count;
    private TextView location;
    private TextView created;
    private TextView changed;
    private MapFragment map;
    private SwitchCompat save_location;
    private FrameLayout fragment;
    private RelativeLayout delete;

    private GoogleMap googleMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_detail_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ctl = (CollapsingToolbarLayout) findViewById(R.id.activity_detail_collapsing_toolbar_layout);
        word_count = (TextView) findViewById(R.id.activity_detail_word_count);
        character_count = (TextView) findViewById(R.id.activity_detail_character_count);
        location = (TextView) findViewById(R.id.activity_detail_location);
        created = (TextView) findViewById(R.id.activity_detail_created);
        changed = (TextView) findViewById(R.id.activity_detail_changed);
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_detail_map);
        save_location = (SwitchCompat) findViewById(R.id.activity_detail_save_location);
        delete = (RelativeLayout) findViewById(R.id.activity_detail_delete);
        fragment = (FrameLayout) findViewById(R.id.activity_detail_map);

        path = "";

        save_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Location l = LocationServices.FusedLocationApi.getLastLocation(googleClient);
                    ApiUtil.addLocation(googleClient, file, l);

                    LatLng latlng = new LatLng(l.getLatitude(), l.getLongitude());
                    setLocation(latlng);
                } else {
                    ApiUtil.removeLocation(googleClient, file);
                    fragment.setVisibility(View.GONE);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("at.dingbat.type");
                i.putExtra("action", "deletefile");
                i.putExtra("file", file.getDriveId().toString());
                LocalBroadcastManager.getInstance(DetailActivity.this).sendBroadcast(i);

                Intent main = new Intent(DetailActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(main);
            }
        });

        map = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_detail_map, map);
        fragmentTransaction.commit();

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        map.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        googleClient.connect();
    }
    
    @Override
    public void onConnected(Bundle bundle) {
        DriveId id = DriveId.decodeFromString(getIntent().getStringExtra("file"));
        file = Drive.DriveApi.getFile(googleClient, id);

        file.getMetadata(googleClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
             public void onResult(DriveResource.MetadataResult metadataResult) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd");
                created.setText(sdf.format(metadataResult.getMetadata().getCreatedDate()));
                changed.setText(sdf.format(metadataResult.getMetadata().getModifiedDate()));
            }
        });

        ResultCallback<DriveApi.MetadataBufferResult> callback = new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if(metadataBufferResult.getMetadataBuffer().getCount() > 0) {
                    path = " > "+metadataBufferResult.getMetadataBuffer().get(0).getTitle()+path;
                    DriveFolder folder = Drive.DriveApi.getFolder(googleClient, metadataBufferResult.getMetadataBuffer().get(0).getDriveId());
                    folder.listParents(googleClient).setResultCallback(this);
                } else {
                    path = getString(R.string.my_drive)+path;
                    location.setText(path);
                }
            }
        };

        file.listParents(googleClient).setResultCallback(callback);

        /*file.listParents(googleClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer buffer = metadataBufferResult.getMetadataBuffer();

                DriveFile f = Drive.DriveApi.getFolder(googleClient, buffer.get(1).getDriveId());

                f.listParents()
                
                String path = getString(R.string.my_drive);

                for(int i = 0; i < buffer.getCount(); i++) {
                    path += " > "+buffer.get(i).getTitle();
                }

                location.setText(path);
            }
        });*/

        doc = new Document(googleClient);

        doc.setOnDocumentLoadedListener(new Document.DocumentLoadedCallback() {
            @Override
            public void onDocumentLoaded() {
                ctl.setTitle(doc.getTitle());

                int words = 0;
                int characters = 0;
                for (Adapter.DataHolder holder : doc.getHolders()) {
                    if (holder instanceof TextBlockItem.DataHolder) {
                        String t = ((TextBlockItem.DataHolder) holder).block.content;
                        characters += t.length();
                        words += t.split(" ").length;
                    }
                }

                word_count.setText(words + "");
                character_count.setText(characters + "");

                setLocation(doc.getLocation());
            }
        });

        doc.load(this, file);
    }

    private void setLocation(LatLng l) {
        if(googleMap != null) {
            if (l != null) {
                fragment.setVisibility(View.VISIBLE);
                marker = googleMap.addMarker(new MarkerOptions().position(l));
                CameraPosition position = new CameraPosition.Builder().target(l).zoom(14.0f).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                googleMap.animateCamera(update);
                googleMap.getUiSettings().setAllGesturesEnabled(false);
            } else {
                save_location.setChecked(false);
                fragment.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if(doc != null) setLocation(doc.getLocation());
    }
}
