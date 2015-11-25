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
import android.view.View;
import android.widget.EditText;

import at.dingbat.type.R;
import at.dingbat.type.adapter.EditorAdapter;
import at.dingbat.type.editor.TextStyle;
import at.dingbat.type.widget.TextBlockItem;

public class EditorActivity extends AppCompatActivity {

    private AppBarLayout abl;
    private EditText toolbar_title;

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

        toolbar_title = (EditText) findViewById(R.id.activity_editor_toolbar_title);

        if(editable) getSupportActionBar().setTitle("");
        else {
            toolbar_title.setVisibility(View.GONE);
            getSupportActionBar().setTitle("Untitled");
        }

        abl = (AppBarLayout) findViewById(R.id.activity_editor_appbar_layout);
        abl.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(editable) {
                    if (verticalOffset > -10) {
                        getSupportActionBar().setTitle("");
                        toolbar_title.setVisibility(View.VISIBLE);
                    } else {
                        getSupportActionBar().setTitle(toolbar_title.getText());
                        toolbar_title.setVisibility(View.GONE);
                    }
                }
            }
        });

        fab_add = (FloatingActionButton) findViewById(R.id.activity_editor_add);

        recycler = (RecyclerView) findViewById(R.id.activity_editor_recycler);
        layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter = new EditorAdapter();

        TextStyle style = new TextStyle();
        style.size = 24;
        style.color = "#FF0000";
        style.indentation = 0;

        adapter.add(TextBlockItem.DataHolder.create("", "Test", style));

        recycler.setLayoutManager(layout_manager);
        recycler.setAdapter(adapter);

    }

}
