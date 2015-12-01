package at.dingbat.type.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Max on 11/25/2015.
 */
public class TextStyle {
    public String type;
    public int size;
    public String color;
    public int indentation;

    public static TextStyle parseJSON(String type, JSONObject obj) throws JSONException {
        TextStyle style = new TextStyle();
        style.type = type;
        if (obj.has("size")) style.size = obj.getInt("size");
        if (obj.has("color")) style.color = obj.getString("color");
        if (obj.has("indentation")) style.indentation = obj.getInt("indentation");
        return style;
    }
}
