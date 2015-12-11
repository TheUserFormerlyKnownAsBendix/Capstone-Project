package at.dingbat.type.adapter;

import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.dingbat.type.model.TextStyle;
import at.dingbat.type.widget.AddItemDialogListItem;

/**
 * Copyright (c) 2015, bd421 GmbH
 * All Rights Reserved
 */
public class AddItemDialogAdapter extends RecyclerView.Adapter {

    private AlertDialog dialog;
    private ArrayList<TextStyle> styles;

    public AddItemDialogAdapter(AlertDialog dialog) {
        this.dialog = dialog;
        styles = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new AddItemDialogListItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).item.setTextStyle(styles.get(position));
        ((ViewHolder)holder).item.setDialog(dialog);
    }

    @Override
    public int getItemCount() {
        return styles.size();
    }

    public void add(String type, TextStyle style) {
        this.styles.add(style);
        notifyItemInserted(this.styles.size()-1);
    }

    public void add(ArrayList<TextStyle> styles) {
        int start = this.styles.size()-1;
        int end = start+styles.size();
        this.styles.addAll(styles);
        notifyItemRangeInserted(start, end);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AddItemDialogListItem item;
        public ViewHolder(AddItemDialogListItem itemView) {
            super(itemView);
            this.item = (AddItemDialogListItem) itemView;
        }
    }

}
