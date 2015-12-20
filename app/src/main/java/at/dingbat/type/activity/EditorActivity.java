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
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;
import at.dingbat.type.adapter.EditorAdapter;
import at.dingbat.type.model.Document;
import at.dingbat.type.model.TextBlock;
import at.dingbat.type.model.TextStyle;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.TextBlockItem;

public class EditorActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private FloatingActionButton fab_add;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private EditorAdapter adapter;

    private AppBarLayout abl;
    private CollapsingToolbarLayout ctl;
    private int offset = 0;

    private boolean editable = false;

    private LocalBroadcastManager lbcm;

    private GoogleApiClient googleClient;
    private DriveFile file;
    private Document doc;
    private String title;

    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_editor_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        fab_add = (FloatingActionButton) findViewById(R.id.activity_editor_add);

        abl = (AppBarLayout) findViewById(R.id.activity_editor_appbar_layout);
        ctl = (CollapsingToolbarLayout) findViewById(R.id.activity_editor_collapsing_toolbar_layout);

        recycler = (RecyclerView) findViewById(R.id.activity_editor_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new EditorAdapter();

        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

        abl.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                offset = verticalOffset;
            }
        });

        lbcm = LocalBroadcastManager.getInstance(this);

        lbcm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                if (action.equals("create")) {
                    TextBlock block = new TextBlock();
                    block.UUID = UUID.randomUUID().toString();
                    if (intent.hasExtra("type")) block.type = intent.getStringExtra("type");
                    else block.type = "default";
                    block.style = doc.getTextStyle(block.type);
                    block.content = block.style.title;
                    doc.addTextBlock(block);
                    adapter.add(TextBlockItem.DataHolder.create(block));
                } else if (action.equals("patch")) {
                    String uuid = intent.getStringExtra("uuid");
                    String content = intent.getStringExtra("content");
                    doc.patchTextBlock(uuid, content);
                } else if (action.equals("patchstyle")) {
                    String uuid = intent.getStringExtra("uuid");
                    String style = intent.getStringExtra("style");
                    doc.patchTextStyle(uuid, style);
                } else if (action.equals("remove")) {
                    String uuid = intent.getStringExtra("uuid");
                    doc.removeTextBlock(uuid);
                    adapter.remove(uuid);
                }
            }
        }, new IntentFilter("at.dingbat.type"));

        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
            Drawable d = null;
            if(editable) d = getResources().getDrawable(R.drawable.ic_add_white_24dp);
            else d = getResources().getDrawable(R.drawable.ic_mode_edit_white_24dp);

            fab_add.setImageDrawable(d);

            final int first = savedInstanceState.getInt("first");

            abl.setExpanded(savedInstanceState.getBoolean("expanded"));

            recycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {
                    recycler.scrollToPosition(first);
                    recycler.removeOnChildAttachStateChangeListener(this);
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {

                }
            });
        }

        recycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if(editable) {
                    Intent i = new Intent("at.dingbat.type");
                    i.putExtra("action", "entereditmodeimminent");
                    lbcm.sendBroadcast(i);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doc.save(new ApiUtil.DocumentSavedCallback() {
                    @Override
                    public void onSaved() {

                    }
                });
            }
        }, 0, 1, TimeUnit.MINUTES);

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
    protected void onStop() {
        doc.save(new ApiUtil.DocumentSavedCallback() {
            @Override
            public void onSaved() {
                Toast.makeText(EditorActivity.this, R.string.document_saved, Toast.LENGTH_SHORT).show();
            }
        });

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(editable) setEditable(false);
        else super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(!loaded) {
            DriveId id = DriveId.decodeFromString(getIntent().getStringExtra("file"));
            file = Drive.DriveApi.getFile(googleClient, id);

            doc = new Document(googleClient);

            doc.setOnDocumentLoadedListener(new Document.DocumentLoadedCallback() {
                @Override
                public void onDocumentLoaded() {
                    loaded = true;

                    adapter.add(doc.getHolders());

                    title = doc.getTitle();

                    getSupportActionBar().setTitle(title);

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

            doc.load(this, file);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename:
                DialogUtil.createRenameFileDialog(this, file, title, new DialogUtil.RenamedCallback() {
                    @Override
                    public void renamed(String title) {
                        EditorActivity.this.title = title;
                        ctl.setTitle(title);
                    }
                }).show();
                return true;
            case R.id.action_details:
                Intent i = new Intent(this, DetailActivity.class);
                i.putExtra("file", file.getDriveId().toString());
                startActivity(i);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("editable", editable);
        outState.putInt("first", layout_manager.findFirstCompletelyVisibleItemPosition());
        outState.putBoolean("expanded", offset == 0);
    }

}
