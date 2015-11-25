package at.dingbat.type.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import at.dingbat.type.widget.FileListItem;
import at.dingbat.type.widget.FolderListItem;
import at.dingbat.type.widget.SeparatorListItem;
import at.dingbat.type.widget.SubHeaderListItem;

/**
 * Created by Max on 11/21/2015.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    public static final int ITEM_TYPE_FILE = 0;
    public static final int ITEM_TYPE_FOLDER = 1;
    public static final int ITEM_TYPE_SEPARATOR = 2;
    public static final int ITEM_TYPE_SUBHEADER = 3;

    private Context context;
    private ArrayList<DataHolder> holders;
    private ArrayList<Section> sections;

    public Adapter(Context context) {
        this.context = context;
        this.holders = new ArrayList<>();
        this.sections = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        switch (viewType) {
            case ITEM_TYPE_FILE:
                vh = new ViewHolder(new FileListItem(context));
                break;
            case ITEM_TYPE_FOLDER:
                vh = new ViewHolder(new FolderListItem(context));
                break;
            case ITEM_TYPE_SEPARATOR:
                vh = new ViewHolder(new SeparatorListItem(context));
                break;
            case ITEM_TYPE_SUBHEADER:
                vh = new ViewHolder(new SubHeaderListItem(context));
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View v = holder.view;
        DataHolder h = holders.get(position);
        if(h instanceof FileListItem.DataHolder) ((FileListItem) v).setDataHolder((FileListItem.DataHolder) h);
        else if(h instanceof FolderListItem.DataHolder) ((FolderListItem) v).setDataHolder((FolderListItem.DataHolder) h);
        else if(h instanceof SubHeaderListItem.DataHolder) ((SubHeaderListItem) v).setDataHolder((SubHeaderListItem.DataHolder) h);
    }

    @Override
    public int getItemCount() {
        return holders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(holders.get(position) instanceof FileListItem.DataHolder) return ITEM_TYPE_FILE;
        else if(holders.get(position) instanceof FolderListItem.DataHolder) return ITEM_TYPE_FOLDER;
        else if(holders.get(position) instanceof SeparatorListItem.DataHolder) return ITEM_TYPE_SEPARATOR;
        else if(holders.get(position) instanceof SubHeaderListItem.DataHolder) return ITEM_TYPE_SUBHEADER;
        else return super.getItemViewType(position);
    }

    /*public void add(DataHolder holder) {
        this.holders.add(holder);
        this.notifyItemInserted(this.holders.size()-1);
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

    public void addSection(Section s) {
        s.setAdapter(this);
        this.sections.add(s);
    }

    public void notifySectionChanged() {
        holders.clear();
        for(Section s: sections) {
            holders.addAll(s.getHolders());
            Log.d("test", s.title);
        }
        for(DataHolder holder: holders) {
            Log.d("test", holder.toString());
        }
        this.notifyDataSetChanged();
    }

    public static class DataHolder {

    }

}
