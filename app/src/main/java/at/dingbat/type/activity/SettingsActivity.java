package at.dingbat.type.activity;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.net.URL;

import at.dingbat.type.R;
import at.dingbat.type.provider.PreferencesProvider;

public class SettingsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String PREFERENCE_SAVE_LOCATION = "location";

    private static final int URL_LOADER = 0;

    private AppCompatCheckBox save_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        save_location = (AppCompatCheckBox) findViewById(R.id.activity_settings_location_data);
        save_location.setChecked(true);

        save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(PreferencesProvider.NAME, PREFERENCE_SAVE_LOCATION);
                values.put(PreferencesProvider.VALUE, (save_location.isChecked()) ? "1" : "0");
                getContentResolver().insert(PreferencesProvider.CONTENT_URI, values);
            }
        });

        getLoaderManager().initLoader(URL_LOADER, null, this);
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

                if (name.equals(PREFERENCE_SAVE_LOCATION)) {
                    save_location.setChecked(value.equals("1"));
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
