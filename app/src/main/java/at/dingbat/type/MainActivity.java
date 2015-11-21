package at.dingbat.type;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.util.ApiUtil;
import at.dingbat.type.widget.FileListItem;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleClient;
    private DriveContents contents;

    private DrawerLayout drawer;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);

        recycler = (RecyclerView) findViewById(R.id.activity_main_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new Adapter(this);

        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);

        toggle.syncState();

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();

        googleClient.connect();
    }

    private void createFile(String title, DriveFolder folder, ResultCallback<DriveFolder.DriveFileResult> callback) {
        MetadataChangeSet file = new MetadataChangeSet.Builder()
                .setTitle("Test")
                .setMimeType("text/typo")
                .build();
        if(callback != null)
            folder.createFile(googleClient, file, contents).setResultCallback(callback);
        else
            folder.createFile(googleClient, file, contents);
    }

    private DriveFolder getFolder(DriveId id) {
        return Drive.DriveApi.getFolder(googleClient, id);
    }

    private DriveFile getFile(DriveId id) {
        return Drive.DriveApi.getFile(googleClient, id);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Drive.DriveApi.newDriveContents(googleClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                contents = result.getDriveContents();
            }
        });

        Drive.DriveApi.getRootFolder(googleClient).listChildren(googleClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                MetadataBuffer buffer = result.getMetadataBuffer();
                for (int i = 0; i < buffer.getCount(); i++) {
                    if (buffer.get(i).isFolder())
                        Log.d("test", "Folder: " + buffer.get(i).getTitle());
                    else {
                        adapter.add(FileListItem.DataHolder.create(buffer.get(i)));
                        Log.d("test", "File: " + buffer.get(i).getTitle() + " " + buffer.get(i).getMimeType());
                    }
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 9002);
            } catch (Exception e) {
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 9002:
                if(resultCode == RESULT_OK) {
                    googleClient.connect();
                }
                break;
        }
    }
}
