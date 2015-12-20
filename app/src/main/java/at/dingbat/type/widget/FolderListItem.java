package at.dingbat.type.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;

import at.dingbat.type.R;
import at.dingbat.type.activity.BrowserActivity;
import at.dingbat.type.activity.DetailActivity;
import at.dingbat.type.adapter.Adapter;

/**
 * Created by Max on 11/21/2015.
 */
public class FolderListItem extends RelativeLayout {

    private DataHolder holder;

    private LinearLayout root;
    private TextView title;
    private TextView child_count;
    private ImageButton more;

    public FolderListItem(final Context context) {
        super(context);

        inflate(context, R.layout.widget_folder_list_item, this);

        root = (LinearLayout) findViewById(R.id.widget_folder_list_item_root);
        title = (TextView) findViewById(R.id.widget_folder_list_item_title);
        child_count = (TextView) findViewById(R.id.widget_folder_list_item_child_count);
        more = (ImageButton) findViewById(R.id.widget_folder_list_item_more);

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
                inflater.inflate(R.menu.widget_folder_list_item_menu, menu.getMenu());
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.widget_folder_list_item_menu_delete:
                                Intent i = new Intent("at.dingbat.type");
                                i.putExtra("action", "deletefolder");
                                i.putExtra("folder", holder.file.getDriveId().toString());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                                return true;
                        }
                        return false;
                    }
                });
            }
        });

    }

    public void setDataHolder(final DataHolder holder) {
        this.holder = holder;
        title.setText(holder.file.getTitle());
        child_count.setText(holder.childCount + (holder.childCount == 0 ? " Item" : " Items"));

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), BrowserActivity.class);
                i.putExtra("folder", holder.file.getDriveId().toString());
                getContext().startActivity(i);
            }
        });
    }

    public static class DataHolder extends Adapter.DataHolder {
        public Metadata file;
        public int childCount;
        public static DataHolder create(Metadata file, int childCount) {
            DataHolder holder = new DataHolder();
            holder.file = file;
            holder.childCount = childCount;
            return holder;
        }
    }

}
