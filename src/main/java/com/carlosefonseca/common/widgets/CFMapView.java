package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.carlosefonseca.common.utils.ActivityStateListener.ActivityStateListenerProvider;
import com.carlosefonseca.common.utils.ActivityStateListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Subclass of MapView that allows for a touch listener to be attached.
 * This is useful if you need to detect when touches occurred on the map and act on them,
 * e.g., stop moving the camera that's following the user to allow free motion.
 *
 */
public class CFMapView extends MapView {

    private ActivityListener mActivityListener = new ActivityListener();

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

    public CFMapView(Context context, GoogleMapOptions googleMapOptions) {super(context, googleMapOptions);}

    public CFMapView(Context context) {
        super(context);
        if (context instanceof ActivityStateListenerProvider) {
            ((ActivityStateListenerProvider) context).getActivityStateListener().addListener(mActivityListener);
        }
    }

    public CFMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof ActivityStateListenerProvider) {
            ((ActivityStateListenerProvider) context).getActivityStateListener().addListener(mActivityListener);
        }
    }

    public CFMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (context instanceof ActivityStateListenerProvider) {
            ((ActivityStateListenerProvider) context).getActivityStateListener().addListener(mActivityListener);
        }
    }

    public class ActivityListener extends ActivityStateListener.SimpleInterface {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            CFMapView.this.onCreate(savedInstanceState);
        }

        @Override
        public void onPause() {
            CFMapView.this.onPause();
        }

        @Override
        public void onResume() {
            CFMapView.this.onResume();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            CFMapView.this.onSaveInstanceState(outState);
        }

        @Override
        public void onDestroy() {
            CFMapView.this.onDestroy();
            if (CFMapView.this.getContext() instanceof ActivityStateListenerProvider) {
                ((ActivityStateListenerProvider) CFMapView.this.getContext()).getActivityStateListener()
                                                                             .addListener(mActivityListener);
            }
        }
    }
}
