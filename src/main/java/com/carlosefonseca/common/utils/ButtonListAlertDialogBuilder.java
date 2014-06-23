package com.carlosefonseca.common.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.jetbrains.annotations.NotNull;

public class ButtonListAlertDialogBuilder extends AlertDialog.Builder {
    private final LinearLayout linearLayout;
    private AlertDialog alertDialog;

    public ButtonListAlertDialogBuilder(Context context) {
        super(context);

        this.linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        final ColorDrawable colorDrawable = new ColorDrawable(Color.LTGRAY);
        linearLayout.setDividerDrawable(colorDrawable);
        setView(linearLayout);
    }

    public void addButton(String text, View.OnClickListener listener) {
        Button button = new Button(getContext());
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setText(text);
        button.setOnClickListener(getListener(listener));
        linearLayout.addView(button);
    }


    private View.OnClickListener getListener(final View.OnClickListener listener) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        };
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    @NotNull
    @Override
    public AlertDialog create() {
        alertDialog = super.create();
        return alertDialog;
    }
}
