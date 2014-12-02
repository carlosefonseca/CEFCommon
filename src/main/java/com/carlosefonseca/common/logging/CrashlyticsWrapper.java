package com.carlosefonseca.common.logging;

import android.content.Context;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CrashlyticsWrapper implements Log.RemoteLogger {
    private static final java.lang.String TAG = CodeUtils.getTag(CrashlyticsWrapper.class);
    private static Class<?> mCrashlyticsClass;

    public static boolean CrashlyticsExists() {
        try {
            return null != getCrashlyticsClass();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "" + e.getMessage(), e);
            return false;
        }
    }

    public CrashlyticsWrapper(Context context) {
        try {
            final Class<?> fabric = getFabricClass();
            final Class<?> crashlytics = getCrashlyticsClass();
            final Constructor<?> crashlyticsConstructor = crashlytics.getDeclaredConstructor((Class[]) null);
            Object newCrashlyticsInstance = crashlyticsConstructor.newInstance((Object[]) null);
            fabric.getDeclaredMethod("with").invoke(null, context, newCrashlyticsInstance);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "You should call CrashlyticsExists() first!");
            Log.e(TAG, "" + e.getMessage(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    private static Class<?> getCrashlyticsClass() throws ClassNotFoundException {
        if (mCrashlyticsClass == null) mCrashlyticsClass = Class.forName("com.crashlytics.android.Crashlytics");
        return mCrashlyticsClass;
    }

    private Class<?> getFabricClass() throws ClassNotFoundException {
        return Class.forName("io.fabric.sdk.android.Fabric");
    }

    @Override
    public void setUserEmail(String email) {
        CrashlyticsReflex.setUserEmail(email);
    }

    @Override
    public void put(String key, String value) {
        CrashlyticsReflex.setString(key, value);
    }

    @Override
    public void put(String key, boolean value) {
        CrashlyticsReflex.setBool(key, value);
    }

    @Override
    public void put(String key, double value) {
        CrashlyticsReflex.setDouble(key, value);
    }

    @Override
    public void put(String key, int value) {
        CrashlyticsReflex.setInt(key, value);
    }

    @Override
    public boolean log(int priority, String tag, String message, Throwable tr) {
        CrashlyticsReflex.log("" + priority + " " + tag + ": " + message);
        if (tr != null) {
            CrashlyticsReflex.logException(tr);
        }
        return false;
    }

    static class CrashlyticsReflex {

        static void setUserEmail(String email) {
            callMethod("setUserEmail", email);
        }

        static void setString(String key, String value) {
            callMethod("setString", key, value);
        }

        static void setBool(String key, Boolean value) {
            callMethod("setBool", key, value);
        }

        static void setDouble(String key, Double value) {
            callMethod("setDouble", key, value);
        }

        static void setInt(String key, Integer value) {
            callMethod("setInt", key, value);
        }

        static void log(String message) {
            callMethod("log", message);
        }

        static void logException(Throwable throwable) {
            callMethod("logException", throwable);
        }

        private static void callMethod(String methodName, Object... args) {
            try {
                mCrashlyticsClass.getDeclaredMethod(methodName).invoke(null, args);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }
}