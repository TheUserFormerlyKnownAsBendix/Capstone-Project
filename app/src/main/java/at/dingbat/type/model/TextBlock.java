package at.dingbat.type.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class TextBlock {
    public static String TYPE_TITLE = "title";
    public static String TYPE_SUBTITLE = "subtitle";
    public static String TYPE_PARAGRAPH = "paragraph";
    public static String TYPE_IMAGE = "image";

    public String UUID;
    public TextStyle style;
    public String type;
    public String content;

    public static TextBlock parseJSON(JSONObject obj, ArrayList<TextStyle> master) throws JSONException {
        TextBlock block = new TextBlock();
        if(obj.has("UUID")) block.UUID = obj.getString("UUID");
        if(obj.has("type")) block.type = obj.getString("type");
        if(obj.has("content")) block.content = obj.getString("content");
        if(obj.has("style")) block.style = TextStyle.parseJSON(block.UUID, obj.getJSONObject("style"));
        else if(block.type != null) {
            for(TextStyle s: master) {
                if(s.type.equals(block.type)) block.style = s;
            }
        } else {
            for(TextStyle s: master) {
                if(s.type.equals("default")) block.style = s;
            }
        }
        return block;
    }

    public JSONObject renderJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("UUID", UUID);
            if(type != null) obj.put("type", type);
            obj.put("content", content);
            if(style != null) obj.put("style", style.renderJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
