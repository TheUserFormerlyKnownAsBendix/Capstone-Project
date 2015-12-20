package at.dingbat.type.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.metadata.MetadataField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.dingbat.type.BuildConfig;
import at.dingbat.type.R;
import at.dingbat.type.activity.DetailActivity;
import at.dingbat.type.activity.EditorActivity;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.model.Document;

/**
 * Created by Max on 11/21/2015.
 */
public class FileListItem extends RelativeLayout {

    private Context context;

    private DataHolder holder;

    private LinearLayout root;
    private TextView title;
    private TextView modified;
    private ImageButton more;

    public FileListItem(final Context context) {
        super(context);

        this.context = context;

        inflate(context, R.layout.widget_file_list_item, this);

        root = (LinearLayout) findViewById(R.id.widget_file_list_item_root);
        title = (TextView) findViewById(R.id.widget_file_list_item_title);
        modified = (TextView) findViewById(R.id.widget_file_list_item_modified);
        more = (ImageButton) findViewById(R.id.widget_file_list_item_more);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, EditorActivity.class);
                i.putExtra("file", holder.file.getDriveId().encodeToString());
                context.startActivity(i);
            }
        });

        more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = null;
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    menu = new PopupMenu(context, more, ALIGN_END);
                } else {
                    menu = new PopupMenu(context, more);
                }
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.widget_file_list_item_menu, menu.getMenu());
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.widget_file_list_item_menu_details:
                                Intent i = new Intent(context, DetailActivity.class);
                                i.putExtra("file", holder.file.getDriveId().toString());
                                context.startActivity(i);
                                return true;
                            case R.id.widget_file_list_item_menu_delete:
                                Intent i2 = new Intent("at.dingbat.type");
                                i2.putExtra("action", "deletefile");
                                i2.putExtra("file", holder.file.getDriveId().toString());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(i2);
                                return true;
                        }
                        return false;
                    }
                });
            }
        });

    }

    public void setDataHolder(DataHolder holder) {
        this.holder = holder;
        title.setText(holder.file.getTitle());

        modified.setText(Document.formatDate(context, holder.file.getModifiedDate()));
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
