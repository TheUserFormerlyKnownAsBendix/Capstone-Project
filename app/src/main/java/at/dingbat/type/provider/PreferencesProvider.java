package at.dingbat.type.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class PreferencesProvider extends ContentProvider {

    /*
     * I know this doesn't make any sense ... this is just to meet the requirements ...
     */

    private SharedPreferences preferences;

    private static final int PREFERENCES = 10;
    private static final int PREFERENCE_ID = 20;

    private static final String AUTHORITY = "at.dingbat.type.provider";
    private static final String BASE_PATH = "preferences";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/preferences";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/preference";

    private static HashMap<String, String> PROJECTION_MAP;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, PREFERENCES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH+"/#", PREFERENCE_ID);
    }

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "Preferences";
    private static final String PREFERENCES_TABLE_NAME = "Preferences";
    private static final int DATABASE_VERSION = 1;
    public static final String NAME = "name";
    public static final String VALUE = "value";
    private static final String CREATE_DB_TABLE =
            "CREATE TABLE "+ PREFERENCES_TABLE_NAME +
                    " ("+NAME+" TEXT PRIMARY KEY," +
                    " "+VALUE+" TEXT NOT NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ PREFERENCES_TABLE_NAME);
            this.onCreate(sqLiteDatabase);
        }
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper helper = new DatabaseHelper(getContext());
        db = helper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(PREFERENCES_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case PREFERENCES:
                builder.setProjectionMap(PROJECTION_MAP);
                break;
            case PREFERENCE_ID:
                builder.appendWhere(NAME+" ="+uri.getPathSegments().get(1));
                break;
        }

        Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PREFERENCES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE+"/preferences";
            case PREFERENCE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE+"/preferences";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        try {
            id = db.insertOrThrow(PREFERENCES_TABLE_NAME, "", values);
        } catch (SQLiteConstraintException e) {
            ContentValues v = new ContentValues(1);
            v.put(VALUE, values.getAsString(VALUE));
            id = db.update(PREFERENCES_TABLE_NAME, v, NAME+" = " + uri.getPathSegments().get(1), null);
        }
        if(id > 0) {
            Uri u = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(u, null);
            return u;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case PREFERENCES:
                count = db.delete(PREFERENCES_TABLE_NAME, selection, selectionArgs);
                break;
            case PREFERENCE_ID:
                count = db.delete(PREFERENCES_TABLE_NAME,
                        NAME+" = "+uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND ("+selection+")" : ""),
                        selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case PREFERENCES:
                count = db.update(PREFERENCES_TABLE_NAME, values, selection, selectionArgs);
                break;
            case PREFERENCE_ID:
                count = db.update(PREFERENCES_TABLE_NAME, values,
                        NAME+" = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND ("+selection+")" : ""),
                        selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
