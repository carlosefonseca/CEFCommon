package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Gogh;
import com.carlosefonseca.common.utils.UIL;

import java.io.File;

public class GoghImageView extends ImageView {
    private static final String TAG = CodeUtils.getTag(GoghImageView.class);

    boolean hideIfNull;

    @Nullable private Double aspectRatio;

    int placeholder;
    private String uri;

    public GoghImageView(Context context) {
        super(context);
    }

    public GoghImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public GoghImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public synchronized void setImageUrl(String url) {setImageUrl(url, true);}

    public synchronized void setImageUrl(String url, boolean xFade) {
        setImageUri(UIL.getUri(url), xFade);
    }

    public Gogh goghForImageUrl(String url) {
        return Gogh.loadPhotoURI(url).hideIfNull(hideIfNull).placeholder(placeholder);
    }

    public void setGogh(Gogh gogh) {setGogh(gogh, true);}

    public void setGogh(Gogh gogh, boolean xFade) {
        if (CodeUtils.equals(gogh.getUri(), this.uri)) return;
        if (xFade) {
            gogh.xFade(this);
        } else {
            gogh.fadeIn(this);
        }
        this.uri = gogh.getUri();
    }

    private void setImageUri(String uri, boolean xFade) {
        if (CodeUtils.equals(uri, this.uri)) return;
        setGogh(goghForImageUrl(uri), xFade);
        this.uri = uri;
    }

    public synchronized void setImageFile(File file, boolean animated) {
        setImageUri(UIL.getUri(file), animated);
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
        this.uri = null;
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

    public void setAspectRatio(@Nullable Double aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec,
                        aspectRatio != null ? (int) (widthMeasureSpec * aspectRatio) : heightMeasureSpec);
    }
}
