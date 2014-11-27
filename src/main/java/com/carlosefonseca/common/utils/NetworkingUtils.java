package com.carlosefonseca.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import de.greenrobot.event.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.carlosefonseca.common.CFApp.getContext;
import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.CodeUtils.isMainThread;

/**
 * Util methods for Network related stuff.
 */
public final class NetworkingUtils {

    private static final String TAG = getTag(NetworkingUtils.class);
    public static boolean NETWORK;
    public static boolean WIFI;

    private static Subscription<WifiListener, WifiStatus> wifiSubscription;
    private static Subscription<InternetListener, NetStatus> internetSubscription;

    private static ConnectivityManager conMan;

    private NetworkingUtils() {}

    public interface WifiListener {
        public void onStatusChanged(WifiStatus status);
    }

    public interface InternetListener {
        public void onStatusChanged(NetStatus status);
    }

    public static Subscription<InternetListener, NetStatus> getInternetSubscription() {
        if (internetSubscription == null) {
            internetSubscription = new Subscription<>(new Subscription.SubscriberDelegate<InternetListener, NetStatus>() {
                @Override
                public void start() {
                    listenForNetworkChanges();
                }

                @Override
                public void stop() {
                    stopListeningForNetworkChanges();
                }

                @Override
                public void send(InternetListener subscriber, NetStatus message) {
                    subscriber.onStatusChanged(message);
                }
            });
        }
        return internetSubscription;
    }

    public static Subscription<WifiListener, WifiStatus> getWifiSubscription() {
        if (wifiSubscription == null) {
            wifiSubscription = new Subscription<>(new Subscription.SubscriberDelegate<WifiListener, WifiStatus>() {
                @Override
                public void start() {
                    listenForWifiChanges();
                }

                @Override
                public void stop() {
                    stopListeningForWifiChanges();
                }
                @Override
                public void send(WifiListener subscriber, WifiStatus message) {
                    subscriber.onStatusChanged(message);
                }
            });
        }
        return wifiSubscription;
    }

    /**
     * Checks Internet access.
     *
     * @return true if there's an active network.
     */
    public static boolean hasInternet() {
        if (conMan == null) {
            if (getContext().checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") ==
                PackageManager.PERMISSION_GRANTED) {
                conMan = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            } else {
                Log.w(TAG, "Permission android.permission.ACCESS_NETWORK_STATE is required!");
                return true;
            }
        }
        NetworkInfo activeNetworkInfo = conMan.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static class NotConnectedException extends IOException {
        public NotConnectedException() {
            super("You are not connected to the Internet.");
        }

        public NotConnectedException(String detailMessage) {
            super(detailMessage);
        }

        public NotConnectedException(String message, Throwable cause) {
            super(message, cause);
        }

        public NotConnectedException(Throwable cause) {
            super("You are not connected to the Internet.", cause);
        }
    }

    /**
     * Fetches the MAC address of the WiFi.
     *
     * @return The MAC address or 00:00:00:00:00:00 if no address got returned.
     */
    public static String getWifiMacAddress() {
        String macAddr = null;
        try {
            WifiManager wifiMan = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            macAddr = wifiInf.getMacAddress();
        } catch (SecurityException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }

        Log.v(TAG, "DEVICE MAC: " + macAddr);

        return macAddr == null ? "00:00:00:00:00:00" : macAddr;
    }

    /**
     * Checks if the device is connected to the internet via wifi.
     *
     * @return true if connected via wifi, false otherwise (including not connected or connected via cellular)
     */
    public static boolean isOnWifi() {
        ConnectivityManager conMan = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMan == null || conMan.getActiveNetworkInfo() == null) return false;

        NetworkInfo networkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State wifi = networkInfo != null ? networkInfo.getState() : null;

        return wifi != null && wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
    }


    /**
     * Registers with the system for changes in wifi connectivity and posts an EventBus notification ({@link
     * com.carlosefonseca.common.utils.NetworkingUtils.WifiStatus} CONNECTED) when it detects a that
     * a wifi connection to the internet has been established. It only notifies when {@link #isOnWifi()} returns true.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void listenForWifiChanges() {
        if (!WIFI) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            getContext().registerReceiver(wifiBroadcastReceiver, intentFilter);
            WIFI = true;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void stopListeningForWifiChanges() {
        if (WIFI) {
            try {
                getContext().unregisterReceiver(wifiBroadcastReceiver);
                WIFI = false;
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }


    static BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (nwInfo != null && NetworkInfo.State.CONNECTED == nwInfo.getState()) {
                    //This implies the WiFi connection is through
                    if (isOnWifi()) {
                        EventBus.getDefault().postSticky(WifiStatus.CONNECTED);
                        getWifiSubscription().send(WifiStatus.CONNECTED);
                    } else {
                        new Handler(context.getMainLooper()).postDelayed(NetworkingUtils.POST_HAS_WIFI, 5000);
                    }
                }
            }
        }
    };

    public enum WifiStatus {
        CONNECTED, DISCONNECTED
    }

    static final Runnable POST_HAS_WIFI = new Runnable() {
        @Override
        public void run() {
            if (isOnWifi()) {
                EventBus.getDefault().postSticky(WifiStatus.CONNECTED);
                getWifiSubscription().send(WifiStatus.CONNECTED);
            } else {
                EventBus.getDefault().postSticky(WifiStatus.DISCONNECTED);
                getWifiSubscription().send(WifiStatus.DISCONNECTED);
            }
        }
    };


    /**
     * Registers with the system for changes in network connectivity and posts an EventBus notification ({@link
     * com.carlosefonseca.common.utils.NetworkingUtils.NetStatus} CONNECTED) when it detects a that
     * a network connection to the internet has been established, either by wifi or cellular.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void listenForNetworkChanges() {
        if (!NETWORK) {
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            getContext().registerReceiver(networkBroadcastReceiver, intentFilter);
            NETWORK = true;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void stopListeningForNetworkChanges() {
        if (NETWORK) {
            try {
                getContext().unregisterReceiver(networkBroadcastReceiver);
                NETWORK = false;
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }

    static BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                if (hasInternet()) {
                    EventBus.getDefault().postSticky(NetStatus.CONNECTED);
                    getInternetSubscription().send(NetStatus.CONNECTED);
                } else {
                    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)) {
                        Log.i(TAG, "Failing over...");
                    } else {
                        EventBus.getDefault().postSticky(NetStatus.DISCONNECTED);
                        getInternetSubscription().send(NetStatus.DISCONNECTED);
                    }
                }
            }
        }
    };


    public enum NetStatus {
        CONNECTED, DISCONNECTED
    }


    static final Runnable POST_HAS_NET = new Runnable() {
        @Override
        public void run() {
            if (isOnWifi()) {
                EventBus.getDefault().postSticky(WifiStatus.CONNECTED);
            } else {
                EventBus.getDefault().postSticky(WifiStatus.DISCONNECTED);
            }
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public static String getLastSegmentOfURL(String url) {
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String fixURL(String urlStr) throws MalformedURLException, URISyntaxException {
//    String urlStr = "http://abc.dev.domain.com/0007AC/ads/800x480 15sec h.264.mp4";
        URL url = new URL(urlStr);
        URI uri = new URI(url.getProtocol(),
                          url.getUserInfo(),
                          url.getHost(),
                          url.getPort(),
                          url.getPath(),
                          url.getQuery(),
                          url.getRef());
        return uri.toURL().toString();
    }

    /**
     * Fetches a Bitmap from a URL. Don't call this from the Main Thread.
     *
     * @param url The image url.
     * @throws IOException
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Bitmap loadBitmap(String url) throws IOException {
        if (isMainThread()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                Log.w(TAG, new RuntimeException("Network On Main Thread"));
            } else {
                Log.w(TAG, new NetworkOnMainThreadException());
            }
        }
        URL url1 = new URL(url.replace(" ", "%20"));
        InputStream inputStream = url1.openConnection().getInputStream();
        BitmapFactory.Options opts = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            opts = new BitmapFactory.Options();
            opts.inMutable = true;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
        inputStream.close();
        return bitmap;
    }

    /**
     * Converts filenames or URLs to an existing file on disk.
     * <p/>
     * Accepted {@code filenameOrUrl} options:
     * <ul>
     * <li>http://example.com/image.png</li>
     * <li>/sdcard/somefolder/image.png</li>
     * <li>image.png</li>
     * </ul>
     *
     * @param filenameOrUrl File object pointing to
     */
    @Nullable
    public static File getFileFromUrlOrPath(@NotNull String filenameOrUrl) {
        File file;
        if (filenameOrUrl.startsWith("http://")) {
            file = ResourceUtils.getFullPath(getLastSegmentOfURL(filenameOrUrl));
        } else {
            file = filenameOrUrl.startsWith("/") ? new File(filenameOrUrl) : ResourceUtils.getFullPath(filenameOrUrl);
        }
        return file;
    }


}
