package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.carlosefonseca.common.widgets.ZoomableUILView;
import android.support.annotation.Nullable;

import java.io.File;

public class ZoomZoomableUILController extends ZoomViewController<ZoomableUILView> {
    public ZoomZoomableUILController(ViewGroup container) {
        super(container);
    }

    public ZoomZoomableUILController(Activity activity) {
        super(activity);
    }

    @Override
    protected ZoomableUILView createExpandedView() {
        return new ZoomableUILView(getContext());
    }

    public void zoomFromView(ImageView thumb, File file) {
        final ZoomableUILView view = getExpandedView();
        view.setImageFile(file);
        zoomFromView(thumb, view);
    }

    public void zoomFromView(ImageView thumb, @Nullable String url) {
        final ZoomableUILView view = getExpandedView();
        view.setImageUrl(url);
        zoomFromView(thumb, view);
    }
}
