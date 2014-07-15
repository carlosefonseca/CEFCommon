package com.carlosefonseca.common.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.security.InvalidParameterException;

/**
 * Detects triple taps. Uses a mix of tap and double tap detection to figure out when a triple tap occurs.
 * This implementation is very simple, at least when compared with the {@link android.view.GestureDetector}, which I tried to
 * extend from but its complexity was annoying.
 * <p/>
 * Works fine when mixed with {@link android.view.View.OnLongClickListener}.
 * No idea how to integrate this with a {@link android.view.View.OnClickListener} or
 * {@link android.view.GestureDetector.OnDoubleTapListener}.
 *
 * Looks like you need to set {@link View#setClickable(boolean)} to true on views that aren't buttons.
 */
@SuppressWarnings("ConstantConditions")
public class TripleTapTouchListener implements View.OnTouchListener {
    private static final String TAG = CodeUtils.getTag(TripleTapTouchListener.class);
    final boolean log = false;

    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final int DOUBLE_TAP_MIN_TIME = 40;//ViewConfiguration.getDoubleTapMinTime();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();

    private final OnTripleTapListener mTripleTapListener;
    private final int mTouchSlopSquare;
    private int mDownCount;
    private int mUpCount;

    private boolean isDown;
    private final Object lock = new Object();

    private MotionEvent[] mDowns = new MotionEvent[3];
    private MotionEvent[] mUps = new MotionEvent[3];


    /**
     * The listener that is used to notify when a triple-tap occurs.
     */
    public interface OnTripleTapListener {
        /**
         * Notified when an event within a triple-tap gesture occurs.
         *
         * @param e The first motion event that occurred during the triple-tap gesture.
         * @return true if the event is consumed, else false
         */
        boolean onTripleTapEvent(MotionEvent e);
    }


    public TripleTapTouchListener(Context context, OnTripleTapListener tripleTapListener) {
        if (tripleTapListener == null) {
            throw new InvalidParameterException("Listener is null");
        }
        mTripleTapListener = tripleTapListener;

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        int touchSlop = configuration.getScaledDoubleTapSlop();
        mTouchSlopSquare = touchSlop * touchSlop;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        synchronized (lock) {
            final int action = ev.getAction();

            if (log) Log.v(TAG, "ENTER mDown: " + mDownCount + " mUp: " + mUpCount);

            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (isDown) {
                        if (log) Log.v(TAG, "DOWN: is down");
                        return false;
                    }
                    isDown = true;

                    // if first, store
                    if (mDownCount == 0) {
                        if (log) Log.v(TAG, "Touch DOWN: " + mDownCount);
                        mDowns[0] = MotionEvent.obtain(ev);
                        mDownCount++;
                    } else {
                        // check if double tap
                        if (isConsideredDoubleTap(mDowns[mDownCount - 1], mUps[mUpCount - 1], ev)) {
                            if (log) Log.v(TAG, "Touch DOWN: " + mDownCount + " IS DOUBLE TAP");
                            mDowns[mDownCount] = MotionEvent.obtain(ev);
                            mDownCount++;
                        } else {
                            cancel();
                            mDowns[0] = MotionEvent.obtain(ev);
                            mDownCount = 1;
                        }
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    if (!isDown) {
                        if (log) Log.v(TAG, "UP: is up");
                        return false;
                    }
                    isDown = false;

                    // this up is a tap?
                    if (isConsideredTap(mDowns[mDownCount - 1], ev)) {
                        if (log) Log.v(TAG, "Touch UP  : " + mUpCount + " IS TAP");
                        if (mUpCount >= 2) {
                            if (log) Log.v(TAG, "TRIPLE TAP!");
                            MotionEvent mDown = mDowns[0];
                            mDowns[0] = null;
                            cancel();
                            return mTripleTapListener.onTripleTapEvent(mDown);
                        } else {
                            mUps[mUpCount] = MotionEvent.obtain(ev);
                            mUpCount++;
                        }

                    } else {
                        cancel();
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    cancel();
                    break;
            }
            if (log) Log.v(TAG, "EXIT mDown: " + mDownCount + " mUp: " + mUpCount);

            return false;
        }
    }

    private void cancel() {
        if (log) Log.v(TAG, "CANCEL");
        mUpCount = 0;
        mDownCount = 0;
        MotionEvent event;
        for (int i = 0; i < 3; i++) {
            event = mUps[i];
            if (event != null) event.recycle();
            mUps[i] = null;

            event = mDowns[i];
            if (event != null) event.recycle();
            mDowns[i] = null;
        }
    }

    private int dist(MotionEvent e1, MotionEvent e2) {
        int deltaX = (int) e1.getX() - (int) e2.getX();
        int deltaY = (int) e1.getY() - (int) e2.getY();

        return deltaX * deltaX + deltaY * deltaY;
    }

    private boolean isConsideredTap(MotionEvent down, MotionEvent up) {
        long delta = up.getEventTime() - down.getEventTime();

        if (delta > TAP_TIMEOUT) {
            if (log) Log.v(TAG, "Not a tap - time");
            return false;
        }

        boolean b = dist(down, up) < mTouchSlopSquare;
        //noinspection PointlessBooleanExpression
        if (log && !b) Log.v(TAG, "Not a tap - distance");
        return b;
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            if (log) Log.v(TAG, "Not a double tap - time");
            return false;
        }

        boolean b = dist(firstDown, secondDown) < mTouchSlopSquare;
        //noinspection PointlessBooleanExpression
        if (log && !b) Log.v(TAG, "Not a double tap - distance");
        return b;
    }

}
