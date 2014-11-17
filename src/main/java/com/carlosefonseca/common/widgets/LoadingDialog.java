package com.carlosefonseca.common.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.CodeUtils.isMainThread;

/**
 * Creates popup dialogs for long, blocking operations.
 * <p/>
 * This operations can have an indeterminate timing by displaying a spinner
 * animation or determinate timing, by displaying a pie animation (these are the "loading popups").
 * <p/>
 * It also displays popups for success and error that can be used after the operation or my themselves and can be auto
 * dismissible, as well as run something at the end (these are the "success/error popups").
 * <p/>
 * The loading popups block the application. The success/error popups allow the user to tap on the screen and dismiss them
 * quickly.
 *
 * Note: Requires the following resources:<br/>
 * - PieView<br/>
 * - R.layout.bw_loading_dialog<br/>
 * - R.style.DialogTranslucent<br/>
 * - R.drawable.check<br/>
 * - R.drawable.cross
 */
public class LoadingDialog extends Dialog {

    private static final String TAG = getTag(LoadingDialog.class);

    private static final long POPUP_DELAY_MILLIS = 1500;
    private static final long ERROR_DELAY_MILLIS = 2000;
    public static boolean defaultDelegateBackToActivity = true;

    private DialogType currentType;
    public final PieView pie;
    protected Handler handler = new Handler(Looper.getMainLooper());
    private TextView message_tv;
    private ImageView image;
    private View progressView;
    private Activity activity;
    private Runnable onStopRunnable;
    private boolean cancelable = true;
    private boolean delegateBackToActivity = defaultDelegateBackToActivity;
    private RuntimeException timeoutCounting;
    private Runnable timeoutRunnable;

    /**
     * Convenience static method to display a Success message and auto dismiss.
     *
     * @param context Current context.
     * @param message Message to display.
     */
    public static void SuccessDialog(Context context, String message) {
        SuccessDialog(context, message, null);
    }

    /**
     * Convenience static method to display a Success message and auto dismiss.
     *
     * @param context  Current context.
     * @param message  Message to display.
     * @param runnable A runnable to execute when the dialog is dismissed.
     */
    public static void SuccessDialog(Context context, String message, @Nullable Runnable runnable) {
        LoadingDialog dialog = new LoadingDialog(context, DialogType.COMPLETED, message);
        dialog.showAndDismiss(runnable);
    }

    /**
     * Convenience static method to display an Error message and auto dismiss.
     *
     * @param context Current context.
     * @param message Message to display.
     */
    public static void ErrorDialog(Context context, String message) {
        ErrorDialog(context, message, null);
    }

    /**
     * Convenience static method to display an Error message and auto dismiss.
     *
     * @param context  Current context.
     * @param message  Message to display.
     * @param runnable A runnable to execute when the dialog is dismissed.
     */
    public static void ErrorDialog(Context context, String message, @Nullable Runnable runnable) {
        LoadingDialog dialog = new LoadingDialog(context, DialogType.ERROR, message);
        dialog.showAndDismiss(runnable);
    }

    /**
     * Creates a new Loading Dialog. It is not displayed by default.
     *
     * @param context Current context.
     * @param message Message to display.
     */
    public LoadingDialog(Context context, String message) {
        this(context, DialogType.LOADING, message);
    }

    /**
     * Creates a new Loading Dialog that runs a specified operation in background and dissmisses the dialog when
     * finished.
     *
     * @param context  Current context.
     * @param message  Message to display.
     * @param callable A callable to be run in background.
     * @param <T>      Return of the callable. Ignored.
     */
    public <T> LoadingDialog(Context context, String message, Callable<T> callable) {
        this(context, message);
        show();
        Task.callInBackground(callable).continueWith(new Continuation<T, Void>() {
            @Override
            public Void then(Task<T> task) throws Exception {
                LoadingDialog.this.dismissNow();
                return null;
            }
        });
    }

    /**
     * Creates a new Loading Dialog. It is not displayed by default. This saves the activity and calls runOnUIThread to setText.
     *
     * @param context Activity to display in.
     * @param message Message to display.
     */
    public LoadingDialog(Activity context, String message) {
        this(context, DialogType.LOADING, message);
        this.activity = context;
    }

    /**
     * Creates a new Loading Dialog. Use {@link #showAndReturn()} to display the dialog and save the instance.
     *
     * @param context Current context.
     * @param type    The type for the dialog.
     * @param message Message to display.
     */
    public LoadingDialog(Context context, DialogType type, String message) {this(context, type, message, true);}

    /**
     * Creates a new Loading Dialog. Use {@link #showAndReturn()} to display the dialog and save the instance.
     *
     * @param context Current context.
     * @param type    The type for the dialog.
     * @param message Message to display.
     * @param fade
     */
    public LoadingDialog(Context context, DialogType type, String message, boolean fade) {
        super(context, R.style.DialogTranslucent);
        if (fade) {
            getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cf_loading_dialog);

        message_tv = (TextView) findViewById(R.id.loading_message);
        image = (ImageView) findViewById(R.id.image);
        progressView = findViewById(R.id.progressBar1);
        pie = (PieView) findViewById(R.id.pie);

        changeDialog(message, type);
    }

    /**
     * Sets the text without changing anything else.
     * @param message  The message to display.
     * @return This instance.
     */
    public LoadingDialog setText(final String message) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                message_tv.setText(message);
            }
        });
        return this;
    }

    @Deprecated
    public void setText(final String s, Activity activity) {
        if (this.activity == null) this.activity = activity;
        setText(s);
    }


    /**
     * Sets an existing instance to the {@link com.carlosefonseca.common.widgets.LoadingDialog.DialogType#COMPLETED} type, sets the message and
     * displays the dialog with auto dismissal.
     *
     * @param message  The message to display.
     * @return This instance.
     */
    public LoadingDialog showAndDismissSuccess(String message) {return showAndDismissSuccess(message, null);}

    /**
     * Sets an existing instance to the {@link com.carlosefonseca.common.widgets.LoadingDialog.DialogType#COMPLETED} type, sets the message and
     * displays the dialog with auto dismissal.
     *
     * @param message  The message to display.
     * @param runnable An optional runnable to be ran on dismissal.
     * @return This instance.
     */
    public LoadingDialog showAndDismissSuccess(String message, @Nullable Runnable runnable) {
        changeDialog(message, DialogType.COMPLETED);
        showAndDismiss(runnable);
        return this;
    }

    /**
     * Sets an existing instance to the {@link com.carlosefonseca.common.widgets.LoadingDialog.DialogType#ERROR} type, sets the message and
     * displays the dialog with auto dismissal.
     *
     * @param message  The message to display.
     * @return This instance.
     */
    public LoadingDialog showAndDismissError(String message) {return showAndDismissError(message, null);}

    /**
     * Sets an existing instance to the {@link com.carlosefonseca.common.widgets.LoadingDialog.DialogType#ERROR} type, sets the message and
     * displays the dialog with auto dismissal.
     *
     * @param message  The message to display.
     * @param runnable An optional runnable to be ran on dismissal.
     * @return This instance.
     */
    public LoadingDialog showAndDismissError(String message, @Nullable Runnable runnable) {
        changeDialog(message, DialogType.ERROR);
        showAndDismiss(runnable);
        return this;
    }

    /**
     * Sets an existing instance to the {@link com.carlosefonseca.common.widgets.LoadingDialog.DialogType#LOADING} type, sets the message
     * and displays the dialog.
     *
     * @param message The message to display.
     * @return This instance.
     */
    public LoadingDialog showLoading(String message) {
        changeDialog(message, DialogType.LOADING);
        show();
        return this;
    }


    public LoadingDialog changeDialog(String message, DialogType dialogType) {
        setupOverlayDialog(message, dialogType);
        return this;
    }

    private void setupOverlayDialog(String message, DialogType dialogType) {
            timeoutCounting = null;
        if (currentType != dialogType) {
            currentType = dialogType;
            switch (dialogType) {
                case COMPLETED:
                    pie.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.check);
                    progressView.setVisibility(View.INVISIBLE);
                    setCanceledOnTouchOutside(true);
                    setCancelable(true);
                    break;
                case ERROR:
                    pie.setVisibility(View.INVISIBLE);
                    progressView.setVisibility(View.INVISIBLE);
                    image.setImageResource(R.drawable.cross);
                    image.setVisibility(View.VISIBLE);
                    setCanceledOnTouchOutside(true);
                    setCancelable(true);
                    break;
                case LOADING:
                    pie.setVisibility(View.INVISIBLE);
                    progressView.setVisibility(View.VISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    setCancelable(false);
                    break;
                case PIE_LOADING:
                    pie.setVisibility(View.VISIBLE);
                    progressView.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    setCancelable(false);
                    break;
            }
        }
        message_tv.setText(message);
    }

    @Override
    public void setCancelable(boolean flag) {
        cancelable = flag;
        super.setCancelable(flag);
    }

    @Override
    public void onBackPressed() {
        if (cancelable) cancel();
        else if (delegateBackToActivity) activity.onBackPressed();
    }

    public void dismissNow() {
        dismissAfterStandardDelay(null);
    }

    private void dismissAfterStandardDelay(@Nullable final Runnable runnable) {
        onStopRunnable = runnable;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getWindow() != null) {
                        dismiss();
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }
        }, currentType == DialogType.ERROR ? ERROR_DELAY_MILLIS : POPUP_DELAY_MILLIS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (onStopRunnable != null) onStopRunnable.run();
    }

    /** Displays the dialog and auto dismisses. Intended to be used with Success/Error dialogs. */
    public void showAndDismiss() {
        showAndDismiss(null);
    }

    /**
     * Displays the dialog and auto dismisses. Intended to be used with Success/Error dialogs.
     *
     * @param runnable A runnable to execute when the dialog is dismissed.
     */
    public void showAndDismiss(@Nullable Runnable runnable) {
        show();
        dismissAfterStandardDelay(runnable);
    }

    /**
     * Displays the dialog and returns the instance. Does not auto dismiss. Intended for use with Loading dialogs.
     *
     * @return This instance.
     */
    public LoadingDialog showAndReturn() {
        show();
        return this;
    }

    /**
     * Updates the percentage of the pie.
     *
     * @param percentage The new percentage.
     */
    public void updatePie(double percentage) {
        updatePie(percentage, null);
    }

    /**
     * Updates the percentage of the pie and the text of the popup.
     *
     * @param percentage The new percentage.
     * @param message    The new text.
     */
    public void updatePie(double percentage, @Nullable String message) {
        pie.updateTo(percentage);
        if (message != null) {
            message_tv.setText(message);
        }
    }

    public void updatePieRelative(double v) {
        pie.relativeUpdate(v);
    }

    public void showError(final String message) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                changeDialog(message, DialogType.ERROR);
                show();
            }
        });
    }

    public void runOnUi(@NotNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else if (activity != null) {
            activity.runOnUiThread(runnable);
        } else {
            Log.w(TAG, new RuntimeException("activity is null"));
        }
    }

    /**
     * Sets a timeout for Indeterminate Loading dialogs. You should timeout your own code and change the dialog accordingly,
     * but if something is weird, this can be helpful... The timeout will only close the dialog if the timeout wasn't canceled
     * (changing the dialog cancels it). The timeout will also Log an exception reporting who called the setTimeout, to help debug
     * what's causing the problem. The exception is NOT thrown.
     *
     * @param millis timeout in milliseconds.
     */
    @Deprecated
    public void setTimeout(int millis) {
        if (true) return;
        timeoutCounting = new RuntimeException("Dialog was closed by timeout! Timeout set here");
        if (timeoutRunnable == null) {
            timeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    if (timeoutCounting != null && isShowing()) {
                        dismiss();
                        Log.w(TAG, timeoutCounting);
                    }
                    timeoutCounting = null;
                }
            };
        }
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, millis);
    }

    public void cancelTimeout() {
        handler.removeCallbacks(timeoutRunnable);
        timeoutRunnable = null;
    }

    public boolean hasTimeout() {
        return timeoutRunnable != null;
    }

    public static enum DialogType {
        LOADING, PIE_LOADING, ERROR, COMPLETED
    }
}