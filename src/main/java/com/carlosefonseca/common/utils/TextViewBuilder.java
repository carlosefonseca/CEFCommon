package com.carlosefonseca.common.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class TextViewBuilder {

    private final Context mContext;
    private String mText;
    private Integer mColor;

    public TextViewBuilder(Context context) {
        mContext = context;
    }

    public TextViewBuilder text(@Nullable String text) {
        mText = text;
        return this;
    }

    public TextViewBuilder color(@Nullable Integer color) {
        mColor = color;
        return this;
    }

    public void apply(TextView textView) {
        textView.setText(mText);
        if (mColor != null) textView.setTextColor(mColor);
    }

    public TextView build() {
        final TextView textView = new TextView(mContext);
        apply(textView);
        return textView;
    }

}
