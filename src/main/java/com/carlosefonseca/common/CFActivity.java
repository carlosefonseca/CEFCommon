package com.carlosefonseca.common;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
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
public class CFActivity extends FragmentActivity {

    private static final String TAG = getTag(CFActivity.class);
    protected boolean canRegisterRunnables;
    protected boolean registered;
    protected SystemBarTintManager tintManager;
    protected LoadingDialog dialog;
    private static WeakReference<CFActivity> latestActivity;

    @Nullable
    public static CFActivity getLatestActivity() {
        final CFActivity activity = latestActivity != null ? latestActivity.get() : null;
        if (activity == null) {
            Log.w(TAG, "Latest Activity not found!");
        }
        return activity;
    }

    @Nullable
    public static LoadingDialog getDialogOnLatestActivityIfExists() {
        CFActivity activity = getLatestActivity();
        if (activity == null) {
            Log.d(TAG + ".getDialog", "No activity");
            return null;
        }
        return activity.getLoadingDialogIfExists();
    }

    @Nullable
    public static LoadingDialog getDialogOnLatestActivity() {
        CFActivity activity = getLatestActivity();
        if (activity == null) {
            Log.d(TAG + ".getDialog", "No activity");
            return null;
        }
        return activity.getLoadingDialog();
    }

    public LoadingDialog getLoadingDialog() {
        if (dialog == null) {
            Log.d(TAG + ".getDialog", "Creating Dialog");
            dialog = new LoadingDialog(this, "");
        }
        return dialog;
    }

    public LoadingDialog getLoadingDialogIfExists() {
        return dialog;
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        setLatestActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) dialog.dismiss();
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
        clearLatestActivityIfSame();
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
        public abstract void run(Activity activity);
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
            public void run(Activity activity) {
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
