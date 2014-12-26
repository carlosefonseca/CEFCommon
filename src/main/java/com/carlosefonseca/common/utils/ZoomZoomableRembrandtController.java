package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.carlosefonseca.common.widgets.ZoomableRembrandtView;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ZoomZoomableRembrandtController extends ZoomViewController<ZoomableRembrandtView> {
    public ZoomZoomableRembrandtController(ViewGroup container) {
        super(container);
    }

    public ZoomZoomableRembrandtController(Activity activity) {
        super(activity);
    }

    @Override
    protected ZoomableRembrandtView createExpandedView() {
        return new ZoomableRembrandtView(getContext());
    }

    public void zoomFromView(ImageView thumb, File file) {
        final ZoomableRembrandtView view = getExpandedView();
        view.setImageFile(file);
        zoomFromView(thumb, view);
    }

    public void zoomFromView(ImageView thumb, @Nullable String url) {
        final ZoomableRembrandtView view = getExpandedView();
        view.setImageUrl(url);
        zoomFromView(thumb, view);
    }
}
