package at.dingbat.type.widget;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;

/**
 * Created by Max on 11/22/2015.
 */
public class SubHeaderListItem extends RelativeLayout {

    private TextView title;

    public SubHeaderListItem(Context context) {
        super(context);

        inflate(context, R.layout.widget_subheader_list_item, this);

        title = (TextView) findViewById(R.id.subheader_list_item_title);
    }

    public void setDataHolder(DataHolder holder) {
        this.title.setText(holder.title);
    }

    public static class DataHolder extends Adapter.DataHolder {
        public String title;
        public static DataHolder create(String title) {
            DataHolder holder = new DataHolder();
            holder.title = title;
            return holder;
        }
    }
}
