package com.carlosefonseca.common.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public static Locale getSystemLocale() {
        Configuration cf = CFApp.getContext().getResources().getConfiguration();
        return cf.locale;
    }

    /**
     * Returns the name of the language, as it is in its own language. (Português, English,…)
     * @param languageCode "pt", "en",…
     */
    @Nullable
    public static String languageOwnName(@Nullable String languageCode) {
        return languageCode == null ? null : getLocale(languageCode).getDisplayName(getLocale(languageCode));
    }

    /**
     * Converts the code to a {@link java.util.Locale}.
     *
     * @param languageCode Possible formats: "pt" or "pt_pt" or "pt-pt". Case insensitive.
     * @return A Locale.
     */
    public static Locale getLocale(@NotNull String languageCode) {
        if (languageCode.length() == 5 && (languageCode.charAt(2) == '-' || languageCode.charAt(2) == '_')) {
            return new Locale(languageCode.substring(0, 2), languageCode.substring(3));
        } else {
            return new Locale(languageCode);
        }
    }

    /**
     * Iterates through the possible locales looking for one of the hints that matches fully (lang + country) or
     * partially (lang). Prefers the first hint that matches either way.
     *
     * @return A Locale or null if no match was found.
     */
    @Nullable
    public static Locale getBest(Collection<Locale> possibleLocales, Locale... hints) {
        for (Locale hint : hints) {
            Locale possibleLocale1 = null;
            for (Locale possibleLocale : possibleLocales) {
                if (possibleLocale.getLanguage().equals(hint.getLanguage())) {
                    if (hint.getCountry().equals(possibleLocale.getCountry())) {
                        return possibleLocale;
                    } else {
                        possibleLocale1 = possibleLocale;
                    }
                }
            }
            if (possibleLocale1 != null) return possibleLocale1;
        }
        return null;
    }


    public static class Lang implements Comparable<Lang> {

        public static final Comparator<Lang> CODE_COMPARATOR = new Comparator<Lang>() {
            @Override
            public int compare(Lang lhs, Lang rhs) {
                return lhs.getCode().compareToIgnoreCase(rhs.getCode());
            }
        };

        private Locale locale;
        private boolean showRegion;
        private String stringCache;
        private String languageCode;
        private String code;

        public Lang(String code) {
            this(LocalizationUtils.getLocale(code), code);
        }

        public Lang(Locale locale, @Nullable String code) {
            this.code = code != null ? code : locale.getLanguage();
            this.locale = locale;
            this.languageCode = locale.getLanguage();
        }

        public Locale getLocale() {
            return locale;
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
            return toString().compareToIgnoreCase(another.toString());
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

        @Nullable
        public static Lang getBest(Collection<Lang> possibleLangs, Lang... hints) {
            for (Lang hint : hints) {
                Lang possibleLang1 = null;
                for (Lang possibleLang : possibleLangs) {
                    if (hint.languageCode.equals(possibleLang.languageCode)) {
                        if (hint.code.equalsIgnoreCase(possibleLang.code)) {
                            return possibleLang;
                        } else {
                            possibleLang1 = possibleLang;
                        }
                    }
                }
                if (possibleLang1 != null) return possibleLang1;
            }
            return null;
        }

        @Nullable
        public static Lang getBest(Collection<Lang> possibleLangs, Locale... hints) {
            for (Locale hint : hints) {
                if (hint == null) continue;
                Lang possibleLang1 = null;
                for (Lang possibleLang : possibleLangs) {
                    Locale possibleLocale = possibleLang.locale;
                    if (hint.getLanguage().equals(possibleLocale.getLanguage())) {
                        if (hint.getCountry().equalsIgnoreCase(possibleLocale.getCountry())) {
                            return possibleLang;
                        } else {
                            possibleLang1 = possibleLang;
                        }
                    }
                }
                if (possibleLang1 != null) return possibleLang1;
            }
            return null;
        }
    }
}
