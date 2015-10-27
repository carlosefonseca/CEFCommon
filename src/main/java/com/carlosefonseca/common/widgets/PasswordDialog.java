package com.carlosefonseca.common.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.carlosefonseca.common.CFApp;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.CodeUtils;

public class PasswordDialog {


    /**
     * Creates a simple password input dialog for a static password.
     * The validation and dialog dismissal are automatic.
     */
    public static AlertDialog make(Context context, String description, final String password, final Runnable onCorrect) {

        return make(context, description, new OnValidate() {
            @Override
            public void onValidate(DialogInterface dialog, String input) {
                if (input.equals(password)) {
                    onCorrect.run();
                }
                dialog.dismiss();
            }
        });
    }

    public interface OnValidate {
        void onValidate(DialogInterface dialog, String input);
    }

    /**
     * Creates a simple password input dialog that allows for a custom password validation.
     * You are responsible for dismissing the dialog.
     */
    public static AlertDialog make(Context context, String description, final OnValidate onValidate) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final AlertDialog d;

        alert.setTitle(description);
        alert.setMessage("Password:");

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        input.setGravity(Gravity.CENTER);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        alert.setView(input);

        final DialogInterface.OnClickListener onPositiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                onValidate.onValidate(dialog, value);
            }
        };

        alert.setPositiveButton("OK", onPositiveClick);

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        d = alert.create();
        CodeUtils.setupNumericEditText(d, input, onPositiveClick);

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                CodeUtils.showKeyboard(input);
            }
        });

        return d;
    }


    /**
     * Requires layout with EditText id password_et
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
