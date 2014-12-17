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

    /**
     * Returns the name of the language, as it is in its own language. (Português, English,…)
     * @param languageCode "pt", "en",…
     */
    public static String languageOwnName(String languageCode) {
        Locale l = getLocale(languageCode);
        return l.getDisplayName(l);
    }

    protected static Locale getLocale(String languageCode) {
        if (languageCode.length() == 5 && languageCode.charAt(2) == '-') {
            return new Locale(languageCode.substring(0, 2), languageCode.substring(3));
        } else {
            return new Locale(languageCode);
        }
    }
}
