package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.util.StateSet;
import android.view.View;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static android.R.attr.*;

/**
 * MultiStateDrawable: A StateListDrawable builder!
 * <p/>
 * Use {@link #icon(android.graphics.Bitmap)} to set the icon for all states. You can then set colors with {@link
 * #normalColor(int)} and friends.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.BitmapDrawable)} and friends to use different bitmap drawables for each
 * state.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.BitmapDrawable, int)} and friends to use different bitmap drawables for
 * each state and also
 * tint them.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.shapes.Shape)} and friends to use different shape drawables for each
 * state.
 * <p/>
 * Use {@link #normal(android.graphics.drawable.shapes.Shape, int)} and friends to use different shape drawables for
 * each state and also
 * tint them.
 * <p/>
 * Use {@link #normalColor(int)} and friends (without using {@link #icon(android.graphics.Bitmap)}) to only do solid
 * colors.
 * <p/>
 * Use {@link #normalRes(int)} and friends to create a drawable from a Resource ID, without tinting.
 * <p/>
 * Use {@link #normalRes(int, int)} and friends to create a drawable from a Resource ID and then tint it.
 * If color is 0 or {@link android.graphics.Color#TRANSPARENT}, it will not tint.
 * <p/>
 * In all cases, call {@link #build()} when you're done setting stuff.
 */
@SuppressWarnings("UnusedDeclaration")
public class MSDrawable {

    private final Resources mResources;

    public MSDrawable asBackground(View view) {
        Drawable background = build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
        return this;
    }

    public MSDrawable asImage(ImageView imageView) {
        imageView.setImageDrawable(build());
        return this;
    }

    enum Mode {
        COLORS, SINGLE_ICON, MULTI_ICON
    }

    public static final int NORMAL = 0;
    public static final int PRESSED = 1;
    public static final int SELECTED = 2;
    public static final int DISABLED = 3;

    public static final int STATE_COUNT = 4;

    Mode mMode = Mode.COLORS;

    Bitmap mIcon;
    Shape mShape;
    int[] mColors = new int[STATE_COUNT];
    BitmapDrawable[] mBitmapDrawables = new BitmapDrawable[STATE_COUNT];
    //    Shape[] mShapes = new Shape[3];
    ShapeDrawable[] mShapeDrawables = new ShapeDrawable[STATE_COUNT];
    Bitmap[] mBitmaps = new Bitmap[STATE_COUNT];
    Drawable[] mFinal = new Drawable[STATE_COUNT];

    /**
     * Create a new builder that is able to access resources. Is the same as MSDrawable(context.getResources())
     */
    public MSDrawable(Context context) { this(context.getResources());}

    /**
     * Create a new builder that is able to access resources
     */
    public MSDrawable(Resources resources) {
        mResources = resources;
    }

    /**
     * Create a new builder that will not access any resources
     */
    public MSDrawable() {
        mResources = null;
    }


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
        mIcon = ((BitmapDrawable) mResources.getDrawable(iconRes)).getBitmap();
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

    /**
     * Sets the icon for all states. Tint it by calling {@link #normalColor(int)} and sibling methods.
     *
     * @param icon A drawable from where to grab the bitmap.
     */
    public MSDrawable icon(Shape icon) {
        mMode = Mode.SINGLE_ICON;
        mShape = icon;
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

    public MSDrawable normal(Drawable drawable) {
        set(drawable, 0, NORMAL);
        return this;
    }

    public MSDrawable normal(Shape shape) {
        set(shape, 0, NORMAL);
        return this;
    }

    public MSDrawable normalRes(int res) {
        set(mResources.getDrawable(res), 0, NORMAL);
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

    public MSDrawable normal(Shape shape, int color) {
        set(shape, color, NORMAL);
        return this;
    }

    public MSDrawable normalRes(int res, int color) {
        set(mResources.getDrawable(res), color, NORMAL);
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

    public MSDrawable pressed(Drawable drawable) {
        set(drawable, 0, PRESSED);
        return this;
    }

    public MSDrawable pressed(Shape shape) {
        set(shape, 0, PRESSED);
        return this;
    }

    public MSDrawable pressedRes(int res) {
        set(mResources.getDrawable(res), 0, PRESSED);
        return this;
    }

    public MSDrawable pressedColor(int color) {
        set(color, PRESSED);
        return this;
    }

    public MSDrawable pressedColor() {
        set(ResourceUtils.computeDarkerColor(mColors[NORMAL], 0.25), PRESSED);
        return this;
    }

    public MSDrawable pressed(BitmapDrawable drawable, int color) {
        set(drawable, color, PRESSED);
        return this;
    }

    public MSDrawable pressed(Shape shape, int color) {
        set(shape, color, PRESSED);
        return this;
    }

    public MSDrawable pressedRes(int res, int color) {
        set(mResources.getDrawable(res), color, PRESSED);
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

    public MSDrawable selected(Drawable drawable) {
        set(drawable, 0, SELECTED);
        return this;
    }

    public MSDrawable selected(Shape shape) {
        set(shape, 0, SELECTED);
        return this;
    }

    public MSDrawable selectedRes(int res) {
        set(mResources.getDrawable(res), 0, SELECTED);
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

    public MSDrawable selected(Shape shape, int color) {
        set(shape, color, SELECTED);
        return this;
    }

    public MSDrawable selectedRes(int res, int color) {
        set(mResources.getDrawable(res), color, SELECTED);
        return this;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public MSDrawable disabled(Bitmap bitmap) {
        set(bitmap, 0, DISABLED);
        return this;
    }

    public MSDrawable disabled(BitmapDrawable drawable) {
        set(drawable, 0, DISABLED);
        return this;
    }

    public MSDrawable disabled(Drawable drawable) {
        set(drawable, 0, DISABLED);
        return this;
    }

    public MSDrawable disabled(Shape shape) {
        set(shape, 0, DISABLED);
        return this;
    }

    public MSDrawable disabledRes(int res) {
        set(mResources.getDrawable(res), 0, DISABLED);
        return this;
    }

    public MSDrawable disabledColor(int color) {
        set(color, DISABLED);
        return this;
    }

    public MSDrawable disabled(BitmapDrawable drawable, int color) {
        set(drawable, color, DISABLED);
        return this;
    }

    public MSDrawable disabled(Shape shape, int color) {
        set(shape, color, DISABLED);
        return this;
    }

    public MSDrawable disabledRes(int res, int color) {
        set(mResources.getDrawable(res), color, DISABLED);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Always returns a new instance, but caches contents.
     */
    public StateListDrawable build() {
        switch (mMode) {
            case COLORS:
                return createFromDrawables(getColor(NORMAL), getColor(PRESSED), getColor(SELECTED), getColor(DISABLED));
            case SINGLE_ICON:
                return createFromDrawables(recolorIcon(NORMAL),
                                           recolorIcon(PRESSED),
                                           recolorIcon(SELECTED),
                                           recolorIcon(DISABLED));
            case MULTI_ICON:
                return createFromDrawables(recolorDrawable(NORMAL),
                                           recolorDrawable(PRESSED),
                                           recolorDrawable(SELECTED),
                                           recolorDrawable(DISABLED));
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    private ColorDrawable getColor(int state) {
        return mFinal[state] != null
               ? (ColorDrawable) mFinal[state]
               : mColors[state] != 0 ? new ColorDrawable(mColors[state]) : null;
    }

    private Drawable recolorIcon(int state) {
        if (mFinal[state] != null) {
            return mFinal[state];
        }

        if (mColors[state] != 0) {
            if (mIcon != null) {
                mFinal[state] = ImageUtils.createRecoloredDrawable(mResources, mIcon, mColors[state]);
            } else if (mShape != null) {
                mFinal[state] = recoloredShape(new ShapeDrawable(mShape), mColors[state]);
            }
        } else if (state == NORMAL) {
            if (mIcon != null) {
                mFinal[state] = new BitmapDrawable(mResources, mIcon);
            } else if (mShape != null) {
                mFinal[state] = new ShapeDrawable(mShape);
            }
        }

        return mFinal[state];
    }

    @Nullable
    private Drawable recolorDrawable(int state) {
        if (mFinal[state] != null) {
            return mFinal[state];
        }

        if (mShapeDrawables[state] != null) { // shape
            return recolorShape(state);
        } else if (mBitmaps[state] != null || mBitmapDrawables[state] != null) {
            return recolorBitmap(state);
        }
        return null;
    }


    private BitmapDrawable recolorBitmap(int state) {
        return mFinal[state] != null
               ? (BitmapDrawable) mFinal[state]
               : mColors[state] != 0
                 ? ImageUtils.createRecoloredDrawable(mResources, getBitmap(state), mColors[state])
                 : getDrawable(state);
    }

    private Drawable recolorShape(int state) {
        if (mFinal[state] == null) {
            mFinal[state] = recoloredShape(mShapeDrawables[state], mColors[state]);
        }
        return mFinal[state];
    }

    private static ShapeDrawable recoloredShape(ShapeDrawable shapeDrawable, int color) {
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static StateListDrawable createFromDrawables(@NonNull Drawable normal,
                                                        @Nullable Drawable pressed,
                                                        @Nullable Drawable selected,
                                                        @Nullable Drawable disabled) {
        StateListDrawable stateListDrawable = new StateListDrawable();

        if (disabled == null) {
            if (selected != null) {
                stateListDrawable.addState(new int[]{state_selected}, selected);
                stateListDrawable.addState(new int[]{state_checked}, selected);
            }
            if (pressed != null) stateListDrawable.addState(new int[]{state_pressed}, pressed);
            stateListDrawable.addState(StateSet.WILD_CARD, normal);
        } else {
            if (selected != null) {
                stateListDrawable.addState(new int[]{state_selected, state_enabled}, selected);
                stateListDrawable.addState(new int[]{state_checked, state_enabled}, selected);
            }
            if (pressed != null) {
                stateListDrawable.addState(new int[]{state_pressed, state_enabled}, pressed);
            }
            stateListDrawable.addState(new int[]{state_enabled}, normal);
            stateListDrawable.addState(StateSet.WILD_CARD, disabled);
        }
        return stateListDrawable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void set(Drawable drawable, int color, int state) {
        set(drawable, null, null, state, color);
    }

    private void set(Shape shape, int color, int state) {
        set(null, null, shape, state, color);
    }

    private void set(Bitmap bitmap, int color, int state) {
        set(null, bitmap, null, state, color);
    }

    private void set(int color, int state) {
        set(null, null, null, state, color);
    }

    private void set(@Nullable Drawable bitmapDrawable, @Nullable Bitmap bitmap, @Nullable Shape shape, int state, int color) {
        if (bitmapDrawable != null || bitmap != null || shape != null) {
            mMode = Mode.MULTI_ICON;
        }
        mColors[state] = color;
//        mShapes[state] = shape;
        mShapeDrawables[state] = shape != null ? new ShapeDrawable(shape) : null;
        if (bitmapDrawable instanceof BitmapDrawable) {
            mBitmapDrawables[state] = (BitmapDrawable) bitmapDrawable;
            mBitmaps[state] = bitmap;
            mFinal[state] = null;
        } else {
            mBitmapDrawables[state] = null;
            mBitmaps[state] = null;
            mFinal[state] = bitmapDrawable;
        }
    }

    private Bitmap getBitmap(int state) {
        return mBitmaps[state] != null ? mBitmaps[state] : mBitmapDrawables[state].getBitmap();
    }

    private BitmapDrawable getDrawable(int state) {
        return mBitmapDrawables[state] != null
               ? mBitmapDrawables[state]
               : new BitmapDrawable(mResources, mBitmaps[state]);
    }
}