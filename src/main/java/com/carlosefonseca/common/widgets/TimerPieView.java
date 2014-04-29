package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

/**
 * Extends the {@link PieView} in a way that can be used as a Timer.
 * <p/>
 * Use {@link #setupAsTimer(int, Runnable)} to set the time and an action to run when the time runs out and it will start
 * counting and updating the pie accordingly, at 50fps.
 * <p/>
 * The timer can be canceled by calling {@link #cancelTimer()}.
 */
public class TimerPieView extends PieView {
    protected boolean canceled = false;
    protected Handler handler;
    protected long start;

    private int millis;
    private Runnable onFinished;

    public TimerPieView(Context context) {
        super(context);
    }

    public TimerPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerPieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setupAsTimer(int millis, Runnable onFinished) {
        this.millis = millis;
        this.onFinished = onFinished;

        if (handler == null) handler = new Handler();
        handler.removeCallbacksAndMessages(null);

        start = System.currentTimeMillis();
        tick.run();
    }

    public void cancelTimer() {
        canceled = true;
    }

    Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (canceled) {
                canceled = false;
                return;
            }
            double percentage = (System.currentTimeMillis() - start) * 1.0 / millis;
            if (percentage <= 1) {
                updateTo((float) percentage);
                handler.postDelayed(tick, 20); // 50fps
            } else {
                updateTo(1);
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        }
    };
}
