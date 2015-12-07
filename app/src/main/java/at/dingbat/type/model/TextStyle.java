package at.dingbat.type.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Max on 11/25/2015.
 */
public class TextStyle {
    public String type;
    public String title;
    public int size;
    public String color;
    public int indentation;

    public static TextStyle parseJSON(String type, JSONObject obj) throws JSONException {
        TextStyle style = new TextStyle();
        style.type = type;
        if (obj.has("title")) style.title = obj.getString("title");
        if (obj.has("size")) style.size = obj.getInt("size");
        if (obj.has("color")) style.color = obj.getString("color");
        if (obj.has("indentation")) style.indentation = obj.getInt("indentation");
        return style;
    }

    public JSONObject renderJSON() {
        JSONObject obj = new JSONObject();
        try {
            if(title != null) obj.put("title", title);
            if(size > 0) obj.put("size", size);
            if(color != null) obj.put("color", color);
            if(indentation >= 0) obj.put("indentation", indentation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
