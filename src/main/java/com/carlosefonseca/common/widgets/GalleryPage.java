package com.carlosefonseca.common.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.carlosefonseca.common.R;

import java.io.File;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class GalleryPage extends FrameLayout {
    int position = -1;
    File file;
    String url;
    ImageView imageView;

    public GalleryPage(Context context) {
        super(context);
        init();
    }

    public GalleryPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GalleryPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GalleryPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
//        imageView = new ImageView(getContext());
//        imageView.setAdjustViewBounds(true);
//        imageView.setId(R.id.image);
//        addView(imageView, new LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imageView = (ImageView) findViewById(R.id.image);
        if (imageView == null) {
            imageView = new ImageView(getContext());
            imageView.setAdjustViewBounds(true);
            imageView.setId(R.id.image);
            addView(imageView, new LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER));
        }
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        imageView.setScaleType(scaleType);
        if (scaleType == ImageView.ScaleType.FIT_XY || scaleType == ImageView.ScaleType.CENTER_CROP) {
            imageView.getLayoutParams().width = MATCH_PARENT;
        }
    }

    public void set(String url, int position) {
        this.url = url;
        this.file = null;
        this.position = position;
    }

    public void set(File file, int position) {
        this.url = null;
        this.file = file;
        this.position = position;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
