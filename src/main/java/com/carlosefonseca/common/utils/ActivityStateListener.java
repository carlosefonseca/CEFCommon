package com.carlosefonseca.common.utils;

import android.app.Activity;

import java.util.HashSet;
import java.util.Set;

public class ActivityStateListener {

    protected Set<Interface> mListeners = new HashSet<>();

    protected Activity mActivity;
    private boolean mRunning;

    public ActivityStateListener(Activity activity) {
        this.mActivity = activity;
    }

    public void addListener(Interface listener) {
        mListeners.add(listener);
    }

    public void addListenerAndNotify(Interface listener) {
        mListeners.add(listener);
        if (mRunning) listener.onStart();
    }

    public void removeListener(Interface listener) {
        mListeners.remove(listener);
    }

    public void removeAndStopListener(Interface listener) {
        listener.onStop(true);
        mListeners.remove(listener);
    }

    public void onStart() {
        if (!mRunning) {
            mRunning = true;
            for (Interface mListener : mListeners) {
                mListener.onStart();
            }
        }
    }

    public void onStop() {
        final boolean finishing = mActivity.isFinishing();
        if (mRunning || finishing) {
            mRunning = false;
            for (Interface mListener : mListeners) mListener.onStop(finishing);
            if (finishing) mListeners.clear();
        }
    }

    public interface Interface {
        public void onStart();

        public void onStop(boolean isFinishing);
    }

    public static class SimpleInterface implements Interface {
        @Override
        public void onStart() {

        }

        @Override
        public void onStop(boolean isFinishing) {

        }
    }
}
