package com.carlosefonseca.common.utils;

import android.os.Handler;
import android.os.Looper;

public class RepeatWhileOnActivity extends ActivityStateListener.SimpleInterface {

    protected Runnable mRunnable;
    protected long mTime;

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean mRunning;

    public RepeatWhileOnActivity(Runnable runnable, long time) {
        this.mRunnable = runnable;
        this.mTime = time;
    }

    @Override
    public void onStart() {
        Log.v();
        if (!mRunning && mRunnable != null) {
            mRunning = true;
            schedule();
        }
    }

    @Override
    public void onStop(boolean isFinishing) {
        Log.v();
        if (mRunning) {
            mRunning = false;
            unschedule();
        }
        if (isFinishing) {
            unschedule();
            mRunnable = null;
            mHandler = null;
        }
    }

    private void unschedule() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void schedule() {
        mHandler.postDelayed(mInnerRunnable, mTime);
    }

    protected Runnable mInnerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                mRunnable.run();
                schedule();
            }
        }
    };

}
