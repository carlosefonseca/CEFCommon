package com.carlosefonseca.common.utils;

import org.apache.commons.lang3.StringUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class UnitUtils {

    public static final String SYSTEM = "unit_system";
    private static final java.lang.String TAG = CodeUtils.getTag(UnitUtils.class);
    private static NumberFormat numberFormatter = new DecimalFormat("@#");

    @Nullable static Localizer localizer;

    private static System mSystem;
    private static System mDefaultSystem = System.METRIC;

    private UnitUtils() {}

    public static void setSystem(String system) {
        System system1 = null;
        try {
            system1 = System.valueOf(system);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
        if (system1 != null) {
            setSystem(system1);
        }
    }

    public static void setDefaultSystem(@Nullable String system) {
        if (system != null) {
            try {
                mDefaultSystem = System.valueOf(system.toUpperCase());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }

    public static void clearUserPreference() {
        PreferencesManager.getSharedPreferences().edit().remove(SYSTEM).apply();
        mSystem = null;
        mDefaultSystem = System.METRIC;
    }

    @NonNull
    public static System setNextSystem() {
        System system = UnitUtils.getSystem();
        double ordinal = 0;
        if (system != null) {
            ordinal = system.ordinal();
        }
        UnitUtils.setSystem(UnitUtils.System.values()[((int) ((ordinal + 1) % System.values().length))]);
        return mSystem;
    }

    public enum System {
        METRIC, IMPERIAL;

        @Override
        public String toString() {
            return tf(super.toString());
        }

        public String temperature() {
            switch (this) {
                case METRIC:
                    return "C";
                case IMPERIAL:
                    return "F";
            }
            return null;
        }
    }


    public static void setSystem(System system) {
        PreferencesManager.setParameter(null, SYSTEM, system.toString());
        if (mSystem != system) Log.i(TAG, "New Measurement System");
        mSystem = system;
    }

    public static System getSystem() {
        if (mSystem != null) return mSystem;

        final String system = PreferencesManager.getParameter(null, SYSTEM, (String) null);
        if (system == null) {
            Log.i(TAG, "No user defined System. Will use default: " + mDefaultSystem);
            mSystem = mDefaultSystem;
            return mSystem;
        }

        try {
            mSystem = System.valueOf(system);
            return mSystem;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "System parse failed. Falling back to default.", e);
        }
        return mDefaultSystem;
    }

    static final double kTTTMetersToKilometersCoefficient = 0.001;
    static final double kTTTMetersToFeetCoefficient = 3.2808399;
    static final double kTTTMetersToYardsCoefficient = 1.0936133;
    static final double kTTTMetersToMilesCoefficient = 0.000621371192;

    static double distanceToKilometers(int meters) {
        return meters * kTTTMetersToKilometersCoefficient;
    }

    static double distanceToFeet(int meters) {
        return meters * kTTTMetersToFeetCoefficient;
    }

    static double distanceToYards(int meters) {
        return meters * kTTTMetersToYardsCoefficient;
    }

    static double distanceToMiles(int meters) {
        return meters * kTTTMetersToMilesCoefficient;
    }

    public static String stringForDistance(int meters) {
        String distanceString = null;
        String unitString = null;
        final System system = getSystem();
        switch (system != null ? system : System.METRIC) {

            case METRIC:
                double kilometerDistance = distanceToKilometers(meters);

                if (kilometerDistance >= 1) {
                    distanceString = kilometerDistance >= 10
                                     ? "" + (int) kilometerDistance
                                     : numberFormatter.format(kilometerDistance);
                    unitString = tf("km");
                } else {
                    distanceString = numberFormatter.format(meters);
                    unitString = tf("m");
                }
                break;
            case IMPERIAL:
                double feetDistance = distanceToFeet(meters);
                double yardDistance = distanceToYards(meters);
                double milesDistance = distanceToMiles(meters);

                if (feetDistance < 300) {
                    distanceString = numberFormatter.format(feetDistance);
                    unitString = tf("ft");
                } else if (yardDistance < 500) {
                    distanceString = numberFormatter.format(yardDistance);
                    unitString = tf("yds");
                } else {
                    distanceString = milesDistance >= 10
                                     ? "" + (int) milesDistance
                                     : numberFormatter.format(milesDistance);
                    unitString = (milesDistance > 1.0 && milesDistance < 1.1) ? tf("mile") : tf("miles");
                }
                break;
        }
        return String.format("%s %s", distanceString, unitString);
    }

    private static String tf(String unit) {
        if (localizer == null) return unit;
        final String t = localizer.translate(unit);
        return StringUtils.defaultIfBlank(t, unit);
    }

    @Nullable
    public static Localizer getLocalizer() {
        return localizer;
    }

    public static void setLocalizer(@Nullable Localizer localizer) {
        UnitUtils.localizer = localizer;
    }

    public static interface Localizer {
        String translate(String unit);
    }
}
