package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.carlosefonseca.common.utils.Rembrandt;

public class RembrandtView extends ImageView {
    public RembrandtView(Context context) {
        super(context);
    }

    public RembrandtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RembrandtView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RembrandtView setImageUrl(String url) {
        Rembrandt.with(getContext()).load(url).into(this);
        return this;
    }
}
