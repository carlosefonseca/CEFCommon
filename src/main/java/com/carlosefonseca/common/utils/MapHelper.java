package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.ImageUtils.dp2px;


public class MapHelper {

    private static final String TAG = getTag(MapHelper.class);

    protected int userZoom = 18;
    protected final View parentView;
    @Nullable protected final GoogleMap gMap;
    protected Marker myLocationMarker;

    int color = Color.BLACK;

    protected LatLngBounds.Builder latLngBoundsBuilder;
    protected LatLngBounds latLngBounds;
    boolean layoutOccurred;
    protected CameraUpdate cameraUpdateWithAllPoints;
    private View followUserButton;
    private boolean followingUser;
    private Handler handler;
    @Nullable protected LatLng userLocation;
    public boolean waitingToFocusOnAll;
    private Drawable parentBackground;
    private boolean followingUserAndPoint;
    private LatLng followPoint;


    @SuppressWarnings("NullableProblems")
    public MapHelper(View view, GoogleMap gMap) {
        parentView = view;
        this.gMap = gMap;
    }

    protected Context getContext() {
        return parentView.getContext();
    }

    protected Bitmap createMarker(int number, int marker) {

        Bitmap b = BitmapFactory.decodeResource(parentView.getResources(), marker);

        Bitmap bitmap = Bitmap.createBitmap(b.copy(Bitmap.Config.ARGB_8888, true));

        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(color);
        p.setAntiAlias(true);
        p.setTextSize(number > 99 ? 15 : 20);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT_BOLD);

        Rect bounds = new Rect();
        String ntxt = "" + number;
        p.getTextBounds(ntxt, 0, ntxt.length(), bounds);
        canvas.drawText(ntxt, (float) (b.getWidth() * 0.4), ((b.getHeight() + bounds.height()) / 2) - 3, p);

        BitmapDrawable bd = new BitmapDrawable(parentView.getResources(), bitmap);

        int space = bd.getIntrinsicHeight() * 6 / 7;
        bd.setBounds(-bd.getIntrinsicWidth() / 2, -space, bd.getIntrinsicWidth() / 2, bd.getIntrinsicHeight() - space);

        return bitmap;
    }

    public void initialState() {
        try {
            CodeUtils.runOnGlobalLayout(parentView, new CodeUtils.RunnableWithView<View>() {
                @Override
                public void run(View view) {
                    if (parentView.getWidth() == 0) return;
                    layoutOccurred = true;
                    CameraUpdate cameraUpdateWithAllPoints1 = getCameraUpdateWithAllPoints();
                    if (gMap != null && cameraUpdateWithAllPoints1 != null) {
                        Log.i(TAG, "Animating Camera");
                        gMap.animateCamera(cameraUpdateWithAllPoints1);
                        parentBackground = parentView.getBackground();
                        ResourceUtils.setBackground(parentView, null);
                    }
                }
            });
            parentView.requestLayout();
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Nullable
    public CameraUpdate getCameraUpdateWithAllPoints() {
        return getCameraUpdateWithAllPoints(false);
    }

    @Nullable
    public CameraUpdate getCameraUpdateWithAllPoints(boolean force) {
        if (layoutOccurred && (cameraUpdateWithAllPoints == null || force)) {
            if (force || latLngBounds == null) {
                try {
                    latLngBounds = latLngBoundsBuilder.build();
                } catch (Exception e) {
                    Log.e(TAG, "" + e.getMessage(), e);
                }
            }
            if (latLngBounds != null) {
                if (latLngBounds.northeast.equals(latLngBounds.southwest)) {
                    cameraUpdateWithAllPoints = CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 18);
                } else {
                    cameraUpdateWithAllPoints = CameraUpdateFactory.newLatLngBounds(latLngBounds, dp2px(50));
                }
            }
        }
        return cameraUpdateWithAllPoints;
    }

    @Nullable
    public LatLngBounds getLatLngBounds() {
        if (latLngBounds == null) latLngBounds = latLngBoundsBuilder.build();
        return latLngBounds;
    }

    public void setFollowUserButton(@Nullable View followUserButton) {
        if (this.followUserButton != null && this.followUserButton != followUserButton) {
            this.followUserButton.setOnClickListener(null);
        }
        this.followUserButton = followUserButton;
        if (followUserButton != null) this.followUserButton.setOnClickListener(followUserAction);
    }

    public View getFollowUserButton() {
        return followUserButton;
    }

    public boolean isFollowingUser() {
        return followingUser;
    }

    public void setFollowingUser(boolean followingUser) {
        if (!this.followingUser && followingUser && userLocation != null) {
            panZoomTo(userLocation);
        }
        this.followingUser = followingUser;
    }

    public void showAllPoints() {
        setFollowingUser(false);
        final CameraUpdate cameraUpdateWithAllPoints1 = getCameraUpdateWithAllPoints();
        if (gMap != null && cameraUpdateWithAllPoints1 != null) {
            gMap.animateCamera(cameraUpdateWithAllPoints1);
        }
    }

    View.OnClickListener followUserAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.isSelected()) {
                view.setSelected(false);
                followingUser = false;
            } else {
                view.setSelected(true);
                followingUser = true;
                if (userLocation != null) panZoomTo(userLocation);
            }
        }
    };

    private void panZoomTo(@NonNull LatLng location) {
        if (gMap != null) {
            if (gMap.getCameraPosition().zoom < userZoom) {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, userZoom));
            } else {
                panTo(location);
            }
        }
    }


    protected void updateMyLocation(@NonNull Location location) {
        userLocation = LL(location);
        if (gMap == null) return;
        if (myLocationMarker != null) {
            updateMyLocationMarkerAnimated(userLocation);
            myLocationMarker.showInfoWindow();
        }
//            myLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

        if (waitingToFocusOnAll && layoutOccurred) {
            waitingToFocusOnAll = false;
            latLngBoundsBuilder.include(new LatLng(location.getLatitude(), location.getLongitude()));
            gMap.moveCamera(getCameraUpdateWithAllPoints(true));
        } else if (followingUser) {
            panTo(userLocation);
        } else if (followingUserAndPoint) {
            panTo(userLocation, followPoint);
        }
    }

    /**
     * Focus on a point and the user on each new coordinate. Disables FollowingUser. Pass null to disable this.
     */
    public void setFollowingUserAndPoint(@Nullable LatLng point) {
        setFollowingUser(false);
        followingUserAndPoint = point != null;
        followPoint = point;
        if (point != null) panTo(userLocation, point);
    }

    private static float latLngDistance(LatLngBounds bounds) {
        float[] results = new float[1];
        Location.distanceBetween(bounds.northeast.latitude,
                                 bounds.northeast.longitude,
                                 bounds.southwest.latitude,
                                 bounds.southwest.longitude,
                                 results);
        return results[0];
    }

    public static LatLng LL(@NonNull Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void panTo(@NonNull LatLng location) {
        if (gMap == null) return;
        gMap.animateCamera(CameraUpdateFactory.newLatLng(location));
    }

    protected void panTo(LatLng... points) {
        if (gMap == null || points == null || points.length == 0) return;

        if (points.length == 1) {
            gMap.animateCamera(CameraUpdateFactory.newLatLng(points[0]));
            return;
        }

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng point : points) {
            if (point != null) builder.include(point);
        }

        final LatLngBounds build = builder.build();

        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(build, dp2px(50)));
    }

    void updateMyLocationMarkerAnimated(@NonNull final LatLng target) {
        if (gMap == null) return;
        final long duration = 250;
        handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = gMap.getProjection();

        Point startPoint = proj.toScreenLocation(myLocationMarker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        if (startPoint == null || startLatLng == null) {
            Log.w(TAG,
                  new RuntimeException(String.format(
                          ">startPoint == null? %s >startLatLng == null? %s >myLocationMarker.getPosition %s",
                          startPoint == null,
                          startLatLng == null,
                          myLocationMarker == null ? null : myLocationMarker.getPosition())));
            return;
        }

//        final LinearInterpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed / duration);
                float t = Math.min(elapsed / duration, 1f);
                double lng = t * target.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * target.latitude + (1 - t) * startLatLng.latitude;
                myLocationMarker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                }
            }
        });
    }

    protected void clearMarkers() {
        if (gMap != null) gMap.clear();
    }

    protected void zoomOnLocation(LatLng latLng) {
        if (gMap != null) gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public void onStart() {
//        parentBackground = parentView.getBackground();
//        ResourceUtils.setBackground(parentView, null);
        ResourceUtils.setBackground(parentView, new ColorDrawable(Color.CYAN));
    }

    public void onResume() {
//        parentBackground = parentView.getBackground();
//        ResourceUtils.setBackground(parentView, null);
        ResourceUtils.setBackground(parentView, null);
    }

    protected void onStop() {
//        ResourceUtils.setBackground(parentView, new ColorDrawable(Color.RED));
//        if (this.parentBackground != null) ResourceUtils.setBackground(parentView, parentBackground);
    }

    public enum AnchorPlacement {
        CENTER_BOTTOM, CENTER;

        public float u() {
            switch (this) {
                case CENTER_BOTTOM:
                case CENTER:
                default:
                    return 0.5f;
            }
        }

        public float v() {
            switch (this) {

                case CENTER_BOTTOM:
                    return 1;
                case CENTER:
                default:
                    return 0.5f;
            }
        }
    }

    /**
     * Holds a bitmap and its placement.
     */
    public static class AnchoredBitmap {
        private final float u;
        private final float v;
        private final BitmapDescriptor bitmapD;

        public AnchoredBitmap(@Nullable Bitmap bitmap, AnchorPlacement placement) {
            this.bitmapD = BitmapDescriptorFactory.fromBitmap(bitmap);
            this.u = placement.u();
            this.v = placement.v();
        }

        public MarkerOptions set(MarkerOptions options) {
            return options.icon(bitmapD).anchor(u, v);
        }
    }
}
