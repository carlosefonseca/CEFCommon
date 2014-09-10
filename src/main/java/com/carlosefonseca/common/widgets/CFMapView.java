package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.view.MotionEvent;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

public class CFMapView extends MapView {
    public CFMapView(Context context, GoogleMapOptions googleMapOptions) {super(context, googleMapOptions);}

    public interface MapTouchListener {
        void onMapTouched(MotionEvent ev);
    }

    MapTouchListener onMapTouchListener;

    public MapTouchListener getOnMapTouchListener() {
        return onMapTouchListener;
    }

    public void setOnMapTouchListener(MapTouchListener onMapTouchListener) {
        this.onMapTouchListener = onMapTouchListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (onMapTouchListener != null) onMapTouchListener.onMapTouched(ev);
        return false;
    }
}
