package at.dingbat.type.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.metadata.MetadataField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;

/**
 * Created by Max on 11/21/2015.
 */
public class FileListItem extends RelativeLayout {

    private DataHolder holder;

    private LinearLayout root;
    private TextView title;
    private TextView modified;

    public FileListItem(Context context) {
        super(context);

        inflate(context, R.layout.widget_file_list_item, this);

        root = (LinearLayout) findViewById(R.id.widget_file_list_item_root);
        title = (TextView) findViewById(R.id.widget_file_list_item_title);
        modified = (TextView) findViewById(R.id.widget_file_list_item_modified);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    public void setDataHolder(DataHolder holder) {
        this.holder = holder;
        title.setText(holder.file.getTitle());

        Date date = new Date();
        //Last change was made less than an hour ago -> show minutes
        long diff = date.getTime() - holder.file.getModifiedDate().getTime();
        // Less than one minute ago
        if(diff < (1000*60)) {
            modified.setText("Now");
        }
        // Less than an hour ago
        else if(diff < (1000*60*60)) {
            modified.setText(Math.round(diff/(1000*60))+" minutes ago");
        }
        // Less than a day ago
        else if(diff < (1000*60*60*24)) {
            modified.setText(Math.round(diff/(1000*60*60))+" hours ago");
        }
        // Less than two days ago
        else if(diff < (1000*60*60*24*2)) {
            modified.setText("Yesterday");
        }
        // More than two days ago - show date
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd");
            modified.setText(sdf.format(holder.file.getModifiedDate()));
        }
    }

    public static class DataHolder extends Adapter.DataHolder {
        public Metadata file;
        public static DataHolder create(Metadata file) {
            DataHolder holder = new DataHolder();
            holder.file = file;
            return holder;
        }
    }

}
