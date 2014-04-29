package com.carlosefonseca.common.utils;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.carlosefonseca.common.CFApp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.LocationSource;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class CFLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,
                                          GooglePlayServicesClient.OnConnectionFailedListener,
                                          LocationListener,
                                          LocationSource {
    // These settings are the same as the settings for the map. They will in fact give you updates at
    // the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create().setInterval(5000)         // 5 seconds
            .setFastestInterval(2000)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private static final String TAG = CodeUtils.getTag(CFLocationManager.class);
    protected OnLocationChangedListener mapLocationChangedListener;
    protected Handler handler;
    private boolean connectionRequested;
    private List<OnLocationChangedListener> listeners = new ArrayList<OnLocationChangedListener>();
    private Location location;
    protected LocationClient mLocationClient;

    private static void toast(String text) {
        if (CFApp.isTestDevice()) Toast.makeText(CFApp.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(CFApp.getContext(), this,  // ConnectionCallbacks
                                                 this); // OnConnectionFailedListener
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        connectionRequested = false;
        mLocationClient.requestLocationUpdates(REQUEST, this);  // LocationListener
        toast("onConnected");
    }

    @Override
    public void onDisconnected() {
        connectionRequested = false;
        toast("onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionRequested = false;
        toast("onConnectionFailed");

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (mapLocationChangedListener != null) mapLocationChangedListener.onLocationChanged(location);
        for (OnLocationChangedListener listener : listeners) {
            listener.onLocationChanged(location);
        }
//        EventBus.getDefault().postSticky(location);
    }

    public Location getLastLocation() {
        return location != null
               ? location
               : mLocationClient != null && mLocationClient.isConnected() ? mLocationClient.getLastLocation() : null;
    }

    public void start() {
        setUpLocationClientIfNeeded();
        if (!hasStarted()) {
            mLocationClient.connect();
            connectionRequested = true;
        }
    }

    public void stop() {
        connectionRequested = false;
        if (mLocationClient != null) mLocationClient.disconnect();
    }

    public void clear() {
    }

    public boolean hasStarted() {
        return (mLocationClient != null &&
                (mLocationClient.isConnected() || mLocationClient.isConnecting() || connectionRequested));
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mapLocationChangedListener = onLocationChangedListener;
        if (!hasStarted()) {
            start();
        }
        if (location != null) onLocationChangedListener.onLocationChanged(location);
    }

    @Override
    public void deactivate() {
        mapLocationChangedListener = null;
    }

    public void addListener(OnLocationChangedListener listener) {
        listeners.add(listener);
        Log.d(TAG, "Listeners++: " + listeners.size());
    }

    public void removeListener(OnLocationChangedListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Listeners--: " + listeners.size());
        if (listeners.isEmpty()) stop();
    }
}
