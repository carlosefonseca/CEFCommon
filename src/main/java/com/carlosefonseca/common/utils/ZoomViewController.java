package com.carlosefonseca.common.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public abstract class ZoomViewController<T extends View> {
    private final Context mContext;
    private final ViewGroup mContainer;
    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;
    private View mFadeView;

    public ZoomViewController(ViewGroup container) {
        this.mContainer = container;
        this.mContext = container.getContext();
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public ZoomViewController(Activity activity) {
        this((ViewGroup) activity.findViewById(android.R.id.content));
    }

    /**
     * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
     * image view and animating its bounds to fit the entire activity content area. More
     * specifically:
     * <p/>
     * <ol>
     * <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
     * <li>Calculate the starting and ending bounds for the expanded view.</li>
     * <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     * simultaneously, from the starting bounds to the ending bounds.</li>
     * <li>Zoom back out by running the reverse animation on click.</li>
     * </ol>
     *
     * @param thumbView    The thumbnail view to zoom in.
     * @param expandedView
     */
    protected void zoomFromView(final View thumbView, final T expandedView) {

        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mContainer.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        thumbView.setAlpha(0f);
        expandedView.setVisibility(View.VISIBLE);

        final View fadeView = getFadeView();
        fadeView.setVisibility(View.VISIBLE);
        fadeView.setAlpha(0f);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedView.setPivotX(0f);
        expandedView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(expandedView, View.X, startBounds.left, finalBounds.left))
           .with(ObjectAnimator.ofFloat(expandedView, View.Y, startBounds.top, finalBounds.top))
           .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_X, startScale, 1f))
           .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_Y, startScale, 1f))
           .with(ObjectAnimator.ofFloat(mFadeView, View.ALPHA, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(expandedView, View.X, startBounds.left))
                   .with(ObjectAnimator.ofFloat(expandedView, View.Y, startBounds.top))
                   .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_X, startScaleFinal))
                   .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_Y, startScaleFinal))
                   .with(ObjectAnimator.ofFloat(mFadeView, View.ALPHA, 0f));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedView.setVisibility(View.GONE);
                        mFadeView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedView.setVisibility(View.GONE);
                        mFadeView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    // TODO: make configurable
    private View getFadeView() {
        if (mFadeView == null) {
            mFadeView = new View(getContext());
            ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
            mDrawable.getPaint()
                     .setShader(new RadialGradient(mContainer.getWidth() / 2,
                                                   mContainer.getHeight() / 2,
                                                   mContainer.getWidth() * 2 / 3,
                                                   Color.TRANSPARENT,
                                                   Color.parseColor("#A0000000"),
                                                   Shader.TileMode.CLAMP));

            ResourceUtils.setBackground(mFadeView, mDrawable);
            mFadeView.setVisibility(View.GONE);
            mContainer.addView(mFadeView, MATCH_PARENT, MATCH_PARENT);
        }
        return mFadeView;
    }

    public Context getContext() {
        return mContext;
    }

    T mView;

    protected T getExpandedView() {
        if (mView == null) {
            getFadeView();
            mView = createExpandedView();
            mContainer.addView(mView, MATCH_PARENT, MATCH_PARENT);
        }
        return mView;
    }

    protected abstract T createExpandedView();
}
