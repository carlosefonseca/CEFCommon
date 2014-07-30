package com.carlosefonseca.common.utils;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

public class SpringUtils {

    public static Spring setSpringTouch(SpringSystem springSystem1, final View button) {
        final Spring spring = springSystem1.createSpring();
        spring.setCurrentValue(1);
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                final float currentValue = (float) spring.getCurrentValue();
                button.setScaleX(currentValue);
                button.setScaleY(currentValue);
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
                    spring.setEndValue(0.8);
                } else if (actionMasked == MotionEvent.ACTION_CANCEL) {
                    spring.setEndValue(1);
                } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        // Log.d("ACTION_MOVE - outside");
                        spring.setEndValue(1);
                    } else {
                        // Log.d("ACTION_MOVE - inside");
                        spring.setEndValue(0.8);
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
