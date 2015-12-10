package at.dingbat.type.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.sax.TextElementListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.adapter.Section;
import at.dingbat.type.util.ApiUtil;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.FileListItem;
import at.dingbat.type.widget.FolderListItem;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleClient;
    private DriveContents contents;

    private Menu menu;

    private DrawerLayout drawer;
    private ImageView profile_cover;
    private ImageView profile_photo;
    private TextView profile_display_name;
    private TextView profile_email;

    private Toolbar search_toolbar;
    private EditText search;
    private ImageButton search_button;
    private boolean isSearchEnabled = false;

    private FloatingActionButton fab_add;
    private FloatingActionButton fab_add_file;
    private FloatingActionButton fab_add_folder;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private Adapter adapter;

    private Section folders;
    private Section files;
    private Section results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);
        profile_cover = (ImageView) findViewById(R.id.drawer_profile_cover);
        profile_photo = (ImageView) findViewById(R.id.drawer_profile_photo);
        profile_display_name = (TextView) findViewById(R.id.drawer_profile_display_name);
        profile_email = (TextView) findViewById(R.id.drawer_profile_email);
        fab_add = (FloatingActionButton) findViewById(R.id.activity_main_add);
        fab_add_file = (FloatingActionButton) findViewById(R.id.activity_main_add_file);
        fab_add_folder = (FloatingActionButton) findViewById(R.id.activity_main_add_folder);
        search_toolbar = (Toolbar) findViewById(R.id.activity_main_search_toolbar);
        search = (EditText) findViewById(R.id.activity_main_search);
        search_button = (ImageButton) findViewById(R.id.activity_main_search_button);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
            }
        });

        fab_add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*ApiUtil.createFile(googleClient, "Untitled", Drive.DriveApi.getRootFolder(googleClient), new ApiUtil.FileCreatedCallback() {
                    @Override
                    public void onFileCreated(DriveFile file) {
                        if(file != null) reload();
                    }
                });*/
                revertFABAnimation();
            }
        });

        fab_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*ApiUtil.createFolder(googleClient, "Untitled", Drive.DriveApi.getRootFolder(googleClient), new ApiUtil.FolderCreatedCallback() {
                    @Override
                    public void onFolderCreated(DriveFolder folder) {
                        if(folder != null) reload();
                    }
                });*/
                DialogUtil.createFolderTitleDialog(MainActivity.this).show();
                revertFABAnimation();
            }
        });

        recycler = (RecyclerView) findViewById(R.id.activity_main_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new Adapter(this);

        folders = new Section("Folders");
        files = new Section("Files");
        results = new Section("Search results");

        adapter.addSection(folders);
        adapter.addSection(files);

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

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addApi(Plus.API)
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

    private void toggleSearch() {
        isSearchEnabled = !isSearchEnabled;
        final TypedArray styledAttr = getTheme().obtainStyledAttributes(new int [] { android.R.attr.actionBarSize });
        ValueAnimator animator = null;
        if(isSearchEnabled) {
            animator = ValueAnimator.ofFloat(0, 1);
            fab_add.animate().setDuration(150).setInterpolator(new AccelerateInterpolator()).alpha(0);
            adapter.clear();
            adapter.addSection(results);
        } else {
            animator = ValueAnimator.ofFloat(1, 0);
            fab_add.animate().setDuration(150).setInterpolator(new AccelerateInterpolator()).alpha(1);
            reload();
        }

        menu.findItem(R.id.action_search).setVisible(!isSearchEnabled);

        animator.setDuration(150);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = search_toolbar.getLayoutParams();
                params.height = (int) ((float) animation.getAnimatedValue() * (int) styledAttr.getDimension(0, 0));
                search_toolbar.setLayoutParams(params);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                styledAttr.recycle();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleClient.connect();
    }

    private DriveFolder getFolder(DriveId id) {
        return Drive.DriveApi.getFolder(googleClient, id);
    }

    private DriveFile getFile(DriveId id) {
        return Drive.DriveApi.getFile(googleClient, id);
    }

    private void reload() {
        ApiUtil.getFolderContents(googleClient, Drive.DriveApi.getRootFolder(googleClient), new ApiUtil.FolderContentsLoadedCallback() {
            @Override
            public void onFolderContentsLoaded(DriveApi.MetadataBufferResult result) {
                MetadataBuffer buffer = result.getMetadataBuffer();

                adapter.clear();

                files.clear();
                folders.clear();

                for (int i = 0; i < buffer.getCount(); i++) {
                    if (!buffer.get(i).isFolder())
                        files.add(FileListItem.DataHolder.create(buffer.get(i)));
                    else folders.add(FolderListItem.DataHolder.create(buffer.get(i)));
                }

                adapter.addSection(folders);
                adapter.addSection(files);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        reload();

        if(Plus.PeopleApi.getCurrentPerson(googleClient) != null) {
            Person current = Plus.PeopleApi.getCurrentPerson(googleClient);
            Picasso.with(this).load(current.getImage().getUrl()).into(profile_photo);
            Picasso.with(this).load(current.getCover().getCoverPhoto().getUrl()).into(profile_cover);

            profile_display_name.setText(current.getDisplayName());
            profile_email.setText(Plus.AccountApi.getAccountName(googleClient));
        } else Log.e("test", "Account is null");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                toggleSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isSearchEnabled) toggleSearch();
        else super.onBackPressed();
    }
}
