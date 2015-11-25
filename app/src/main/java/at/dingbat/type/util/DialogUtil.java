package at.dingbat.type.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import at.dingbat.type.R;

/**
 * Created by Max on 11/22/2015.
 */
public class DialogUtil {

    public static AlertDialog createFolderTitleDialog(final Context context) {
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

        return dialog;
    }

}
