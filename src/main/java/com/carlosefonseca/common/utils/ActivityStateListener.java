package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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


    public void onCreate(Bundle savedInstanceState) {
        for (Interface mListener : mListeners) mListener.onCreate(savedInstanceState);
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

    public void onPause() {
        for (Interface mListener : mListeners) mListener.onPause();
    }

    public void onResume() {
        for (Interface mListener : mListeners) mListener.onResume();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Interface mListener : mListeners) mListener.onActivityResult(requestCode, resultCode, data);
    }

    public void onSaveInstanceState(Bundle outState) {
        for (Interface mListener : mListeners) mListener.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        for (Interface mListener : mListeners) mListener.onDestroy();
    }

    /**
     * @return True if something handled the back.
     */
    public boolean onBackPressed() {
        for (Interface listener : mListeners) {
            if (listener.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    public interface Interface {
        public void onCreate(Bundle savedInstanceState);

        public void onStart();

        public void onStop(boolean isFinishing);

        public void onPause();

        public void onResume();

        public void onActivityResult(int requestCode, int resultCode, Intent data);

        public void onSaveInstanceState(Bundle outState);

        public void onDestroy();

        /**
         * @return True if the back was handler, false if was ignored.
         */
        public boolean onBackPressed();
    }

    public static class SimpleInterface implements Interface {
        @Override
        public void onCreate(Bundle savedInstanceState) { }

        @Override
        public void onStart() { }

        @Override
        public void onStop(boolean isFinishing) { }

        @Override
        public void onPause() { }

        @Override
        public void onResume() { }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) { }

        @Override
        public void onSaveInstanceState(Bundle outState) { }

        @Override
        public void onDestroy() { }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onBackPressed() { return false; }
    }

    public static interface ActivityStateListenerProvider {
        ActivityStateListener getActivityStateListener();
    }
}
