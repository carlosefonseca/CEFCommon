package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.carlosefonseca.common.widgets.ZoomableUILView;
import android.support.annotation.Nullable;

import java.io.File;

public class ZoomZoomZoom extends ZoomViewController<FrameLayout> {


    public ZoomZoomZoom(ViewGroup container) {
        super(container);
    }


    public ZoomZoomZoom(Activity activity) {
        super(activity);
    }

    @Override
    protected FrameLayout createExpandedView() {
        FrameLayout frameLayout = new FrameLayout(getContext());
        ZoomableUILView child = new ZoomableUILView(getContext());
        child.setFocusable(false);
        frameLayout.addView(child);
        return frameLayout;
    }

    public void zoomFromView(ImageView thumb, File file) {
        final FrameLayout view = getExpandedView();
        ((ZoomableUILView) view.getChildAt(0)).setImageFile(file);
        zoomFromView(thumb, view);
    }

    public void zoomFromView(ImageView thumb, @Nullable String url) {
        final FrameLayout view = getExpandedView();
        ((ZoomableUILView) view.getChildAt(0)).setImageUrl(url);
        zoomFromView(thumb, view);
    }
}
