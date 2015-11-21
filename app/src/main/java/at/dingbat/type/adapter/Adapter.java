package at.dingbat.type.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import at.dingbat.type.widget.FileListItem;

/**
 * Created by Max on 11/21/2015.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    public static final int ITEM_TYPE_FILE = 0;

    private Context context;
    private ArrayList<DataHolder> holders;

    public Adapter(Context context) {
        this.context = context;
        this.holders = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        switch (viewType) {
            case ITEM_TYPE_FILE:
                vh = new ViewHolder(new FileListItem(context));
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View v = holder.view;
        DataHolder h = holders.get(position);
        if(h instanceof FileListItem.DataHolder) {
            ((FileListItem) v).setDataHolder((FileListItem.DataHolder) h);
        }
    }

    @Override
    public int getItemCount() {
        return holders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(holders.get(position) instanceof FileListItem.DataHolder) return ITEM_TYPE_FILE;
        else return super.getItemViewType(position);
    }

    public void add(DataHolder holder) {
        this.holders.add(holder);
        this.notifyItemInserted(this.holders.size()-1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

    public static class DataHolder {

    }

}
