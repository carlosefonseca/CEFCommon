package com.carlosefonseca.common.logging;

import android.content.Context;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            Class<?> kitArray = Class.forName("[Lio.fabric.sdk.android.Kit;");
            Object[] array = (Object[]) Array.newInstance(Class.forName("io.fabric.sdk.android.Kit"), 1);
            array[0] = newCrashlyticsInstance;
            Method withMethod = fabric.getDeclaredMethod("with", Context.class, kitArray);
            withMethod.invoke(null, context, array);
        } catch (ClassNotFoundException e) {
            android.util.Log.w(TAG, "You should call CrashlyticsExists() first!");
            android.util.Log.e(TAG, "" + e.getMessage(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            android.util.Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    private static Class<?> getCrashlyticsClass() throws ClassNotFoundException {
        if (mCrashlyticsClass == null) mCrashlyticsClass = Class.forName("com.crashlytics.android.Crashlytics");
        return mCrashlyticsClass;
    }

    private static Class<?> getFabricClass() throws ClassNotFoundException {
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

    static final class CrashlyticsReflex {

        private static Method mSetInt;
        private static Method mSetDouble;
        private static Method mSetBool;
        private static Method mSetString;
        private static Method mSetUserEmail;
        private static Method mLog;
        private static Method mLogException;

        private CrashlyticsReflex() {}

        static void setUserEmail(String email) {
            if (mSetUserEmail == null) mSetUserEmail = getMethod("setUserEmail", String.class);
            callMethod(mSetUserEmail, email);
        }

        static void setString(String key, String value) {
            if (mSetString == null) mSetString = getMethod("setString", String.class, String.class);
            callMethod(mSetString, key, value);
        }

        static void setBool(String key, boolean value) {
            if (mSetBool == null) mSetBool = getMethod("setBool", String.class, Boolean.TYPE);
            callMethod(mSetBool, key, value);
        }

        static void setDouble(String key, double value) {
            if (mSetDouble == null) mSetDouble = getMethod("setDouble", String.class, Double.TYPE);
            callMethod(mSetDouble, key, value);
        }

        static void setInt(String key, int value) {
            if (mSetInt == null) mSetInt = getMethod("setInt", String.class, Integer.TYPE);
            callMethod(mSetInt, key, value);
        }

        static void log(String message) {
            if (mLog == null) mLog = getMethod("log", String.class);
            callMethod(mLog, message);
        }

        static void logException(Throwable throwable) {
            if (mLogException == null) mLogException = getMethod("logException", Throwable.class);
            callMethod(mLogException, throwable);
        }

        private static void callMethod(Method method, Object... args) {
            try {
                method.invoke(null, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                android.util.Log.e(TAG, "" + e.getMessage(), e);
            }
        }

        private static Method getMethod(String methodName, Class... classes) {
            Method method;
            try {
                method = mCrashlyticsClass.getDeclaredMethod(methodName, classes);
                return method;
            } catch (NoSuchMethodException e) {
                android.util.Log.e(TAG, "" + e.getMessage(), e);
            }
            return null;
        }
    }
}