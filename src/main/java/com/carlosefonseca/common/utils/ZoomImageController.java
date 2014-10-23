package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ZoomImageController extends ZoomViewController<ImageView> {
    public ZoomImageController(ViewGroup container) {
        super(container);
    }

    public ZoomImageController(Activity activity) {
        super(activity);
    }

    protected ImageView createExpandedView() {
        return new ImageView(getContext());
    }

    public void zoomFromView(ImageView thumb, int hdRes) {
        final ImageView view = getExpandedView();
        view.setImageResource(hdRes);
        zoomFromView(thumb, view);
    }

    public void zoomFromView(ImageView thumb, Bitmap bitmap) {
        final ImageView view = getExpandedView();
        view.setImageBitmap(bitmap);
        zoomFromView(thumb, view);
    }

    public void zoomFromView(ImageView thumb, Drawable drawable) {
        final ImageView view = getExpandedView();
        view.setImageDrawable(drawable);
        zoomFromView(thumb, view);
    }

}
