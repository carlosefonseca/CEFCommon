package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.widgets.RembrandtView;
import android.support.annotation.Nullable;

import java.io.File;

public class ZoomRembrandtController extends ZoomImageController {


    @Nullable private Rembrandt rembrandt;

    public ZoomRembrandtController(ViewGroup container) {
        super(container);
    }

    public ZoomRembrandtController(Activity activity) {
        super(activity);
    }

    public ZoomRembrandtController(ViewGroup view, @Nullable Rembrandt rembrandt) {
        super(view);
        this.rembrandt = rembrandt;
    }

    @Override
    protected ImageView createExpandedView() {
        return new RembrandtView(getContext(), rembrandt);
    }

    public void zoomFromView(final ImageView thumb, String url) {
        final ImageView view = getExpandedView();
        ((RembrandtView) view).setImageUrl(url, false).continueWith(new Continuation<Void, Object>() {
            @Nullable
            @Override
            public Object then(Task<Void> task) throws Exception {
                zoomFromView(thumb, view);
                return null;
            }
        });
    }

    public void zoomFromView(final ImageView thumb, File file) {
        final ImageView view = getExpandedView();
        ((RembrandtView) view).setImageFile(file, false).continueWith(new Continuation<Void, Object>() {
            @Nullable
            @Override
            public Object then(Task<Void> task) throws Exception {
                zoomFromView(thumb, view);
                return null;
            }
        });
    }
}
