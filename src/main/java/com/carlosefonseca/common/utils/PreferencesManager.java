package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@SuppressWarnings({"UnusedDeclaration", "UtilityClassWithoutPrivateConstructor"})
public class PreferencesManager {

    protected PreferencesManager() {}

    public static SharedPreferences getSharedPreferences() {
        return getSharedPreferences(null);
    }

    public static SharedPreferences getSharedPreferences(@Nullable Context context) {
        if (context == null) {
            context = CFApp.getContext();
        }
        return context.getSharedPreferences(context.getPackageName(), 0);
    }

    //region SETTERS
    public static void setParameter(@Nullable Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static void setParameter(@Nullable Context context, String key, boolean value) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static void setParameter(@Nullable Context context, String key, int value) {
        getSharedPreferences(context).edit().putInt(key, value).apply();
    }

    public static void setParameter(@Nullable Context context, String key, long value) {
        getSharedPreferences(context).edit().putLong(key, value).apply();
    }

    public static void setParameter(@Nullable Context context, String key, float value) {
        getSharedPreferences(context).edit().putFloat(key, value).apply();
    }

    public static void setParameter(@Nullable Context context, String key, Date date) {
        getSharedPreferences(context).edit().putLong(key, date.getTime()).apply();
    }
    //endregion

    //region GETTERS
    public static String getParameter(@Nullable Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    public static float getParameter(@Nullable Context context, String key, float defValue) {
        return getSharedPreferences(context).getFloat(key, defValue);
    }

    public static long getParameter(@Nullable Context context, String key, long defValue) {
        return getSharedPreferences(context).getLong(key, defValue);
    }

    public static int getParameter(@Nullable Context context, String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }

    public static boolean getParameter(@Nullable Context context, String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    public static Date getParameter(@Nullable Context context, String key, Date defValue) {
        return new Date(getSharedPreferences(context).getLong(key, defValue.getTime()));
    }
    //endregion
}
