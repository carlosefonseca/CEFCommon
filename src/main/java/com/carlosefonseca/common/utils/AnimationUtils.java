package com.carlosefonseca.common.utils;

import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AnimationUtils {

    private static final java.lang.String TAG = CodeUtils.getTag(AnimationUtils.class);

    private AnimationUtils() {}

    /**
     * Creates a value animator that animates the height of a view.
     *
     * @param view       The view to animate.
     * @param startValue The initial value.
     * @param finalValue The final value.
     * @param duration   The duration of the animation.
     * @return The ValueAnimator
     */
    public static ValueAnimator AnimateViewHeight(final View view, int startValue, int finalValue, long duration) {
        assert view.getLayoutParams() != null;
        ValueAnimator va = ValueAnimator.ofInt(startValue, finalValue);
        va.setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                view.requestLayout();
            }
        });
        return va;
    }

    /**
     * Convenience method to run Runnables on animation start and/or animation end.
     *
     * @param onStart A runnable that will be ran on animation start.
     * @param onEnd   A runnable that will be ran on animation end.
     */
    public static Animator.AnimatorListener makeListener(@Nullable final Runnable onStart, @Nullable final Runnable onEnd) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (onStart != null) onStart.run();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (onEnd != null) onEnd.run();
            }

            @Override
            public void onAnimationCancel(Animator animator) { }


            @Override
            public void onAnimationRepeat(Animator animator) { }
        };
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static ViewPropertyAnimator fade(final boolean becomeVisible, final View view, int millis) {
        return fade(becomeVisible, view, millis, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static ViewPropertyAnimator fade(final boolean becomeVisible,
                                            final View view,
                                            int millis,
                                            @Nullable final AnimatorListenerAdapter listener) {
        return view.animate().alpha(becomeVisible ? 1 : 0).setDuration(millis).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                if (view.getVisibility() != View.VISIBLE) view.setVisibility(View.VISIBLE);
                if (listener != null) listener.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!becomeVisible) view.setVisibility(View.GONE);
                if (listener != null) listener.onAnimationEnd(animation);
            }
        });
    }

    public static void setImageBitmapWithXFade(final ImageView imageView, final Bitmap bitmap, int millis) {
        setImageDrawableWithXFade(imageView, new BitmapDrawable(imageView.getResources(), bitmap), millis);
    }

    public static void setImageDrawableWithXFade(final ImageView imageView, final Drawable drawable, int millis) {
        Drawable currentDrawable = imageView.getDrawable();
        if (currentDrawable == null) {
            currentDrawable = new ColorDrawable(Color.TRANSPARENT);
        } else if (currentDrawable instanceof TransitionDrawable) {
            currentDrawable = ((TransitionDrawable) currentDrawable).getDrawable(1);
        }
        Drawable[] arrayDrawable = new Drawable[2];
        arrayDrawable[0] = currentDrawable;
        arrayDrawable[1] = drawable;
        TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
        transitionDrawable.setCrossFadeEnabled(true);
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(millis);
    }

    public enum Parameter {
        HEIGHT, WIDTH, PADLEFT
    }

    /**
     * Animates certain properties of a view.
     *
     * @return A ValueAnimator
     */
    public static ValueAnimator AnimateView(final View view,
                                            final Parameter parameter,
                                            int startValue,
                                            int finalValue,
                                            int duration) {
        ValueAnimator va = ValueAnimator.ofInt(startValue, finalValue);
        va.setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                switch (parameter) {
                    case HEIGHT:
                        break;
                    case WIDTH:
                        break;
                    case PADLEFT:
                        view.setPadding(value, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                        break;
                }
                view.requestLayout();
            }
        });
        return va;
    }

    public static class AnimationListenerImpl implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) { }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setScale(List<? extends View> views, int scale) {
        for (View view : views) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }
}
