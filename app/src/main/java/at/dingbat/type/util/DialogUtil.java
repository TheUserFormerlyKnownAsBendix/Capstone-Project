package at.dingbat.type.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import at.dingbat.type.R;
import at.dingbat.type.adapter.AddItemDialogAdapter;
import at.dingbat.type.model.TextStyle;

/**
 * Created by Max on 11/22/2015.
 */
public class DialogUtil {

    public static AlertDialog createFileDialog(final Context context, final DriveFolder folder) {
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
                i.putExtra("folder", folder.getDriveId().toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static AlertDialog createRenameFileDialog(final Context context, final DriveFile file,  String title, final RenamedCallback callback) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_rename_file, null);
        b.setView(view);
        final AlertDialog dialog = b.create();
        final EditText text = (EditText) view.findViewById(R.id.dialog_rename_file_input);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        text.setText(title);

        ((AppCompatButton)view.findViewById(R.id.dialog_rename_file_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ((AppCompatButton)view.findViewById(R.id.dialog_rename_file_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("at.dingbat.type");
                i.putExtra("action", "renamefile");
                i.putExtra("title", text.getText().toString());
                i.putExtra("file", file.getDriveId().toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                callback.renamed(text.getText().toString());
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static AlertDialog createFolderDialog(final Context context, final DriveFolder folder) {
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
                i.putExtra("folder", folder.getDriveId().toString());
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

        AddItemDialogAdapter a = new AddItemDialogAdapter(dialog);
        a.add(sorted);

        r.setAdapter(a);

        return dialog;
    }

    public static AlertDialog createEditStyleDialog(final Context context, final TextStyle style, final StyleChangedCallback callback, final TextBlockRemovedCallback removedCallback) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_edit_style, null);
        b.setView(view);
        final AlertDialog dialog = b.create();

        ((TextView)view.findViewById(R.id.dialog_edit_style_title)).setText(style.title + " " + context.getString(R.string.properties));

        ((Button)view.findViewById(R.id.dialog_edit_style_increase_indent)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.indentation = Math.min(10, style.indentation + 1);
                callback.styleChanged(style);
            }
        });

        ((Button)view.findViewById(R.id.dialog_edit_style_decrease_indent)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.indentation = Math.max(0, style.indentation - 1);
                callback.styleChanged(style);
            }
        });

        RadioButton radio_black = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_black);
        RadioButton radio_grey = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_grey);
        RadioButton radio_red = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_red);
        RadioButton radio_amber = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_amber);
        RadioButton radio_green = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_green);
        RadioButton radio_blue = (RadioButton) view.findViewById(R.id.dialog_edit_style_radio_blue);

        int color = Color.parseColor(style.color);
        if(color == context.getResources().getColor(R.color.grey)) {
            radio_grey.setChecked(true);
        } else if(color == context.getResources().getColor(R.color.red)) {
            radio_red.setChecked(true);
        } else if(color == context.getResources().getColor(R.color.amber)) {
            radio_amber.setChecked(true);
        } else if(color == context.getResources().getColor(R.color.green)) {
            radio_green.setChecked(true);
        } else if(color == context.getResources().getColor(R.color.blue)) {
            radio_blue.setChecked(true);
        } else {
            radio_black.setChecked(true);
        }

        radio_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.black)));
            }
        });

        radio_grey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.grey)));
            }
        });

        radio_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.red)));
            }
        });

        radio_amber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.amber)));
            }
        });

        radio_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.green)));
            }
        });

        radio_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                style.color = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.blue)));
            }
        });



        ((AppCompatButton)view.findViewById(R.id.dialog_edit_style_remove)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removedCallback.removed();
                dialog.dismiss();
            }
        });
        ((AppCompatButton)view.findViewById(R.id.dialog_edit_style_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ((AppCompatButton)view.findViewById(R.id.dialog_edit_style_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.styleChanged(style);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static interface StyleChangedCallback {
        void styleChanged(TextStyle style);
    }

    public static interface TextBlockRemovedCallback {
        void removed();
    }

    public static interface RenamedCallback {
        void renamed(String title);
    }

}
