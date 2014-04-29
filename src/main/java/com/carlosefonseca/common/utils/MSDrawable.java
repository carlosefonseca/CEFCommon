package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

/**
 * MultiStateDrawable: A StateListDrawable builder!
 * <p/>
 * Use {@link #icon(android.graphics.Bitmap)} to set the icon for all states. You can then set colors with {@link
 * #normalColor(int)} and friends.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.BitmapDrawable)} and friends to use different drawables for each state.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.BitmapDrawable)} and friends to use different drawables for each state and also
 * tint them.
 * <p/>
 * Use {@link #normalColor(int)} (without using {@link #icon(android.graphics.Bitmap)}) to only do colors.
 * <p/>
 * In all cases, call {@link #build()} when you're done setting stuff.
 */
@SuppressWarnings("UnusedDeclaration")
public class MSDrawable {

    private final Context mContext;

    enum Mode {
        COLORS, SINGLE_ICON, MULTI_ICON
    }

    public static final int NORMAL = 0;
    public static final int PRESSED = 1;
    public static final int SELECTED = 2;

    Mode mMode = Mode.COLORS;

    Bitmap mIcon;
    int[] mColors = new int[3];
    BitmapDrawable[] mDrawables = new BitmapDrawable[3];
    Bitmap[] mBitmaps = new Bitmap[3];
    Drawable[] mFinal = new Drawable[3];

    public MSDrawable(Context context) { this.mContext = context;}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the icon for all states. Tint it by calling {@link #normalColor(int)} and sibling methods.
     */
    public MSDrawable icon(Bitmap icon) {
        mMode = Mode.SINGLE_ICON;
        mIcon = icon;
        return this;
    }

    /**
     * Sets the icon for all states. Tint it by calling {@link #normalColor(int)} and sibling methods.
     *
     * @param iconRes The resource id from where to grab the Bitmap.
     */
    public MSDrawable icon(int iconRes) {
        mMode = Mode.SINGLE_ICON;
        mIcon = ((BitmapDrawable) mContext.getResources().getDrawable(iconRes)).getBitmap();
        return this;
    }

    /**
     * Sets the icon for all states. Tint it by calling {@link #normalColor(int)} and sibling methods.
     *
     * @param icon A drawable from where to grab the bitmap.
     */
    public MSDrawable icon(BitmapDrawable icon) {
        mMode = Mode.SINGLE_ICON;
        mIcon = icon.getBitmap();
        return this;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public MSDrawable normal(Bitmap bitmap) {
        set(bitmap, 0, NORMAL);
        return this;
    }

    public MSDrawable normal(BitmapDrawable drawable) {
        set(drawable, 0, NORMAL);
        return this;
    }

    public MSDrawable normalRes(int res) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), 0, NORMAL);
        return this;
    }

    public MSDrawable normalColor(int color) {
        set(color, NORMAL);
        return this;
    }

    public MSDrawable normal(BitmapDrawable drawable, int color) {
        set(drawable, color, NORMAL);
        return this;
    }

    public MSDrawable normalRes(int res, int color) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), color, NORMAL);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public MSDrawable pressed(Bitmap bitmap) {
        set(bitmap, 0, PRESSED);
        return this;
    }

    public MSDrawable pressed(BitmapDrawable drawable) {
        set(drawable, 0, PRESSED);
        return this;
    }

    public MSDrawable pressedRes(int res) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), 0, PRESSED);
        return this;
    }

    public MSDrawable pressedColor(int color) {
        set(color, PRESSED);
        return this;
    }

    public MSDrawable pressed(BitmapDrawable drawable, int color) {
        set(drawable, color, PRESSED);
        return this;
    }

    public MSDrawable pressedRes(int res, int color) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), color, PRESSED);
        return this;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public MSDrawable selected(Bitmap bitmap) {
        set(bitmap, 0, SELECTED);
        return this;
    }

    public MSDrawable selected(BitmapDrawable drawable) {
        set(drawable, 0, SELECTED);
        return this;
    }

    public MSDrawable selectedRes(int res) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), 0, SELECTED);
        return this;
    }

    public MSDrawable selectedColor(int color) {
        set(color, SELECTED);
        return this;
    }

    public MSDrawable selected(BitmapDrawable drawable, int color) {
        set(drawable, color, SELECTED);
        return this;
    }

    public MSDrawable selectedRes(int res, int color) {
        set((BitmapDrawable) mContext.getResources().getDrawable(res), color, SELECTED);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Always returns a new instance, but caches contents.
     */
    public Drawable build() {
        switch (mMode) {
            case COLORS:
                return createFromDrawables(getColor(NORMAL), getColor(PRESSED), getColor(SELECTED));
            case SINGLE_ICON:
                return createFromDrawables(recolorIcon(NORMAL), recolorIcon(PRESSED), recolorIcon(SELECTED));
            case MULTI_ICON:
                return createFromDrawables(recolorBitmap(NORMAL), recolorBitmap(PRESSED), recolorBitmap(SELECTED));
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ColorDrawable getColor(int state) {
        return mFinal[state] != null
               ? (ColorDrawable) mFinal[state]
               : mColors[state] != 0 ? new ColorDrawable(mColors[state]) : null;
    }

    private BitmapDrawable recolorIcon(int state) {
        return mFinal[state] != null
               ? (BitmapDrawable) mFinal[state]
               : mColors[state] != 0 ? ImageUtils.createRecoloredDrawable(mContext, mIcon, mColors[state]) : null;
    }

    private BitmapDrawable recolorBitmap(int state) {
        return mFinal[state] != null
               ? (BitmapDrawable) mFinal[state]
               : mColors[state] != 0
                 ? ImageUtils.createRecoloredDrawable(mContext, getBitmap(state), mColors[state])
                 : getDrawable(state);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static StateListDrawable createFromDrawables(Drawable normal, Drawable pressed, Drawable selected) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        if (selected != null) stateListDrawable.addState(new int[]{android.R.attr.state_selected}, selected);
        if (pressed != null) stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        stateListDrawable.addState(StateSet.WILD_CARD, normal);
        return stateListDrawable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void set(BitmapDrawable drawable, int color, int state) {
        set(drawable, null, color, state);
    }

    private void set(Bitmap bitmap, int color, int state) {
        set(null, bitmap, color, state);
    }

    private void set(int color, int state) {
        set(null, null, color, state);
    }

    private void set(BitmapDrawable bitmapDrawable, Bitmap bitmap, int color, int state) {
        if (bitmapDrawable != null || bitmap != null) {
            mMode = Mode.MULTI_ICON;
        }
        mColors[state] = color;
        mDrawables[state] = bitmapDrawable;
        mBitmaps[state] = bitmap;
        mFinal[state] = null;
    }

    private Bitmap getBitmap(int state) {
        return mBitmaps[state] != null ? mBitmaps[state] : mDrawables[state].getBitmap();
    }

    private BitmapDrawable getDrawable(int state) {
        return mDrawables[state] != null ? mDrawables[state] : new BitmapDrawable(mContext.getResources(), mBitmaps[state]);
    }
}