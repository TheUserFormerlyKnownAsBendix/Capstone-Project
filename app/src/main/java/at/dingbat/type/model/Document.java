package at.dingbat.type.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.widget.TextBlockItem;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class Document extends ContentProvider {

    private GoogleApiClient client;
    private DriveFile file;
    private Metadata metadata;

    private String title;
    private double version;
    private LatLng location;
    private ArrayList<TextStyle> master;
    private ArrayList<TextBlock> blocks;

    private DocumentLoadedCallback documentLoaded;

    private boolean changed = false;

    public Document(GoogleApiClient client) {
        this.client = client;
        this.master = new ArrayList<>();
        this.blocks = new ArrayList<>();
    }

    public void patchTextBlock(String uuid, String content) {
        TextBlock block = getTextBlock(uuid);
        if(block != null && !block.content.equals(content)) {
            block.content = content;
            changed = true;
        }
    }

    public void addTextBlock(TextBlock block) {
        this.blocks.add(block);
    }

    public TextBlock getTextBlock(String UUID) {
        for(TextBlock block: blocks) {
            if(block.UUID.equals(UUID)) return block;
        }
        return null;
    }

    public TextStyle getTextStyle(String type) {
        for(TextStyle s: master) {
            if(s.type.equals(type)) return s;
        }
        return null;
    }

    public JSONObject renderJSON() {
        JSONObject doc = new JSONObject();
        try {
            JSONObject typo = new JSONObject();
            typo.put("version", "1.0");

            JSONObject styles = new JSONObject();
            for(TextStyle style: master) {
                styles.put(style.type, style.renderJSON());
            }

            JSONArray root = new JSONArray();
            for(TextBlock block: blocks) {
                root.put(block.renderJSON());
            }

            doc.put("typo", typo);
            doc.put("location", location);
            doc.put("master", styles);
            doc.put("root", root);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public void parseJSON(String content) {
        try {
            JSONObject document = new JSONObject(content);
            this.version = Double.parseDouble(document.getJSONObject("typo").getString("version"));
            if(this.version == 1) {
                if(document.has("master")) {
                    JSONObject styles = document.getJSONObject("master");
                    Iterator<?> keys = styles.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        try {
                            TextStyle style = TextStyle.parseJSON(key, styles.getJSONObject(key));
                            this.master.add(style);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if(document.has("root")) {
                    JSONArray root = document.getJSONArray("root");
                    for (int i = 0; i < root.length(); i++) {
                        try {
                            JSONObject obj = root.getJSONObject(i);
                            blocks.add(TextBlock.parseJSON(obj, master));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            if(documentLoaded != null) documentLoaded.onDocumentLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void load(DriveFile file) {
        this.file = file;
        ApiUtil.getMetadata(client, file, new ApiUtil.MetadataLoadedCallback() {
            @Override
            public void onMetadataLoaded(DriveResource.MetadataResult result) {
                metadata = result.getMetadata();
                title = metadata.getTitle();
                Map<CustomPropertyKey, String> properties = metadata.getCustomProperties();
                CustomPropertyKey lat = new CustomPropertyKey("lat", CustomPropertyKey.PUBLIC);
                CustomPropertyKey lon = new CustomPropertyKey("lon", CustomPropertyKey.PUBLIC);
                if (properties.containsKey(lat) && properties.containsKey(lon)) {
                    location = new LatLng(Double.parseDouble(properties.get(lat)), Double.parseDouble(properties.get(lon)));
                }
            }
        });
        ApiUtil.readFile(client, file, new ApiUtil.FileReadCallback() {
            @Override
            public void onFileRead(String content) {
                parseJSON(content);
            }
        });
    }

    public void setOnDocumentLoadedListener(DocumentLoadedCallback callback) {
        this.documentLoaded = callback;
    }

    public void save(at.dingbat.apiutils.ApiUtil.DocumentSavedCallback callback) {
        if(changed) {
            ApiUtil.writeFile(client, file, renderJSON().toString(), callback);
        }
    }

    public ArrayList<Adapter.DataHolder> getHolders() {
        ArrayList<Adapter.DataHolder> holders = new ArrayList<>();
        for(TextBlock block: blocks) {
            holders.add(TextBlockItem.DataHolder.create(block));
        }
        return holders;
    }

    public ArrayList<TextStyle> getMaster() {
        return master;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static interface DocumentLoadedCallback {
        void onDocumentLoaded();
    }

}
