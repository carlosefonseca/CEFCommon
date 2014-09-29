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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CFLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,
                                          GooglePlayServicesClient.OnConnectionFailedListener,
                                          LocationListener,
                                          LocationSource {
    private static final String TAG = CodeUtils.getTag(CFLocationManager.class);

    private LocationRequest mRequest;
    private int frequencyMillis = 5000;

    protected OnLocationChangedListener mapLocationChangedListener;
    protected Handler handler;
    private boolean connectionRequested;
    private Set<OnLocationChangedListener> listeners = new HashSet<>();
    private Location location;
    protected LocationClient mLocationClient;
    private boolean mShouldBeLocating;

    private static void toast(String text) {
        if (CFApp.isTestDevice()) Toast.makeText(CFApp.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    /* GOOGLE */

    /**
     * when it connects, calls {@link #onConnected(android.os.Bundle)}
     *
     * @return True if LocationClient is ready, false if connecting.
     */
    boolean setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(CFApp.getContext(), this, this);
        }

        if (mLocationClient.isConnected()) return true;

        if (!mLocationClient.isConnecting()) {
            connectionRequested = true;
            mLocationClient.connect();
        }
        return false;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        connectionRequested = false;
        if (mShouldBeLocating) startLocationUpdates();
        toast("onConnected");
    }

    private void startLocationUpdates() {
        mLocationClient.requestLocationUpdates(getLocationRequest(), this);
    }

    private void stopLocationUpdates() {
        if (mLocationClient != null && mLocationClient.isConnected()) mLocationClient.removeLocationUpdates(this);
    }


    @Override
    public void onDisconnected() {
        connectionRequested = false;
        toast("onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionRequested = false;
        Toast.makeText(CFApp.getContext(), "Error on Location Services!", Toast.LENGTH_SHORT).show();

    }

    public void clear() { }

    public boolean hasStarted() {
        return (mLocationClient != null &&
                (mLocationClient.isConnected() || mLocationClient.isConnecting() || connectionRequested));
    }

    /* (MAP) LOCATION SOURCE */

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mapLocationChangedListener = onLocationChangedListener;
        start();
        if (location != null) onLocationChangedListener.onLocationChanged(location);
    }

    @Override
    public void deactivate() {
        mapLocationChangedListener = null;
    }

    /* LISTENERS */

    public void addListener(OnLocationChangedListener listener) {
        listeners.add(listener);
        Log.d(TAG, "Listeners++: " + listeners.size());
    }

    public void removeListener(OnLocationChangedListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Listeners--: " + listeners.size());
        if (listeners.isEmpty()) stop();
    }

    /**
     * Optional.
     */
    public void prepare() {
        setUpLocationClientIfNeeded();
    }

    private LocationRequest getLocationRequest() {
        if (mRequest == null) {
            mRequest = LocationRequest.create()
                                      .setInterval(frequencyMillis)
                                      .setFastestInterval(frequencyMillis / 2)
                                      .setSmallestDisplacement(5)
                                      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mRequest;
    }


    /* API */

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (mapLocationChangedListener != null) mapLocationChangedListener.onLocationChanged(location);
        for (OnLocationChangedListener listener : listeners) {
            listener.onLocationChanged(location);
        }
    }

    public void start() {
        if (mShouldBeLocating) return;
        mShouldBeLocating = true;
        if (setUpLocationClientIfNeeded()) {
            startLocationUpdates();
        }
    }

    public void stop() {
        mShouldBeLocating = false;
        connectionRequested = false;
        stopLocationUpdates();
    }

    @Nullable
    public Location getLastLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            @Nullable Location googleLoc = mLocationClient.getLastLocation();
            return location == null || (googleLoc != null && googleLoc.getTime() > location.getTime())
                   ? googleLoc
                   : location;
        }
        return location;
    }

    public int getFrequencyMillis() {
        return frequencyMillis;
    }

    public void setFrequencyMillis(int frequencyMillis) {
        this.frequencyMillis = frequencyMillis;
        mRequest = null;
        if (mShouldBeLocating) {
            startLocationUpdates();
        }
    }

    public void close() {
        stop();
        if (mLocationClient != null) {
            mLocationClient.unregisterConnectionCallbacks(this);
            mLocationClient.unregisterConnectionFailedListener(this);
            mLocationClient.disconnect();
            mLocationClient = null;
        }
        listeners = null;
        mapLocationChangedListener = null;
    }
}
