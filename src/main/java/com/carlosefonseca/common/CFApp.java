package com.carlosefonseca.common;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.ResourceUtils;

import java.io.File;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;

/**
 * Base Class for the Application class.
 * Does the context management and sets up the test device.
 * Utils also access this class for its context.
 */

public class CFApp extends Application {
    private static final String TAG = getTag(CFApp.class);
    public static final String VERSION_KEY = "VERSION";

    public static Context context;
    public static boolean test;
    private static final boolean ALLOW_TEST_DEVICE = true;
    private static Boolean isTestDevice = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        setTestDevice(isTest());

        final SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        final int stored = sharedPreferences.getInt(VERSION_KEY, -1);
        final int current = CodeUtils.getAppVersionCode();
        if (stored != current) {
            Log.put("App Update", "" + stored + " -> " + current);
            sharedPreferences.edit().putInt(VERSION_KEY, current).apply();
        } else {
            Log.i("App Version: " + current);
        }

        init(current, stored);
    }

    protected void init(int currentVersion, int previousVersion) {}

    public static boolean isLandscapeTablet() {
        return isLandscape() && isTablet();
    }


    public static boolean testIfTestDevice() {
        if (isTestDevice == null) isTestDevice = isEmulator() || checkForceLogFile();
        return isTestDevice;
    }

    private static boolean isEmulator() {return Build.FINGERPRINT.startsWith("generic");}

    private static boolean checkForceLogFile() {
        return new File(Environment.getExternalStorageDirectory(), "logbw.txt").exists();
    }

    public static Context getContext() {
        if (context == null) Log.w(TAG, "Please setup a CFApp subclass!");
        return context;
    }

    /**
     * You should override this and return com.yourapp.BuildConfig.DEBUG
     */
    protected boolean isTest() {
        return test;
    }

    public static boolean isTestDevice() {
        return test;
    }

    public static void setTestDevice(boolean testDevice) {
        CFApp.test = testDevice;
        Log.setConsoleLogging(testDevice || testIfTestDevice());
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

    public static SharedPreferences getUserPreferences() {return getUserPreferences("default");}

    public static SharedPreferences getUserPreferences(String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }




    /**
     * 320dp: a small phone screen (240x320 ldpi, 320x480 mdpi, 480x800 hdpi, etc).
     * 360dp: a typical phone screen
     * 480dp: a tweener tablet like the Streak (480x800 mdpi).
     * 600dp: a 7” tablet (600x1024 mdpi).
     * 720dp: a 10” tablet (720x1280 mdpi, 800x1280 mdpi, etc).
     */
    static FormFactor sFormFactor;

    enum FormFactor {
        SMALL_PHONE, PHONE, LARGE_PHONE, TABLET_7, TABLET_10
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static FormFactor getFormFactor() {
        if (sFormFactor != null) return sFormFactor;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            throw new UnsupportedOperationException("smallestScreenWidthDp requires SDK 13+");
        }
        float smallestWidth = getContext().getResources().getConfiguration().smallestScreenWidthDp;

        if (smallestWidth >= 720) sFormFactor = FormFactor.TABLET_10;
        else if (smallestWidth >= 600) sFormFactor = FormFactor.TABLET_7;
        else if (smallestWidth >= 480) sFormFactor = FormFactor.LARGE_PHONE;
        else if (smallestWidth >= 360) sFormFactor = FormFactor.PHONE;
        else if (smallestWidth >= 320) sFormFactor = FormFactor.SMALL_PHONE;
        return sFormFactor;
    }

    public static boolean isPhone() {
        switch (getFormFactor()) {
            case SMALL_PHONE:
            case PHONE:
            case LARGE_PHONE:
                return true;
        }
        return false;
    }

    public static boolean isTablet() {
        switch (getFormFactor()) {
            case TABLET_7:
            case TABLET_10:
                return true;
        }
        return false;
    }

    public static boolean isLandscape() {
        return ResourceUtils.isLandscape(context.getResources());
    }
}
