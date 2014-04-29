package com.carlosefonseca.common.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.carlosefonseca.common.CFApp;

import java.util.Locale;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;

public final class LocalizationUtils {
    public static String TAG = getTag(LocalizationUtils.class);

    private LocalizationUtils() {}

    public static String getLanguageName(String code) {
        return new Locale(code).getDisplayLanguage();
    }

    public static void setSystemLanguage(String languageName) {
        Locale locale = new Locale(languageName);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = CFApp.getContext().getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    /**
     * @return stuff like "pt", not "pt_PT"
     */
    public static String getSystemLanguage() {
        Configuration cf = CFApp.getContext().getResources().getConfiguration();
        return cf.locale.getLanguage();
    }
}
