package at.dingbat.type.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.dingbat.type.R;
import at.dingbat.type.adapter.AddItemDialogAdapter;
import at.dingbat.type.model.TextStyle;

/**
 * Created by Max on 11/22/2015.
 */
public class DialogUtil {

    public static AlertDialog createFileDialog(final Context context) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_create_file, null);
        b.setView(view);
        final AlertDialog dialog = b.create();
        final EditText text = (EditText) view.findViewById(R.id.dialog_create_file_input);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ((AppCompatButton)view.findViewById(R.id.dialog_create_file_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ((AppCompatButton)view.findViewById(R.id.dialog_create_file_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("at.dingbat.type");
                i.putExtra("action", "createfile");
                i.putExtra("title", text.getText().toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static AlertDialog createFolderDialog(final Context context) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_create_folder, null);
        b.setView(view);
        final AlertDialog dialog = b.create();
        final EditText text = (EditText) view.findViewById(R.id.dialog_create_folder_input);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ((AppCompatButton)view.findViewById(R.id.dialog_create_folder_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ((AppCompatButton)view.findViewById(R.id.dialog_create_folder_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("at.dingbat.type");
                i.putExtra("action", "createfolder");
                i.putExtra("title", text.getText().toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static AlertDialog createAddTextBlockItemDialog(final Context context, ArrayList<TextStyle> master) {
        ArrayList<TextStyle> sorted = new ArrayList<>(master);
        int c = -1;
        for(int i = 0; i < sorted.size(); i++) {
            if(sorted.get(i).type.equals("default")) {
                c = i;
                break;
            }
        }
        if(c > -1) sorted.remove(c);

        Collections.sort(master, new Comparator<TextStyle>() {
            @Override
            public int compare(TextStyle lhs, TextStyle rhs) {
                return lhs.size > rhs.size ? 1 : 0;
            }
        });

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_add_item, null);
        b.setView(view);
        AlertDialog dialog = b.create();

        RecyclerView r = (RecyclerView) view.findViewById(R.id.dialog_add_item_recycler);
        LinearLayoutManager l = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        r.setLayoutManager(l);

        AddItemDialogAdapter a = new AddItemDialogAdapter();
        a.add(sorted);

        r.setAdapter(a);

        return dialog;
    }

}
