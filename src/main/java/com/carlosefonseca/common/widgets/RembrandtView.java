package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import bolts.Task;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.ImageUtils;
import com.carlosefonseca.common.utils.Rembrandt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public class RembrandtView extends ImageView {
    private static final java.lang.String TAG = CodeUtils.getTag(RembrandtView.class);
    private Rembrandt mRembrandt;

    boolean hideIfNull;
    private String url;
    private File file;

    @Nullable private Double aspectRatio;

    int placeholder;

    public RembrandtView(Context context) {
        this(context, new Rembrandt(context));
    }

    public RembrandtView(Context context, @Nullable Rembrandt rembrandt) {
        super(context);
        this.mRembrandt = rembrandt == null ? Rembrandt.with(context) : rembrandt;
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

    public synchronized RembrandtView setImageUrl(@Nullable String url) {
        if (CodeUtils.equals(url, this.url)) {
            if (url == null) setVisibility(GONE);
            return this;
        }
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
        this.file = null;
        if (!animated) {
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).into(this);
        } else if (this.url != null) {
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).xFade(this);
        } else {
            //noinspection ConstantConditions
            return getRembrandt().load(url).hideIfNull(hideIfNull).placeholder(placeholder).fadeIn(this);
        }
    }

    public synchronized bolts.Task<Void> setImageFile(File file, boolean animated) {
        if (CodeUtils.equals(file, this.file)) return Task.forResult(null);
        this.file = file;
        this.url = null;
        if (!animated) {
            return getRembrandt().load(file).hideIfNull(hideIfNull).placeholder(placeholder).into(this);
        } else if (this.file != null) {
            return getRembrandt().load(file).hideIfNull(hideIfNull).placeholder(placeholder).xFade(this);
        } else {
            //noinspection ConstantConditions
            return getRembrandt().load(file).hideIfNull(hideIfNull).placeholder(placeholder).fadeIn(this);
        }
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

    @Nullable
    public Double getAspectRatio() {
        return aspectRatio;
    }

    /**
     * Ex: 3/5 <-> height / width
     *
     * @param aspectRatio
     */
    public void setAspectRatio(@Nullable Double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setAspectRatioFromImage(@NonNull File image) {
        final double aspectRatio1 = ImageUtils.getAspectRatio(image);
        if (aspectRatio1 != 0) aspectRatio = aspectRatio1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (aspectRatio != null) {
            final int h = (int) (getMeasuredWidth() * aspectRatio);
            setMeasuredDimension(widthMeasureSpec, h);
        }
    }
}
