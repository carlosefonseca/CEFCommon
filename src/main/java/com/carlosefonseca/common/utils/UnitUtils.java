package com.carlosefonseca.common.utils;

import com.carlosefonseca.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class UnitUtils {

    public static final String SYSTEM = "unit_system";
    private static final java.lang.String TAG = CodeUtils.getTag(UnitUtils.class);
    private static System mSystem = System.METRIC;
    private static NumberFormat numberFormatter = new DecimalFormat("@#");

    @Nullable static Localizer localizer;

    private UnitUtils() {}

    public enum System {
        METRIC, IMPERIAL;

        @Override
        public String toString() {
            return tf(super.toString());
        }
    }


    public static void setSystem(System system) {
        PreferencesManager.setParameter(null, SYSTEM, system.toString());
        mSystem = system;
    }

    @Nullable
    public static System getSystem() {
        @Nullable final String system = PreferencesManager.getParameter(null, SYSTEM, (String) null);
        if (system == null) {
            Log.i(TAG, "No System Defined!");
            return null;
        }
        try {
            mSystem = System.valueOf(system);
            return mSystem;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "" + e.getMessage(), e);
        }
        return null;
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
        switch (mSystem) {

            case METRIC:
                double kilometerDistance = distanceToKilometers(meters);

                if (kilometerDistance >= 1) {
                    distanceString = numberFormatter.format(kilometerDistance);
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
                    distanceString = numberFormatter.format(milesDistance);
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
