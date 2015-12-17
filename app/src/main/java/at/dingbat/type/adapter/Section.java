package at.dingbat.type.adapter;

import android.util.Log;

import java.util.ArrayList;

import at.dingbat.type.widget.SeparatorListItem;
import at.dingbat.type.widget.SubHeaderListItem;

/**
 * Created by Max on 11/22/2015.
 */
public class Section {

    public String title;
    public boolean showSeparator;

    private Adapter adapter;
    private ArrayList<Adapter.DataHolder> holders;

    public Section(String title) {
        this.title = title;
        this.showSeparator = false;
        holders = new ArrayList<>();
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.adapter.notifySectionChanged();
    }

    public Section showSeparator(boolean show) {
        this.showSeparator = show;
        if(this.adapter != null) this.adapter.notifySectionChanged();
        return this;
    }

    public void add(Adapter.DataHolder holder) {
        this.holders.add(holder);
        if(this.adapter != null) this.adapter.notifySectionChanged();
    }

    public ArrayList<Adapter.DataHolder> getHolders() {
        if(this.holders.size() == 0) return this.holders;
        ArrayList<Adapter.DataHolder> h = new ArrayList<>(holders);
        h.add(0, SubHeaderListItem.DataHolder.create(this.title));
        if(this.showSeparator) h.add(new SeparatorListItem.DataHolder());
        return h;
    }

    public void clear() {
        this.holders.clear();
        if(this.adapter != null) this.adapter.notifySectionChanged();
    }

}
