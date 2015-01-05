package com.carlosefonseca.common.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CFLocation extends Location implements Comparable<CFLocation> {
    public static final String LINE_COORDINATE_PART_SPLITTER = " ";
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");
    public static final float NO_DISTANCE = Float.NaN;
    public static final int NO_INDEX = -1;
    private static final java.lang.String TAG = CodeUtils.getTag(CFLocation.class);
    public final float distance;
    public final int index;

    /**
     * Create an RCLocation from a KML coordinate.
     *
     * @param coordinateString The coordinate as a string in the format -9.123,38.123,0 (long,lat,…)
     */
    public CFLocation(@NotNull String coordinateString) {
        this(coordinateString, null, NO_INDEX);
    }

    /**
     * Create an RCLocation from a KML coordinate and sets the distance to a location.
     *
     * @param coordinateString   The coordinate as a string in the format -9.123,38.123,0 (long,lat,…).
     * @param locationToDistance The location to calculated a distance from.
     * @param index              An index.
     */
    public CFLocation(@NotNull String coordinateString, @Nullable CFLocation locationToDistance, int index) {
        super("");
        String[] split1 = coordinateString.split(LINE_COORDINATE_PART_SPLITTER);
        double latitude = Double.parseDouble(split1[1]);
        double longitude = Double.parseDouble(split1[0]);
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            throw new InvalidParameterException("Coordinates " + coordinateString + " aren't valid.");
        }
        setLatitude(latitude);
        setLongitude(longitude);
        if (locationToDistance != null) {
            distance = distanceTo(locationToDistance);
        } else {
            distance = NO_DISTANCE;
        }
        this.index = index;
    }

    public CFLocation(Location l, float distance) {
        super(l);
        this.distance = distance;
        index = NO_INDEX;
    }

    public CFLocation(@NotNull Double lat, @NotNull Double lng) {
        this(lat, lng, NO_DISTANCE);
    }

    public CFLocation(double lat, double lng, Location loc) {
        this(lat, lng, loc, NO_INDEX);
    }

    public CFLocation(@NotNull Double lat, @NotNull Double lng, float distance) {
        super("");
        setLatitude(lat);
        setLongitude(lng);
        this.distance = distance;
        index = NO_INDEX;
    }

    public CFLocation(@NotNull Location location, float distance, int index) {
        super(location);
        this.distance = distance;
        this.index = index;
    }

    public CFLocation(double lat, double lng, @Nullable Location locationToDistance, int index) {
        super("");
        setLatitude(lat);
        setLongitude(lng);
        this.distance = locationToDistance != null ? distanceTo(locationToDistance) : NO_DISTANCE;
        this.index = index;
    }

    /**
     * Creates a new Location similar to the first argument but calculates and stores the distance from the first to
     * the second argument.
     * @param location The location to copy.
     * @param distanceTo The second point to calculate the distance to.
     */
    public CFLocation(Location location, Location distanceTo) {
        super(location);
        this.distance = location.distanceTo(distanceTo);
        this.index = NO_INDEX;
    }

    @Override
    public int compareTo(@NotNull CFLocation other) {
        return (int) (this.distance - other.distance);
    }

    @Override
    public String toString() {
        return String.format("%s,%s%s",
                             DECIMAL_FORMAT.format(getLatitude()),
                             DECIMAL_FORMAT.format(getLongitude()),
                             (distance != NO_DISTANCE ? "  D " + DECIMAL_FORMAT.format(distance) : ""));
    }

    public static Location getCenter(Collection<Location> locations) {
        double maxLat = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;

        for (Location location : locations) {
            maxLat = Math.max(maxLat, location.getLatitude());
            minLat = Math.min(minLat, location.getLatitude());
            maxLng = Math.max(maxLng, location.getLongitude());
            minLng = Math.min(minLng, location.getLongitude());
        }

//        final CFLocation topRight = new CFLocation(maxLat, maxLng);
//        final CFLocation bottomLeft = new CFLocation(minLat, minLng);

//        return new CFLocation((topRight.getLatitude() + bottomLeft.getLatitude()) / 2,
//                              (topRight.getLongitude() + bottomLeft.getLongitude()) / 2);

        return new CFLocation((maxLat + minLat) / 2, (maxLng + minLng) / 2);
    }

    /**
     * @return (TopRight, BottomLeft)
     */
    public static Pair<Location, Location> getBounds(List<Location> locations) {
        double maxLat = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;

        for (Location location : locations) {
            maxLat = Math.max(maxLat, location.getLatitude());
            minLat = Math.min(minLat, location.getLatitude());
            maxLng = Math.max(maxLng, location.getLongitude());
            minLng = Math.min(minLng, location.getLongitude());
        }
        final CFLocation topRight = new CFLocation(maxLat, maxLng);
        final CFLocation bottomLeft = new CFLocation(minLat, minLng);
        return new Pair<Location, Location>(topRight, bottomLeft);
    }

    @Nullable
    public static String reverseGeocodeLocality(Context context, double lat, double lon, @NotNull Locale locale)
            throws IOException {
        try {
            //noinspection ConstantConditions
            if (locale == null) {
                Log.w(TAG, "LOCALE IS NULL!");
                locale = Locale.getDefault();
                if (locale == null) {
                    locale = Locale.ENGLISH;
                }
            }
            List<Address> fromLocation = new Geocoder(context, locale).getFromLocation(lat, lon, 1);
            Address a = fromLocation.get(0);
            String msg = a.getLocality() != null
                         ? a.getLocality()
                         : a.getSubAdminArea() != null
                           ? a.getSubAdminArea()
                           : a.getAdminArea() != null
                             ? a.getAdminArea()
                             : a.getMaxAddressLineIndex() > 0 ? a.getAddressLine(a.getMaxAddressLineIndex() - 1)
                                                                 .replaceFirst("[\\d-]+", "")
                                                                 .trim() : a.getCountryName();
            Log.d("reverseGeocodeLocality", locale.getDisplayLanguage() + ": " + msg + " - " + fromLocation.toString());
            return msg;
        } catch (IOException e) {
            Log.w(TAG, "" + e.getMessage(), e);
        }
        return null;
    }
}
