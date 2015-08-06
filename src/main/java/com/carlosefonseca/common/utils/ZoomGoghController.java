package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.carlosefonseca.common.widgets.GoghImageView;

import java.io.File;

public class ZoomGoghController extends ZoomImageController {

    public ZoomGoghController(ViewGroup container) {
        super(container);
    }

    public ZoomGoghController(Activity activity) {
        super(activity);
    }

    @Override
    protected ImageView createExpandedView() {
        GoghImageView goghImageView = new GoghImageView(getContext());
        goghImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return goghImageView;
    }

    public void zoomFromView(final ImageView thumb, String url) {
        final GoghImageView view = (GoghImageView) getExpandedView();
        view.setImageBitmap(UIL.loadSync(url));
        zoomFromView(thumb, view);
    }

    public void zoomFromView(final ImageView thumb, File file) {
        final GoghImageView view = (GoghImageView) getExpandedView();
        view.setImageBitmap(UIL.loadSync(file));
        zoomFromView(thumb, view);
    }
}
