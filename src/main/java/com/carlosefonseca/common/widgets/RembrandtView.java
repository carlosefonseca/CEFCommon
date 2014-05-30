package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Rembrandt;

public class RembrandtView extends ImageView {
    private static final java.lang.String TAG = CodeUtils.getTag(RembrandtView.class);
    private Rembrandt mRembrandt;

    public RembrandtView(Context context) {
        this(context, new Rembrandt(context));
    }

    public RembrandtView(Context context, Rembrandt rembrandt) {
        super(context);
        this.mRembrandt = rembrandt;
    }

    public RembrandtView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public RembrandtView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Rembrandt getRembrandt() {
        return mRembrandt;
    }

    public void setRembrandt(Rembrandt mRembrandt) {
        this.mRembrandt = mRembrandt;
    }

    public RembrandtView setImageUrl(String url) {
        Rembrandt.with(getContext()).load(url).fadeIn(this);
        return this;
    }
    public RembrandtView setCrossFadeImageUrl(String url) {
        Rembrandt.with(getContext()).load(url).xFade(this);
        return this;
    }
}
