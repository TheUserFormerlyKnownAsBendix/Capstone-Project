package at.dingbat.type.widget;

import android.content.Context;
import android.widget.RelativeLayout;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;

/**
 * Created by Max on 11/22/2015.
 */
public class SeparatorListItem extends RelativeLayout {

    public SeparatorListItem(Context context) {
        super(context);
        inflate(context, R.layout.widget_separator_list_item, this);
    }

    public static class DataHolder extends Adapter.DataHolder {

    }

}
