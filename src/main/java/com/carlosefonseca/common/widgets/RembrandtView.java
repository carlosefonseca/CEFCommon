package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import bolts.Task;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Rembrandt;

public class RembrandtView extends ImageView {
    private static final java.lang.String TAG = CodeUtils.getTag(RembrandtView.class);
    private Rembrandt mRembrandt;

    boolean hideIfNull;
    private String url;

    int placeholder;

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
        if (mRembrandt == null) mRembrandt = Rembrandt.with(getContext()); // DANGEROUS ON LISTS
        return mRembrandt;
    }

    public void setRembrandt(Rembrandt mRembrandt) {
        this.mRembrandt = mRembrandt;
    }

    public synchronized RembrandtView setImageUrl(String url) {
        if (CodeUtils.equals(url, this.url)) return this;
        if (this.url != null) {
            getRembrandt().load(url).hideIfNull(hideIfNull).xFade(this);
        } else {
            getRembrandt().load(url).hideIfNull(hideIfNull).fadeIn(this);
        }
        this.url = url;
        return this;
    }

    public synchronized bolts.Task<Void> setImageUrl(String url, boolean animated) {
        if (CodeUtils.equals(url, this.url)) return Task.forResult(null);
        this.url = url;
        if (!animated) {
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).into(this);
        } else if (this.url != null) {
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).xFade(this);
        } else {
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).fadeIn(this);
        }
//        return this;
    }
    public RembrandtView setCrossFadeImageUrl(String url) {
        if (CodeUtils.equals(url, this.url)) return this;
        this.url = url;
        getRembrandt().load(url).hideIfNull(hideIfNull).xFade(this);
        return this;
    }

    public int getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(int placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        this.url = null;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        this.url = null;
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
