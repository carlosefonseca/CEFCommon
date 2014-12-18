package com.carlosefonseca.common.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
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

    public static Locale getLocale(String languageCode) {
        if (languageCode.length() == 5 && (languageCode.charAt(2) == '-' || languageCode.charAt(2) == '_')) {
            return new Locale(languageCode.substring(0, 2), languageCode.substring(3));
        } else {
            return new Locale(languageCode);
        }
    }

    public static class Lang implements Comparable<Lang> {

        private Locale locale;
        private boolean showRegion;
        private String stringCache;
        private String languageCode;
        private String code;

        public Lang(String code) {
            this.code = code;
            locale = getLocale(code);
            languageCode = locale.getLanguage();
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            if (stringCache == null) {
                stringCache = showRegion ? locale.getDisplayName(locale) : locale.getDisplayLanguage(locale);
            }
            return stringCache;
        }


        public boolean showRegion() {
            return showRegion;
        }

        public void setShowRegion(boolean showRegion) {
            if (this.showRegion != showRegion) {
                stringCache = null;
                this.showRegion = showRegion;
            }
        }

        @Override
        public int compareTo(@NotNull Lang another) {
            return toString().compareTo(another.toString());
        }

        public static void setShowRegions(List<Lang> langList) {
            HashMap<String, Lang> map = new HashMap<>();
            for (Lang lang : langList) {
                Lang seen = map.get(lang.languageCode);
                if (seen == null) {
                    map.put(lang.languageCode, lang);
                } else {
                    seen.setShowRegion(true);
                    lang.setShowRegion(true);
                }
            }
        }
    }
}
