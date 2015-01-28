package com.carlosefonseca.common.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Subclass of MapView that allows for a touch listener to be attached.
 * This is useful if you need to detect when touches occurred on the map and act on them,
 * e.g., stop moving the camera that's following the user to allow free motion.
 *
 */
@SuppressLint("ViewConstructor")
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
