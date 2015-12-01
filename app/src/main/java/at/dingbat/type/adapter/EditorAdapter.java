package at.dingbat.type.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import at.dingbat.type.widget.Editable;
import at.dingbat.type.widget.TextBlockItem;

/**
 * Created by Max on 11/24/2015.
 */
public class EditorAdapter extends RecyclerView.Adapter<EditorAdapter.ViewHolder> {

    public static final int TYPE_TEXT_BLOCK = 0;
    
    private ArrayList<Adapter.DataHolder> holders;

    private boolean isEditable = false;

    public EditorAdapter() {
        this.holders = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        if(viewType == TYPE_TEXT_BLOCK) vh = new ViewHolder(new TextBlockItem(parent.getContext()));
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View v = holder.view;
        if(v instanceof TextBlockItem) ((TextBlockItem) v).setDataHolder((TextBlockItem.DataHolder) holders.get(position));
        ((Editable)v).setEditable(isEditable);
    }

    @Override
    public int getItemCount() {
        return holders.size();
    }

    @Override
    public int getItemViewType(int position) {
        Adapter.DataHolder holder = holders.get(position);
        if(holder instanceof TextBlockItem.DataHolder) return TYPE_TEXT_BLOCK;
        return super.getItemViewType(position);
    }

    public void add(Adapter.DataHolder holder) {
        this.holders.add(holder);
        this.notifyItemInserted(this.holders.size()-1);
    }

    public void add(ArrayList<Adapter.DataHolder> holders) {
        int start = this.holders.size()-1;
        int end = start+holders.size();
        this.holders.addAll(holders);
        notifyItemRangeInserted(start, end);
    }

    public void editable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

}
