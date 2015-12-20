package at.dingbat.type.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.plus.Plus;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.adapter.Section;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.FileListItem;
import at.dingbat.type.widget.FolderListItem;

public class BrowserActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleClient;
    private LocalBroadcastManager lbcm;

    private DriveFolder folder;

    private Menu menu;

    private FloatingActionButton fab_add;
    private FloatingActionButton fab_add_file;
    private FloatingActionButton fab_add_folder;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private Adapter adapter;

    private Section folders;
    private Section files;

    private boolean save_location = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_browser_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        recycler = (RecyclerView) findViewById(R.id.activity_browser_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new Adapter(this);

        fab_add = (FloatingActionButton) findViewById(R.id.activity_browser_add);
        fab_add_file = (FloatingActionButton) findViewById(R.id.activity_browser_add_file);
        fab_add_folder = (FloatingActionButton) findViewById(R.id.activity_browser_add_folder);

        folders = new Section(getString(R.string.folders));
        files = new Section(getString(R.string.files));

        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
            }
        });

        fab_add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.createFileDialog(BrowserActivity.this, folder).show();
                revertFABAnimation();
            }
        });

        fab_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.createFolderDialog(BrowserActivity.this, folder).show();
                revertFABAnimation();
            }
        });

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addApi(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    private void animateFab() {
        if(fab_add.getRotation() != 0) {
            revertFABAnimation();
        } else {
            startFABAnimation();
        }
    }

    private void startFABAnimation() {
        fab_add.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).rotation(45);
        float offsetX = fab_add.getX() + (fab_add.getWidth()-fab_add_file.getWidth())/2;
        float offsetY = fab_add.getY() + (fab_add.getHeight()-fab_add_file.getHeight())/2;
        fab_add_file.setX(offsetX);
        fab_add_folder.setX(offsetX);
        fab_add_file.setY(offsetY);
        fab_add_folder.setY(offsetY);
        fab_add_file.animate().setDuration(300).setInterpolator(new DecelerateInterpolator()).alpha(1f).translationY(-500);
        fab_add_folder.animate().setDuration(300).setInterpolator(new DecelerateInterpolator()).alpha(1f).translationY(-300);
    }

    private void revertFABAnimation() {
        fab_add.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).rotation(0);
        float offsetY = fab_add.getY() + (fab_add.getHeight()-fab_add_file.getHeight())/2;
        fab_add_file.animate().setDuration(300).setInterpolator(new AccelerateInterpolator()).alpha(0f).y(offsetY);
        fab_add_folder.animate().setDuration(300).setInterpolator(new AccelerateInterpolator()).alpha(0f).y(offsetY);
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
        DriveId id = DriveId.decodeFromString(getIntent().getStringExtra("folder"));
        folder = Drive.DriveApi.getFolder(googleClient, id);

        ApiUtil.getMetadata(googleClient, folder, new ApiUtil.MetadataLoadedCallback() {
            @Override
            public void onMetadataLoaded(DriveResource.MetadataResult result) {
                getSupportActionBar().setTitle(result.getMetadata().getTitle());
            }
        });
        ApiUtil.getFolderContents(googleClient, folder, new ApiUtil.FolderContentsLoadedCallback() {
            @Override
            public void onFolderContentsLoaded(DriveApi.MetadataBufferResult result) {
                MetadataBuffer buffer = result.getMetadataBuffer();

                adapter.clear();

                files.clear();
                folders.clear();

                for (int i = 0; i < buffer.getCount(); i++) {
                    if(!buffer.get(i).isTrashed()) {
                        if (!buffer.get(i).isFolder())
                            files.add(FileListItem.DataHolder.create(buffer.get(i)));
                        else {
                            final Metadata meta = buffer.get(i);
                            Drive.DriveApi.getFolder(googleClient, buffer.get(i).getDriveId()).listChildren(googleClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                @Override
                                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                                    folders.add(FolderListItem.DataHolder.create(meta, metadataBufferResult.getMetadataBuffer().getCount()));
                                }
                            });
                        }
                    }
                }

                adapter.addSection(folders);
                adapter.addSection(files);
                adapter.notifySectionChanged();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
