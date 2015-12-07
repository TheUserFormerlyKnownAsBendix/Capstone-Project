package at.dingbat.type.model;

import android.util.Log;

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
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.StringUtills;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class Document {

    private GoogleApiClient client;
    private DriveFile file;

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

    public void patchTextBlock(String uuid, String content) {
        TextBlock block = getTextBlock(uuid);
        if(block != null) block.content = content;
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

    public JSONObject renderJSON() {
        JSONObject doc = new JSONObject();
        try {
            JSONObject typo = new JSONObject();
            typo.put("version", "1.0");

            JSONArray location = new JSONArray();
            for(LatLng latlng: locations) {
                JSONObject l = new JSONObject();
                l.put("lat", latlng.latitude);
                l.put("lon", latlng.longitude);
                location.put(l);
            }

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
        this.file = file;
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
        String f = renderJSON().toString();
        Log.d("text", "Saving file: " + f);
        ApiUtil.writeFile(client, file, renderJSON().toString());
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
