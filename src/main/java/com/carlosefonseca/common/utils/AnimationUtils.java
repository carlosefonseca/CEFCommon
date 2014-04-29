package com.carlosefonseca.common.utils;

import android.view.View;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import org.jetbrains.annotations.Nullable;

public final class AnimationUtils {

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
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
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
}
