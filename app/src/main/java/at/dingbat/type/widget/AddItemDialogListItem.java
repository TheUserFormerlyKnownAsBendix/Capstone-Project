package at.dingbat.type.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.dingbat.type.R;
import at.dingbat.type.model.TextStyle;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class AddItemDialogListItem extends RelativeLayout {

    private TextStyle style;

    private TextView title;
    private TextView primary_text;

    public AddItemDialogListItem(Context context) {
        super(context);

        inflate(context, R.layout.widget_add_item_list_item, this);

        this.title = (TextView) findViewById(R.id.widget_add_item_list_item_title);
        this.primary_text = (TextView) findViewById(R.id.widget_add_item_list_item_primary_title_short);

    }

    public void setTextStyle(TextStyle style) {
        this.style = style;
        this.title.setText(style.type);
        this.title.setTextColor(Color.parseColor(style.color));
        this.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.size);
    }

}
