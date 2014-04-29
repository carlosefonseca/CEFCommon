package com.carlosefonseca.common.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.carlosefonseca.common.R;

public class YesNoDialog extends DialogFragment {

    private TextView title;
    private TextView message;
    private Button positive;
    private Button negative;
    private CharSequence positiveText;
    private View.OnClickListener positiveListener;
    private CharSequence negativeText;
    private View.OnClickListener negativeListener;
    private CharSequence messageText;

    public YesNoDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.DialogTranslucent);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.carlosefonseca.common.R.layout.yes_no_popup, container, false);
//            title = (TextView) v.findViewById(R.id.title);
        message = (TextView) v.findViewById(R.id.message);
        positive = (Button) v.findViewById(R.id.positive);
        negative = (Button) v.findViewById(R.id.negative);

        negative.setVisibility(View.GONE);

        if (messageText != null) message.setText(messageText);
        if (positiveText != null) setPositiveButton(positiveText, positiveListener);
        if (negativeText != null) setNegativeButton(negativeText, negativeListener);

        return v;
    }

//        public YesNoDialog setTitle(int titleId) {  }

//        public android.app.AlertDialog.Builder setTitle(java.lang.CharSequence title) { /* compiled code */ }

//        public android.app.AlertDialog.Builder setCustomTitle(android.view.View customTitleView) { /* compiled code */ }

    public YesNoDialog setMessage(int messageId) {
        setMessage(getResources().getText(messageId));
        return this;
    }

    public YesNoDialog setMessage(CharSequence m) {
        if (message != null) {
            message.setText(m);
        } else {
            this.messageText = m;
        }
        return this;
    }

//        public android.app.AlertDialog.Builder setIcon(int iconId) { /* compiled code */ }

//        public android.app.AlertDialog.Builder setIcon(android.graphics.drawable.Drawable icon) { /* compiled code */ }

//        public android.app.AlertDialog.Builder setIconAttribute(int attrId) { /* compiled code */ }

    public YesNoDialog setPositiveButton(int textId, View.OnClickListener listener) {
        return setPositiveButton(getResources().getString(textId), listener);
    }

    public YesNoDialog setPositiveButton(CharSequence text, final View.OnClickListener listener) {
        if (positive != null) {
            positive.setText(text);
            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    listener.onClick(null);
                }
            });
        } else {
            this.positiveText = text;
            this.positiveListener = listener;
        }
        return this;
    }

    public YesNoDialog setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        return setPositiveButton(text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(null, 0);
            }
        });
    }

    public YesNoDialog setNegativeButton(int textId, View.OnClickListener listener) {
        return setNegativeButton(getResources().getString(textId), listener);
    }

    public YesNoDialog setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        return setNegativeButton(text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(null, 0);
            }
        });
    }

    public YesNoDialog setNegativeButton(CharSequence text, final View.OnClickListener listener) {
        if (negative != null) {
            negative.setVisibility(View.VISIBLE);
            negative.setText(text);
            negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    listener.onClick(null);
                }
            });
        } else {
            this.negativeText = text;
            this.negativeListener = listener;
        }


        return this;
    }

/*
        public android.app.AlertDialog.Builder setNeutralButton(int textId,
                                                                android.content.DialogInterface.OnClickListener listener) { */
/* compiled code *//*
 }

        public android.app.AlertDialog.Builder setNeutralButton(java.lang.CharSequence text,
                                                                android.content.DialogInterface.OnClickListener listener) { */
/* compiled code *//*
 }
*/

    public YesNoDialog setMyCancelable(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }


    public static Dialog showYesNoDialog(Context context,
                                         String text,
                                         final View.OnClickListener yes_action,
                                         final View.OnClickListener no_action,
                                         CharSequence ok_text,
                                         CharSequence cancel_text,
                                         int layout_id,
                                         int popup_text_id,
                                         int ok_bt_id,
                                         int cancel_btn_id) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(layout_id);

        TextView info_text = (TextView) dialog.findViewById(popup_text_id);
        info_text.setText(text);

        final Button ok_bt = (Button) dialog.findViewById(ok_bt_id);
        final Button cancel_bt = (Button) dialog.findViewById(cancel_btn_id);

        if (ok_text != null) ok_bt.setText(ok_text);
        if (cancel_text != null) cancel_bt.setText(cancel_text);

        ok_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ok_bt.setEnabled(false);
                cancel_bt.setEnabled(false);
                yes_action.onClick(ok_bt);
                dialog.dismiss();
            }
        });

        cancel_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ok_bt.setEnabled(false);
                cancel_bt.setEnabled(false);
                if (no_action != null) no_action.onClick(cancel_bt);
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
