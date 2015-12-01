package at.dingbat.type.model;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.util.ApiUtil;
import at.dingbat.type.widget.TextBlockItem;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class Document {

    private GoogleApiClient client;

    private double version;
    private ArrayList<LatLng> locations;
    private ArrayList<TextStyle> master;
    private ArrayList<TextBlock> blocks;

    private DocumentLoadedCallback documentLoaded;

    public Document(GoogleApiClient client) {
        this.client = client;
        this.locations = new ArrayList<>();
        this.master = new ArrayList<>();
        this.blocks = new ArrayList<>();
    }

    public void patchTextBlock(String diff, String uuid) {

    }

    public void parseJSON(String content) {
        try {
            JSONObject document = new JSONObject(content);
            this.version = Double.parseDouble(document.getJSONObject("typo").getString("version"));
            if(this.version == 1) {
                if(document.has("locations")) {
                    JSONArray loc = document.getJSONArray("locations");
                    for (int i = 0; i < loc.length(); i++) {
                        LatLng latlng = new LatLng(loc.getJSONObject(i).getDouble("lat"), loc.getJSONObject(i).getDouble("lng"));
                        this.locations.add(latlng);
                    }
                }
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

    public void save() {

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

    public static interface DocumentLoadedCallback {
        void onDocumentLoaded();
    }

}
