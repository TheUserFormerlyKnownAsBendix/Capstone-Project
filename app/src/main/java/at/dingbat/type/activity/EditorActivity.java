package at.dingbat.type.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import at.dingbat.type.R;
import at.dingbat.type.adapter.EditorAdapter;
import at.dingbat.type.editor.TextStyle;
import at.dingbat.type.widget.Editable;
import at.dingbat.type.widget.TextBlockItem;

public class EditorActivity extends AppCompatActivity {

    private FloatingActionButton fab_add;

    private RecyclerView recycler;
    private LinearLayoutManager layout_manager;
    private EditorAdapter adapter;

    private boolean editable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_editor_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Untitled");

        fab_add = (FloatingActionButton) findViewById(R.id.activity_editor_add);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditable(!editable);
            }
        });

        recycler = (RecyclerView) findViewById(R.id.activity_editor_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new EditorAdapter();

        TextStyle heading = new TextStyle();
        heading.size = 24;
        heading.color = "#000000";
        heading.indentation = 0;

        TextStyle subheading = new TextStyle();
        subheading.size = 18;
        subheading.color = "#696969";
        subheading.indentation = 1;

        TextStyle paragraph = new TextStyle();
        paragraph.size = 12;
        paragraph.color = "#0000FF";
        paragraph.indentation = 2;

        adapter.add(TextBlockItem.DataHolder.create("", "Title", heading));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle 2", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));
        adapter.add(TextBlockItem.DataHolder.create("", "Subtitle 3", subheading));
        adapter.add(TextBlockItem.DataHolder.create("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", paragraph));


        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

    }

    private void setEditable(boolean editable) {
        if(this.editable && !editable) {
            int start = layout_manager.findFirstVisibleItemPosition();
            int end = layout_manager.findLastVisibleItemPosition();
            for(int i = start; i <= end; i++) {
                ((Editable) recycler.findViewHolderForAdapterPosition(i).itemView).onExitEditMode();
            }
        } else {
            int start = layout_manager.findFirstVisibleItemPosition();
            int end = layout_manager.findLastVisibleItemPosition();
            for(int i = start; i <= end; i++) {
                ((Editable) recycler.findViewHolderForAdapterPosition(i).itemView).onEnterEditMode();
            }
        }
        this.editable = editable;
        adapter.editable(editable);
    }

}
