package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Rembrandt;

public class RembrandtView extends ImageView {
    private static final java.lang.String TAG = CodeUtils.getTag(RembrandtView.class);
    private Rembrandt mRembrandt;

    boolean hideIfNull;
    private String url;

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
        if (mRembrandt == null) mRembrandt = Rembrandt.with(getContext());
        return mRembrandt;
    }

    public void setRembrandt(Rembrandt mRembrandt) {
        this.mRembrandt = mRembrandt;
    }

    public RembrandtView setImageUrl(String url) {
        if (CodeUtils.equals(url, this.url)) return this;
        this.url = url;
        getRembrandt().load(url).hideIfNull(hideIfNull).fadeIn(this);
        return this;
    }
    public RembrandtView setCrossFadeImageUrl(String url) {
        if (CodeUtils.equals(url, this.url)) return this;
        this.url = url;
        getRembrandt().load(url).hideIfNull(hideIfNull).xFade(this);
        return this;
    }

    public boolean isHideIfNull() {
        return hideIfNull;
    }

    /**
     * Set GONE when URL is null or empty, and VISIBLE otherwise. Defaults to false.
     *
     * @param hideIfNull
     */
    public void setHideIfNull(boolean hideIfNull) {
        this.hideIfNull = hideIfNull;
    }
}
