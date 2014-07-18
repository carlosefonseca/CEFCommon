package com.carlosefonseca.common.utils;

import android.location.Location;
import android.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

public class CFLocation extends Location implements Comparable<CFLocation> {
    public static final String LINE_COORDINATE_PART_SPLITTER = " ";
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");
    public final float distance;
    public final int index;

    /**
     * Create an RCLocation from a KML coordinate.
     *
     * @param coordinateString The coordinate as a string in the format -9.123,38.123,0 (long,lat,…)
     */
    public CFLocation(@NotNull String coordinateString) {
        this(coordinateString, null, -1);
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
            distance = -1;
        }
        this.index = index;
    }

    public CFLocation(Location l, float distance) {
        super(l);
        this.distance = distance;
        index = -1;
    }

    public CFLocation(@NotNull Double lat, @NotNull Double lng) {
        this(lat, lng, -1);
    }

    public CFLocation(double lat, double lng, Location loc) {
        this(lat, lng, loc, -1);
    }

    public CFLocation(@NotNull Double lat, @NotNull Double lng, float distance) {
        super("");
        setLatitude(lat);
        setLongitude(lng);
        this.distance = distance;
        index = -1;
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
        this.distance = locationToDistance != null ? distanceTo(locationToDistance) : -1;
        this.index = index;
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
                             (distance > -1 ? "  D " + DECIMAL_FORMAT.format(distance) : ""));
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

}
