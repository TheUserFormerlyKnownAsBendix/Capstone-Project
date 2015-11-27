package at.dingbat.type.model;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private Map<String, TextStyle> master;
    private ArrayList<TextBlock> blocks;

    public Document(GoogleApiClient client) {
        this.client = client;
        this.locations = new ArrayList<>();
        this.master = new HashMap<>();
        this.blocks = new ArrayList<>();
    }

    public void patchTextBlock(String diff, String uuid) {

    }

    public void parseJSON(String content) {
        try {
            JSONObject document = new JSONObject(content);
            Log.d("test", "object");
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

    public void save() {

    }

    public void getHolders() {

    }

    public static class TextBlock {
        public static String TYPE_TITLE = "title";
        public static String TYPE_SUBTITLE = "subtitle";
        public static String TYPE_PARAGRAPH = "paragraph";
        public static String TYPE_IMAGE = "image";

        public String UUID;
        public TextStyle style;
        public String content;
    }

}
