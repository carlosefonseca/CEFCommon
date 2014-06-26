package com.carlosefonseca.common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.NetworkingUtils;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.CodeUtils.runOnBackground;

/**
 * Base Class for the Application class.
 * Does the context management and sets up the test device.
 * Utils also access this class for its context.
 */

public class CFApp extends Application {
    private static final String TAG = getTag(CFApp.class);

    protected static String TEST_DEVICE_WIFI_MAC_ADDRESS = null;

    public static Context context;
    public static boolean test;
    private static final boolean ALLOW_TEST_DEVICE = true;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        setTestDevice(isTest());

        runOnBackground(new Runnable() {
            @Override
            public void run() {
                availableMemory();
            }
        });

        init();

        checkAppVersion();
    }

    private void checkAppVersion() {
        final int stored = getSharedPreferences(getPackageName(), MODE_PRIVATE).getInt("VERSION", -1);
        final int current = CodeUtils.getAppVersionCode();
        if (stored != current) {
            Log.put("App Update", "" + stored + " -> " + current);
            appUpdated(stored, current);
        }
    }


    protected void init() { }

    protected void appUpdated(int stored, int current) { }

    public static boolean testIfTestDevice() {
        return Build.FINGERPRINT.startsWith("generic") // Emulador
               || (TEST_DEVICE_WIFI_MAC_ADDRESS != null &&
                   TEST_DEVICE_WIFI_MAC_ADDRESS.contains(NetworkingUtils.getWifiMacAddress()));
    }

    public static Context getContext() {
        if (context == null) Log.w(TAG, "Please setup a CFApp subclass!");
        return context;
    }

    protected boolean isTest() {
        return test;
    }

    public static boolean isTestDevice() {
        return test;
    }

    public static void setTestDevice(boolean testDevice) {
        CFApp.test = testDevice;
        Log.setConsoleLogging(testDevice);
        if (testDevice)
            Log.w(TAG, "TEST DEVICE!");
    }

    /**
     * @return {@link android.app.ActivityManager.MemoryInfo#availMem}
     * @see android.app.ActivityManager#getMemoryInfo(android.app.ActivityManager.MemoryInfo)
     */
    public static long availableMemory() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        Log.i(TAG, "RAM \"Available\": " + availableMegs + " MB");
        return mi.availMem;
    }

    @Override
    public void onTerminate() {
        context = null;
        super.onTerminate();
    }

    public static SharedPreferences getUserPreferences() {return getUserPreferences("default");}

    public static SharedPreferences getUserPreferences(String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
