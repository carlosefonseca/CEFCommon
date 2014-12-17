package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@SuppressWarnings("UnusedDeclaration")
public final class ResourceUtils {
    private static final java.lang.String TAG = CodeUtils.getTag(ResourceUtils.class);

    private ResourceUtils() {}

    public static String s(int res) {
        return CFApp.getContext().getResources().getString(res);
    }

    public static Drawable d(int res) {
        return CFApp.getContext().getResources().getDrawable(res);
    }

    public static float dim(int res) {
        return CFApp.getContext().getResources().getDimension(res);
    }

    public static int dimPx(int res) {
        return CFApp.getContext().getResources().getDimensionPixelSize(res);
    }

    /**
     * Get a resource for a given string name, for instance, "drawable/some_drawable_name"
     *
     * @return The resource identifier or 0 if not found.
     */
    public static int resourceForName(String s) {
        return CFApp.getContext().getResources().getIdentifier(s, null, CFApp.getContext().getPackageName());
    }

    /**
     * Correct way to set the text size on a text view.
     *
     * @param view    The text view.
     * @param sizeRes The resource id for the dimension.
     */
    public static void setTextSize(@NotNull TextView view, int sizeRes) {
        assert view.getResources() != null;
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getResources().getDimension(sizeRes));
    }

    /**
     * Helper for setting the background on a view while hiding the deprecation of
     * {@link View#setBackgroundDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param view       The view.
     * @param background The drawable for the background.
     */
    public static void setBackground(@NotNull View view, @Nullable Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

    /**
     * Creates a State List for normal and pressed states that can be used as textColor.
     *
     * @param normalColor   A color.
     * @param selectedColor A color.
     * @return A new ColorStateList with pressed and normal states.
     */
    public static ColorStateList createDualColorStateList(int normalColor, int selectedColor) {
        return createDualColorStateList(normalColor, selectedColor, SELECTED);
    }

    /**
     * Creates a State List for normal and pressed states that can be used as textColor.
     *
     * @param normal A color.
     * @param other  A color.
     * @param state {@link #PRESSED}, {@link #SELECTED} or {@link #BOTH}
     * @return A new ColorStateList with pressed and normal states.
     */
    public static ColorStateList createDualColorStateList(int normal, int other, int state) {
        int[] stateList;
        switch (state) {
            case PRESSED:
                stateList = new int[]{android.R.attr.state_pressed};
                break;
            case SELECTED:
                stateList = new int[]{android.R.attr.state_checked, android.R.attr.state_selected};
                break;
            case BOTH:
                stateList = new int[]{android.R.attr.state_checked, android.R.attr.state_selected, android.R.attr.state_pressed};
                break;
            default:
                throw new IllegalArgumentException("Unrecognized state");
        }

        final int[][] states = new int[stateList.length + 1][];
        final int[] colors = new int[stateList.length + 1];

        for (int i = 0; i < states.length; i++) {
            states[i] = i == stateList.length ? new int[]{} : new int[]{stateList[i]};
            colors[i] = i == stateList.length ? normal : other;
        }
        return new ColorStateList(states, colors);
    }

    /**
     * Creates a State List for normal, pressed and selected states that can be used as textColor.
     *
     * @param normal A color.
     * @param pressed  A color.
     * @param selected A color.
     * @return A new ColorStateList with all states set.
     */
    public static ColorStateList createTripleColorStateList(int normal, int pressed, int selected) {
        int[] stateList = new int[]{android.R.attr.state_checked,
                                    android.R.attr.state_selected,
                                    android.R.attr.state_pressed};

        final int[][] states = new int[stateList.length + 1][];
        final int[] colors = new int[]{selected, selected, pressed, normal};

        for (int i = 0; i < states.length; i++) {
            states[i] = i == stateList.length ? new int[]{} : new int[]{stateList[i]};
        }

        return new ColorStateList(states, colors);
    }

    /**
     * Prepends getExternalFilesDir to the parameter. This is the app's private files.
     */
    @Nullable
    public static File getFullPath(String localPath) {
        final File externalFilesDir = CFApp.getContext().getExternalFilesDir(null);
        return externalFilesDir == null ? null : new File(externalFilesDir, localPath);
    }

    /**
     * Creates a {@link android.graphics.drawable.StateListDrawable} from a resource id which is recolored with a color for the
     * default state and another color for both PRESSED and SELECTED states.
     *
     * @param context    A context.
     * @param resId      The (bitmap) resource id.
     * @param color      The default color.
     * @param otherColor The color for the other states.
     */
    public static StateListDrawable createDualStateDrawableFromResource(Context context, int resId, int color, int otherColor) {
        return ResourceUtils.createDualStateDrawable(
                new BitmapDrawable(context.getResources(), ImageUtils.createRecoloredBitmap(context, resId, color)),
                new BitmapDrawable(context.getResources(), ImageUtils.createRecoloredBitmap(context, resId, otherColor)), BOTH);
    }

    /**
     * Sets the background of a view to two colors, one for the pressed+enabled state and another for all other states.
     * (Unfortunately the State List Drawable object can't be applied to multiple objects,so you'll have to call this method for
     * every view, since the created StateListDrawable can't be reused.
     *
     * @param button   The view to apply the color.
     * @param color    The default color.
     * @param selected The color for the Pressed+Enabled state.
     */
    public static void setDualColorBackground(View button, int color, int selected) {
        setDualColorBackground(button, color, selected, SELECTED);
    }

    /**
     * Sets the background of a view to two colors, one for the pressed+enabled state and another for all other states.
     * (Unfortunately the State List Drawable object can't be applied to multiple objects,so you'll have to call this method for
     * every view, since the created StateListDrawable can't be reused.
     *
     * @param button The view to apply the color.
     * @param color  The default color.
     * @param other  The color for the Pressed+Enabled state.
     * @param state  For what state
     */
    public static void setDualColorBackground(View button, int color, int other, int state) {
        setDualDrawableBackground(button, new ColorDrawable(color), new ColorDrawable(other), state);
    }

    /**
     * Sets the background of a view to two colors, one for the pressed+enabled state and another for all other states.
     * (Unfortunately the State List Drawable object can't be applied to multiple objects,so you'll have to call this method for
     * every view, since the created StateListDrawable can't be reused.
     *
     * @param button   The view to apply the color.
     * @param normal   The default color.
     * @param selected The color for the Pressed+Enabled state.
     */
    public static void setDualDrawableBackground(View button, Drawable normal, Drawable selected) {
        setDualDrawableBackground(button, normal, selected, SELECTED);
    }

    /**
     * Sets the background of a view to two colors, one for the pressed+enabled state and another for all other states.
     * (Unfortunately the State List Drawable object can't be applied to multiple objects,so you'll have to call this method for
     * every view, since the created StateListDrawable can't be reused.
     *
     * @param button The view to apply the color.
     * @param state {@link #PRESSED}, {@link #SELECTED} or {@link #BOTH}
     */
    public static void setDualDrawableBackground(View button, Drawable normal, Drawable other, int state) {
        setBackground(button, createDualStateDrawable(normal, other, state));
    }

    public static enum State {
        /**
         * Pressed state. Will use <tt>android.R.attr.state_pressed</tt>.
         */
        PRESSED,
        /**
         * Selected state. Will use <tt>android.R.attr.state_selected</tt>.
         */
        SELECTED,
        /**
         * Both Pressed and Selected states. Will use <tt>android.R.attr.state_pressed</tt> and
         * <tt>android.R.attr.state_selected</tt>.
         */
        BOTH
    }

    public static final int PRESSED = 0;
    public static final int SELECTED = 1;
    public static final int BOTH = 2;

    /**
     * Creates a {@link android.graphics.drawable.StateListDrawable} with a normal color and a color for another state.
     *
     * @param normalColor Default color.
     * @param otherColor  Color for the specified states.
     * @param state       {@link #PRESSED}, {@link #SELECTED} or {@link #BOTH}
     */
    public static StateListDrawable createDualStateDrawable(int normalColor, int otherColor, int state) {
        return createDualStateDrawable(new ColorDrawable(normalColor), new ColorDrawable(otherColor), state);
    }

    /**
     * Creates a {@link android.graphics.drawable.StateListDrawable} with a drawable for the default state and another drawable
     * for the SELECTED state.
     *
     * @param normal   Default state drawable.
     * @param selected Drawable for the selected state.
     */
    public static StateListDrawable createDualStateDrawable(Drawable normal, Drawable selected) {
        return createDualStateDrawable(normal, selected, SELECTED);
    }

    /**
     * Creates a {@link android.graphics.drawable.StateListDrawable} with a drawable for the default state and another drawable
     * for the other specified states.
     *
     * @param normal Default state drawable.
     * @param other  Drawable for the specified state.
     * @param state  {@link #PRESSED}, {@link #SELECTED} or {@link #BOTH}
     */
    public static StateListDrawable createDualStateDrawable(Drawable normal, Drawable other, int state) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        if (state == PRESSED || state == BOTH) {
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, other);
        }
        if (state == SELECTED || state == BOTH) {
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, other);
            stateListDrawable.addState(new int[]{android.R.attr.state_checked}, other);
        }
        stateListDrawable.addState(StateSet.WILD_CARD, normal);
        return stateListDrawable;
    }

    /**
     * Takes a color, extracts it's HSV values and reduces the Value component by some amount.
     *
     * @param color    The original color.
     * @param darkness Amount to subtract to Value, which is [0..1].
     * @return The new, darker color.
     */
    public static int computeDarkerColor(int color, double darkness) {
        return new HSVColor(color).addValue((float) -darkness).color();
    }

    public static HashSet<String> getAssets(Context context1) {
        HashSet<String> assets;
        try {
            assets = new HashSet<>(Arrays.asList(context1.getAssets().list("")));
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
            assets = new HashSet<>();
        }
        return assets;
    }


    public static class HSVColor {

        private float[] hsv;

        short HUE = 0;
        short SAT = 1;
        short VAL = 2;

        public HSVColor(int color) {
            hsv = new float[3];
            Color.colorToHSV(color, hsv);
        }

        public HSVColor addRGB(int value) {
            int color = color();
            int argb = Color.argb(Color.alpha(color),
                                  Math.min(0xFF, Color.red(color) + value),
                                  Math.min(0xFF, Color.green(color) + value),
                                  Math.min(0xFF, Color.blue(color) + value));
            Color.colorToHSV(argb, hsv);
            return this;
        }

        public HSVColor addHue(double value) {
            return add(HUE, value);
        }

        public HSVColor addSat(double value) {
            return add(SAT, value);
        }

        public HSVColor addValue(double value) {
            return add(VAL, value);
        }

        public HSVColor mulHue(double value) {
            return mul(HUE, value);
        }

        public HSVColor mulSat(double value) {
            return mul(SAT, value);
        }

        public HSVColor mulValue(double value) {
            return mul(VAL, value);
        }

        private HSVColor add(short field, double value) {
            hsv[field] = (float) Math.min(Math.max(0, hsv[field] + value), field == HUE ? 359 : 1);
            return this;
        }


        private HSVColor mul(short field, double value) {
            hsv[field] = (float) Math.min(Math.max(hsv[field] * value, 0), field == HUE ? 359 : 1);
            return this;
        }

        public int color() {
            return Color.HSVToColor(hsv);
        }
    }

    public static String hexColor(int intColor) {
        return Integer.toHexString(intColor);
    }

}
