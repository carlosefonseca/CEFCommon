package com.carlosefonseca.common;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.carlosefonseca.common.utils.ActivityStateListener;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.widgets.LoadingDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import de.greenrobot.event.EventBus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.CodeUtils.isMainThread;

/**
 * Base Activity with a lot of good stuff.
 */
public class CFActivity extends FragmentActivity implements ActivityStateListener.ActivityStateListenerProvider {

    private static final String sTAG = getTag(CFActivity.class);
    public static final int REQUEST_EXIT = -1234;
    public static final int REQUEST_EXIT_ALL = -12345;
    protected final String TAG = getTag(this.getClass());
    protected boolean canRegisterRunnables;
    protected boolean registered;
    protected SystemBarTintManager tintManager;
    protected WeakReference<LoadingDialog> dialog;
    private static WeakReference<CFActivity> latestActivity;
    @Nullable private ActivityStateListener mActivityStateListener;

    @Override
    public void onBackPressed() {
        if (mActivityStateListener != null) {
            if (!mActivityStateListener.onBackPressed()) super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mActivityStateListener != null) mActivityStateListener.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mActivityStateListener != null) mActivityStateListener.onDestroy();
        mActivityStateListener = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivityStateListener != null) mActivityStateListener.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case REQUEST_EXIT:
                Log.i(TAG + " (" + sTAG + ")", "Exit Requested!");
                finish();
                break;
            case REQUEST_EXIT_ALL:
                Log.i(TAG + " (" + sTAG + ")", "Exit All Requested!");
                setResult(REQUEST_EXIT_ALL);
                finish();
                break;
        }
        if (mActivityStateListener != null) mActivityStateListener.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    public static CFActivity getLatestActivity() {
        final CFActivity activity = latestActivity != null ? latestActivity.get() : null;
        if (activity == null) {
            Log.w(sTAG, "Latest Activity not found!");
        }
        return activity;
    }

    @Nullable
    public static LoadingDialog getDialogOnLatestActivityIfExists() {
        CFActivity activity = getLatestActivity();
        if (activity == null) {
            Log.d(sTAG + ".getDialog", "No activity");
            return null;
        }
        return activity.getLoadingDialogIfExists();
    }

    @Nullable
    public static LoadingDialog getDialogOnLatestActivity() {
        CFActivity activity = getLatestActivity();
        if (activity == null) {
            Log.d(sTAG + ".getDialog", "No activity");
            return null;
        }
        return activity.getLoadingDialog();
    }

    public LoadingDialog getLoadingDialog() {
        if (dialog == null || dialog.get() == null) {
            Log.d(TAG + ".getDialog", "Creating Dialog");
            dialog = new WeakReference<>(new LoadingDialog(this, ""));
        }
        return dialog.get();
    }

    @Nullable
    public LoadingDialog getLoadingDialogIfExists() {
        return dialog == null ? null : dialog.get();
    }

    @Override
    public ActivityStateListener getActivityStateListener() {
        if (mActivityStateListener == null) {
            mActivityStateListener = new ActivityStateListener(this);
        }
        return mActivityStateListener;
    }

    /**
     * When running on KitKat, tints the system bar.
     *
     * @param color Color to tint.
     */
    protected void tintStatusBar(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (tintManager == null) {
                tintManager = new SystemBarTintManager(this);
            }
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(color);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //noinspection ObjectEquality
        setLatestActivity();
        if (canRegisterRunnables && !registered) {
            EventBus.getDefault().registerSticky(this, RunnableOnActivity.class, RunnableOnActivityWrapper.class);
            registered = true;
        }
        if (mActivityStateListener != null) mActivityStateListener.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActivityStateListener != null) mActivityStateListener.onResume();
        setLatestActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.get() != null) dialog.get().dismiss();
        if (isFinishing()) dialog = null;
        if (mActivityStateListener != null) mActivityStateListener.onStop();
        clearLatestActivityIfSame();
    }

    private void setLatestActivity() {
        if (latestActivity == null || latestActivity.get() != this) latestActivity = new WeakReference<>(this);
    }

    private void clearLatestActivityIfSame() {
        if (latestActivity != null && latestActivity.get() == this) latestActivity = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (registered) {
            EventBus.getDefault().unregister(this, RunnableOnActivity.class, RunnableOnActivityWrapper.class);
            registered = false;
        }
//        clearLatestActivityIfSame();
        if (mActivityStateListener != null) mActivityStateListener.onPause();
    }

    void onEventMainThread(RunnableOnActivity runnable) {
        EventBus.getDefault().removeStickyEvent(runnable);
        runnable.run(this);
    }

    void onEventMainThread(RunnableOnActivityWrapper runnable) {
        EventBus.getDefault().removeStickyEvent(runnable);
        runnable.runnableOnActivity.run(this);
    }

    protected String s(int res) {
        return getResources().getString(res);
    }

    protected void hideKeyboard() {
        getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    public static abstract class RunnableOnActivity {
        public abstract void run(CFActivity activity);
    }

    protected static class RunnableOnActivityWrapper {
        protected final RunnableOnActivity runnableOnActivity;

        RunnableOnActivityWrapper(RunnableOnActivity runnableOnActivity) {
            this.runnableOnActivity = runnableOnActivity;
        }
    }

    public static void post(RunnableOnActivity runnableOnActivity) {
        EventBus.getDefault().postSticky(new RunnableOnActivityWrapper(runnableOnActivity));
    }

    public static RunnableOnActivity makeToast(final String message, final int time) {
        return new RunnableOnActivity() {

            @Override
            public void run(CFActivity activity) {
                Toast.makeText(activity, message, time).show();
            }
        };
    }

    public static RunnableOnActivity makeToast(String message) {
        return makeToast(message, Toast.LENGTH_SHORT);
    }

    public static void Toast(final String message) {
        final CFActivity activity = latestActivity != null ? latestActivity.get() : null;
        if (activity != null) {
            if (isMainThread()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
