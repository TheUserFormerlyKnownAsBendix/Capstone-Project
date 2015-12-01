package at.dingbat.type.activity;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import at.dingbat.type.R;
import at.dingbat.type.adapter.EditorAdapter;
import at.dingbat.type.model.Document;
import at.dingbat.type.model.TextStyle;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.TextBlockItem;

public class EditorActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private FloatingActionButton fab_add;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private EditorAdapter adapter;

    private boolean editable = false;

    private LocalBroadcastManager lbcm;

    private GoogleApiClient googleClient;
    private Document doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_editor_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Untitled");

        fab_add = (FloatingActionButton) findViewById(R.id.activity_editor_add);

        recycler = (RecyclerView) findViewById(R.id.activity_editor_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new EditorAdapter();

        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

        lbcm = LocalBroadcastManager.getInstance(this);

        lbcm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                if(action.equals("patch")) {
                    String uuid = intent.getStringExtra("uuid");
                    String[] diff = intent.getStringArrayExtra("diff");

                }
            }
        }, new IntentFilter("at.dingbat.type"));

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    private void setEditable(boolean editable) {
        Intent i = new Intent("at.dingbat.type");
        if(editable) i.putExtra("action", "entereditmode");
        else i.putExtra("action", "exiteditmode");
        lbcm.sendBroadcast(i);

        Drawable d = null;
        if(editable) d = getResources().getDrawable(R.drawable.ic_add_white_24dp);
        else d = getResources().getDrawable(R.drawable.ic_mode_edit_white_24dp);

        fab_add.setImageDrawable(d);

        this.editable = editable;
        adapter.editable(editable);
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleClient.connect();
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
    public void onBackPressed() {
        if(editable) setEditable(false);
        else super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle bundle) {
        DriveId id = DriveId.decodeFromString(getIntent().getStringExtra("file"));
        DriveFile file = Drive.DriveApi.getFile(googleClient, id);

        doc = new Document(googleClient);

        doc.setOnDocumentLoadedListener(new Document.DocumentLoadedCallback() {
            @Override
            public void onDocumentLoaded() {
                adapter.add(doc.getHolders());

                fab_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editable) setEditable(true);
                        else {
                            DialogUtil.createAddTextBlockItemDialog(EditorActivity.this, doc.getMaster()).show();
                        }
                    }
                });
            }
        });

        doc.load(file);
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
