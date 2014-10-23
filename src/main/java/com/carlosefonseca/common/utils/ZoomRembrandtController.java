package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.widgets.RembrandtView;

public class ZoomRembrandtController extends ZoomImageController {


    public ZoomRembrandtController(ViewGroup container) {
        super(container);
    }

    public ZoomRembrandtController(Activity activity) {
        super(activity);
    }

    @Override
    protected ImageView createExpandedView() {
        final RembrandtView rembrandtView = new RembrandtView(getContext());
        return rembrandtView;
    }

    public void zoomFromView(final ImageView thumb, String url) {
        final ImageView view = getExpandedView();
        ((RembrandtView) view).setImageUrl(url, false).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                zoomFromView(thumb, view);
                return null;
            }
        });
    }
}
