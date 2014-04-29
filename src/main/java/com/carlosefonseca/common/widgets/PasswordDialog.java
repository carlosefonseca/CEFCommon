package com.carlosefonseca.common.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.carlosefonseca.common.CFApp;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.CodeUtils;

public class PasswordDialog {
    public static AlertDialog make(Context context, String description, final String password, final Runnable onCorrect) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final AlertDialog d;

        alert.setTitle(description);
        alert.setMessage("Password:");

        // Set an EditText view to get user input
        final EditText input = new EditText(context);

        alert.setView(input);

        final DialogInterface.OnClickListener onPositiveClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (value.equals(password)) {
                    onCorrect.run();
                }
            }
        };

        CodeUtils.setupNumericEditText(input, onPositiveClick);

        alert.setPositiveButton("OK", onPositiveClick);

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        d = alert.create();
        return d;
    }


    /**
     * Requires layout with EditText id password_et
     *
     * @param context
     * @param layoutId
     * @param password
     * @param onCorrect
     * @return
     */
    public static Dialog make(final Context context,
                              int layoutId,
                              int edittext,
                              int ok,
                              int cancel,
                              int error,
                              final String password,
                              final Runnable onCorrect) {

        final Dialog dialog = new Dialog(context, R.style.DialogTranslucent);

        dialog.setContentView(layoutId);

        // Set an EditText view to get user input
        final EditText input = (EditText) dialog.findViewById(edittext);
        final Button positiveButton = (Button) dialog.findViewById(ok);
        final Button negativeButton = (Button) dialog.findViewById(cancel);
        final View errorView = dialog.findViewById(error);

        View.OnClickListener onPositiveClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = input.getText().toString();
                if (CFApp.isTestDevice() || value.equals(password)) {
                    CodeUtils.hideKeyboard(input);
                    onCorrect.run();
                    dialog.dismiss();
                } else {
                    errorView.setVisibility(View.VISIBLE);
                }
            }
        };

        positiveButton.setOnClickListener(onPositiveClick);

        CodeUtils.setupNumericEditText(input, onPositiveClick);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
