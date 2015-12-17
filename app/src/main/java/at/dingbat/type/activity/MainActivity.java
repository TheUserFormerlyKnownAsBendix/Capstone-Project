package at.dingbat.type.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Map;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.adapter.Section;
import at.dingbat.type.provider.PreferencesProvider;
import at.dingbat.type.util.DialogUtil;
import at.dingbat.type.widget.FileListItem;
import at.dingbat.type.widget.FolderListItem;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;

    private GoogleApiClient googleClient;
    private LocalBroadcastManager lbcm;

    private Menu menu;

    private DrawerLayout drawer;
    private ImageView profile_cover;
    private ImageView profile_photo;
    private TextView profile_display_name;
    private TextView profile_email;
    private LinearLayout drawer_settings;
    private LinearLayout drawer_info;

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

    private Section location;
    private Section recent;
    private Section folders;
    private Section files;
    private Section results;

    private boolean save_location = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        lbcm = LocalBroadcastManager.getInstance(this);
        lbcm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("action")) {
                    String action = intent.getStringExtra("action");
                    if (action.equals("createfile")) {
                        Location l = null;
                        if(save_location) l = LocationServices.FusedLocationApi.getLastLocation(googleClient);
                        ApiUtil.createFile(googleClient, intent.getStringExtra("title"), Drive.DriveApi.getFolder(googleClient, DriveId.decodeFromString(intent.getStringExtra("folder"))), l, new ApiUtil.FileCreatedCallback() {
                            @Override
                            public void onFileCreated(DriveFile file) {
                                if (file != null) {
                                    Intent i = new Intent(MainActivity.this, EditorActivity.class);
                                    i.putExtra("file", file.getDriveId().toString());
                                    startActivity(i);
                                }
                            }
                        });
                    } else if (action.equals("createfolder")) {
                        ApiUtil.createFolder(googleClient, intent.getStringExtra("title"), Drive.DriveApi.getFolder(googleClient, DriveId.decodeFromString(intent.getStringExtra("folder"))), new ApiUtil.FolderCreatedCallback() {
                            @Override
                            public void onFolderCreated(DriveFolder folder) {
                                if (folder != null) reload();
                            }
                        });
                    }
                }
            }
        }, new IntentFilter("at.dingbat.type"));

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
        drawer_settings = (LinearLayout) findViewById(R.id.drawer_button_settings);
        drawer_info = (LinearLayout) findViewById(R.id.drawer_button_info);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
            }
        });

        fab_add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.createFileDialog(MainActivity.this, Drive.DriveApi.getRootFolder(googleClient)).show();
                revertFABAnimation();
            }
        });

        fab_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.createFolderDialog(MainActivity.this, Drive.DriveApi.getRootFolder(googleClient)).show();
                revertFABAnimation();
            }
        });

        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        recycler = (RecyclerView) findViewById(R.id.activity_main_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new Adapter(this);

        location = new Section("Based on your location");
        location.showSeparator(true);
        recent = new Section("Recent");
        recent.showSeparator(true);
        folders = new Section("Folders");
        files = new Section("Files");
        results = new Section("Search results");

        adapter.addSection(location);
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
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    ApiUtil.search(googleClient, search.getText().toString(), new ApiUtil.SearchCallback() {
                        @Override
                        public void onResult(DriveApi.MetadataBufferResult result) {
                            MetadataBuffer buffer = result.getMetadataBuffer();
                            for (int i = 0; i < buffer.getCount(); i++) {
                                if (!buffer.get(i).isTrashed())
                                    results.add(FileListItem.DataHolder.create(buffer.get(i)));
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addApi(Plus.API)
                .addApi(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getLoaderManager().initLoader(URL_LOADER, null, this);

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

        if(googleClient.isConnected()) reload();
        else googleClient.connect();
    }

    private void reload() {
        ApiUtil.getFolderContents(googleClient, Drive.DriveApi.getRootFolder(googleClient), new ApiUtil.FolderContentsLoadedCallback() {
            @Override
            public void onFolderContentsLoaded(DriveApi.MetadataBufferResult result) {
                MetadataBuffer buffer = result.getMetadataBuffer();

                adapter.clear();

                files.clear();
                folders.clear();
                location.clear();
                recent.clear();

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

                adapter.addSection(location);
                adapter.addSection(recent);
                adapter.addSection(folders);
                adapter.addSection(files);
                adapter.notifySectionChanged();
            }
        });
        ApiUtil.getAllTypos(googleClient, new ApiUtil.SearchCallback() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                location.clear();

                Location current = LocationServices.FusedLocationApi.getLastLocation(googleClient);

                CustomPropertyKey lat = new CustomPropertyKey("lat", CustomPropertyKey.PUBLIC);
                CustomPropertyKey lon = new CustomPropertyKey("lon", CustomPropertyKey.PUBLIC);

                MetadataBuffer buffer = result.getMetadataBuffer();
                for(int i = 0; i < buffer.getCount(); i++) {
                    Metadata data = buffer.get(i);
                    Date now = new Date();
                    if(now.getTime() < (data.getModifiedDate().getTime() + 1000*60*60)) {
                        recent.add(FileListItem.DataHolder.create(data));
                    }
                    Map<CustomPropertyKey, String> properties = data.getCustomProperties();
                    if(properties.containsKey(lat) && properties.containsKey(lon)) {
                        Location to = new Location("");
                        to.setLatitude(Double.parseDouble(properties.get(lat)));
                        to.setLongitude(Double.parseDouble(properties.get(lon)));
                        float distance = current.distanceTo(to);
                        if(distance < 100) {
                            location.add(FileListItem.DataHolder.create(data));
                        }
                    }
                }
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
            case R.id.action_refresh:
                reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isSearchEnabled) toggleSearch();
        else super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case URL_LOADER:
                return new CursorLoader(this,
                        PreferencesProvider.CONTENT_URI,
                        new String[] { "NAME", "VALUE" },
                        null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor != null) {
            int name_i = cursor.getColumnIndex(PreferencesProvider.NAME);
            int value_i = cursor.getColumnIndex(PreferencesProvider.VALUE);
            while (cursor.moveToNext()) {
                String name = cursor.getString(name_i);
                String value = cursor.getString(value_i);

                if (name.equals(SettingsActivity.PREFERENCE_SAVE_LOCATION)) {
                    save_location = value.equals("1");
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
