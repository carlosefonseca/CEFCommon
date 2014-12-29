package com.carlosefonseca.common.utils;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import bolts.Task;
import com.carlosefonseca.common.CFApp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CFLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,
                                          GooglePlayServicesClient.OnConnectionFailedListener,
                                          LocationListener,
                                          LocationSource,
                                          GoogleApiClient.ConnectionCallbacks,
                                          GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = CodeUtils.getTag(CFLocationManager.class);

    private LocationRequest mRequest;
    private int frequencyMillis = 5000;

    protected OnLocationChangedListener mapLocationChangedListener;
    protected Handler handler;
    private boolean connectionRequested;
    private Set<OnLocationChangedListener> listeners = new HashSet<>();
    @Nullable private Set<OnLocationChangedListener> oneTimeListeners = new HashSet<>();
    private Location location;
    private boolean mShouldBeLocating;
    @Nullable protected GoogleApiClient mGoogleApiClient;

    private static void toastLog(String text) {
        if (CFApp.isTestDevice()) Toast.makeText(CFApp.getContext(), text, Toast.LENGTH_SHORT).show();
        Log.d(TAG, text);
    }

    /* GOOGLE */

    /**
     * when it connects, calls {@link #onConnected(android.os.Bundle)}
     *
     * @return True if LocationClient is ready, false if connecting.
     */
    boolean setUpLocationClientIfNeeded() {
        return getGAC().isConnected();
    }

    private GoogleApiClient getGAC() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(CFApp.getContext()).addApi(LocationServices.API)
                                                                              .addConnectionCallbacks(this)
                                                                              .addOnConnectionFailedListener(this)
                                                                              .build();
        }
        if (!mGoogleApiClient.isConnected()) {
            connectionRequested = true;
            mGoogleApiClient.connect();
        }
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        connectionRequested = false;
        if (mShouldBeLocating) startLocationUpdates();
        toastLog("GooglePlay: Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        toastLog("GooglePlay: Connection Suspended");
    }

    private void startLocationUpdates() {
        if (getGAC().isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), this);
        } else {
            mShouldBeLocating = true;
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient == null) return;
        if (getGAC().isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } else {
            mShouldBeLocating = false;
        }
    }


    @Override
    public void onDisconnected() {
        connectionRequested = false;
        toastLog("GooglePlay: Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionRequested = false;
        toastLog("GooglePlay: ConnectionFailed!");
    }

    public void clear() { }

    public boolean hasStarted() {
        return mGoogleApiClient != null && ((mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting() ||
                                             connectionRequested));
    }

    /* (MAP) LOCATION SOURCE */

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mapLocationChangedListener = onLocationChangedListener;
        start();
        if (location != null) onLocationChangedListener.onLocationChanged(location);
    }

    /**
     * If there's a map use {@link #deactivate(com.google.android.gms.maps.LocationSource.OnLocationChangedListener)}
     */
    @Override
    public void deactivate() {
    }

    public void deactivate(@Nullable OnLocationChangedListener listener) {
        if (listener == null || (listener.equals(mapLocationChangedListener))) mapLocationChangedListener = null;
    }

    /* LISTENERS */

    public void addListener(OnLocationChangedListener listener) {
        listeners.add(listener);
        Log.d(TAG, "Listeners++: " + listeners.size());
    }

    /**
     * Adds a listener that will only receive one location.
     */
    public void addOneTimeListener(OnLocationChangedListener listener) {
        if (oneTimeListeners == null) {
            oneTimeListeners = new HashSet<>();
        }
        oneTimeListeners.add(listener);
        Log.d(TAG, "OneTimeListenerListeners++: " + oneTimeListeners.size());
    }

    public void removeListener(OnLocationChangedListener listener) {
        if (listeners != null && listener != null) {
            listeners.remove(listener);
            Log.d(TAG, "Listeners--: " + listeners.size());
        }
        if (listeners == null || listeners.isEmpty()) stop();
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
        if (oneTimeListeners != null) {
            for (Iterator<OnLocationChangedListener> iterator = oneTimeListeners.iterator(); iterator.hasNext(); ) {
                iterator.next().onLocationChanged(location);
                iterator.remove();
            }
            oneTimeListeners = null;
        }
    }

    public void start() {
        if (mShouldBeLocating) return;
        mShouldBeLocating = true;
        startLocationUpdates();
    }

    public void stop() {
        mShouldBeLocating = false;
        connectionRequested = false;
        stopLocationUpdates();
    }

    @Nullable
    public Location getLastLocation() {
        if (getGAC().isConnected()) {
            @Nullable Location googleLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return location == null || (googleLoc != null && googleLoc.getTime() > location.getTime())
                   ? googleLoc
                   : location;
        }
        return location;
    }

    public Task<Location> getLastLocationTask() {
        Location location1 = null;
        if (getGAC().isConnected()) {
            @Nullable Location googleLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            location1 = location == null || (googleLoc != null && googleLoc.getTime() > location.getTime())
                        ? googleLoc
                        : location;
        }

        if (location1 != null) return Task.forResult(location1);

        if (location != null) return Task.forResult(location);

        final Task<Location>.TaskCompletionSource taskCompletionSource = Task.create();

        addOneTimeListener(new OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                taskCompletionSource.setResult(location);
            }
        });

        return taskCompletionSource.getTask();
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
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            mGoogleApiClient.disconnect();
        }
        oneTimeListeners = null;
        listeners = null;
        mapLocationChangedListener = null;
    }
}
