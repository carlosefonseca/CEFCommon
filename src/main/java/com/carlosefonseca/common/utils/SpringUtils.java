package com.carlosefonseca.common.utils;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.nineoldandroids.animation.ArgbEvaluator;

public class SpringUtils {

    public static Spring setSpringTouch(SpringSystem springSystem1, final View button) {
        return setSpringTouch(springSystem1, button, 0.8);
    }

    public static Spring setSpringTouch(SpringSystem springSystem1, final View button, final double endValue) {
        return setSpringTouch(springSystem1, button, endValue, Color.TRANSPARENT, Color.TRANSPARENT);
    }

    public static Spring setSpringTouch(SpringSystem springSystem1,
                                        final View button,
                                        final double endValue,
                                        final int pressedColor,
                                        final int normalColor) {

        final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        final boolean colors = pressedColor != normalColor;

        final Spring spring = springSystem1.createSpring();
        spring.setCurrentValue(1);
        spring.addListener(new SimpleSpringListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onSpringUpdate(Spring spring) {
                final float currentValue = (float) spring.getCurrentValue();
                button.setScaleX(currentValue);
                button.setScaleY(currentValue);
                if (colors) {
                    button.setBackgroundColor((Integer) argbEvaluator.evaluate(Math.min(1f, currentValue),
                                                                               pressedColor,
                                                                               normalColor));
                }
            }
        });


        button.setOnTouchListener(new View.OnTouchListener() {
            Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (rect == null) {
                    rect = new Rect(button.getLeft(), button.getTop(), button.getRight(), button.getBottom());
                }
                final int actionMasked = event.getActionMasked();
                if (actionMasked == MotionEvent.ACTION_DOWN) {
                    spring.setEndValue(endValue);
                } else if (actionMasked == MotionEvent.ACTION_CANCEL) {
                    spring.setEndValue(1);
                } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        // Log.d("ACTION_MOVE - outside");
                        spring.setEndValue(1);
                    } else {
                        // Log.d("ACTION_MOVE - inside");
                        spring.setEndValue(endValue);
                    }
                } else if (actionMasked == MotionEvent.ACTION_UP) {
                    spring.setEndValue(1);
                    if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        // Log.d("ACTION_UP - inside");
                        button.performClick();
                    }
                } else {
                    return false;
                }
                return true;
            }
        });
        return spring;
    }
}
