package at.dingbat.type.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditable(!editable);
            }
        });

        recycler = (RecyclerView) findViewById(R.id.activity_editor_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new EditorAdapter();


        TextStyle heading = new TextStyle();
        heading.size = 24;
        heading.color = "#000000";
        heading.indentation = 0;

        TextStyle subheading = new TextStyle();
        subheading.size = 18;
        subheading.color = "#696969";
        subheading.indentation = 1;

        TextStyle paragraph = new TextStyle();
        paragraph.size = 12;
        paragraph.color = "#000000";
        paragraph.indentation = 2;

        adapter.add(TextBlockItem.DataHolder.create("", "Title", heading));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle 2", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle 3", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));


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

        Log.d("test", "DriveId: "+getIntent().getStringExtra("file"));

    }

    private void setEditable(boolean editable) {
        Intent i = new Intent("at.dingbat.type");
        if(editable) i.putExtra("action", "entereditmode");
        else i.putExtra("action", "exiteditmode");
        lbcm.sendBroadcast(i);

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
    public void onConnected(Bundle bundle) {
        Log.d("test", "DriveId: "+getIntent().getStringExtra("file"));

        DriveId id = DriveId.decodeFromString(getIntent().getStringExtra("file"));
        DriveFile file = Drive.DriveApi.getFile(googleClient, id);

        doc = new Document(googleClient);

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
