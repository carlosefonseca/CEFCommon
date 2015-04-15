package com.carlosefonseca.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.carlosefonseca.common.CFApp;
import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.lang.Runtime.getRuntime;

public final class CodeUtils {

    //    public static final Pattern packageNameRegex = Pattern.compile(".+\\.([^.]+\\.).+");
    public static final Pattern packageNameRegex = Pattern.compile("\\.");
    private static final String TAG = CodeUtils.getTag(CodeUtils.class);
    public static final String SIDE_T = "├─";
    public static final String LONG_L = "└─";

    private CodeUtils() {}

    public static void hideKeyboard(View input) {
        //noinspection ConstantConditions
        InputMethodManager imm = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public static void recreateActivity(Activity a) {
        Log.d(TAG, "Recreating Activity.");
        if (Build.VERSION.SDK_INT >= 11) {
            a.recreate();
        } else {
            Intent intent = a.getIntent();
            a.finish();
            a.startActivity(intent);
        }
    }

    public static String getTag(Class clazz) {
        return packageNameRegex.split(clazz.getName(), 3)[1] + "." + clazz.getSimpleName();
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static String separator(final String text) {return "════════ " + text + " ════════";}

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    public static int hashCode(int seed, Object... objects) {
        if (objects == null) return seed;
        int hashCode = seed;
        for (Object element : objects) {
            hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void setupWebView(WebView webView) {
        final Context context = webView.getContext();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setDatabasePath(context.getFilesDir() + "/databases/");
        }
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) webSettings.setDisplayZoomControls(false);
        webView.zoomOut();
        webSettings.setGeolocationDatabasePath(context.getFilesDir() + "/databases/");

        // Force links and redirects to open in the WebView instead of in a browser
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {
                    view.loadUrl(url);
                } else {
                    UrlUtils.tryStartIntentForUrl(context, url);
                }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
    }

    static DecimalFormat formatter;

    static {
        formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols decimalFormatSymbols = formatter.getDecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static String getKB(long bytes) {
        return formatter.format(bytes / 1024);
    }

    protected static long getFreeMem() {
        Runtime r = getRuntime();
        long freeMem = r.maxMemory() - r.totalMemory() + r.freeMemory();
        long freeMemLess = Math.max(0, freeMem - (2 * 1024 * 1024));
        Log.d(TAG,
              "getFreeMem: Memory usage: %s ( %s / %s ) kB. Reporting only: %s kB",
              getKB(freeMem),
              getKB(r.totalMemory() - r.freeMemory()),
              getKB(r.maxMemory()),
              getKB(freeMemLess));
        return freeMemLess;
    }

    public static String getTimespan(long uptimeMillisOnStart) {
        double secs = (SystemClock.uptimeMillis() - uptimeMillisOnStart) / 1000d;
        if (secs < 10) {
            return String.format("%.03fs", secs);
        } else if (secs < 60) {
            return String.valueOf((int) secs) + "s";
        } else {
            return "" + (secs / 60) + "m " + (secs % 60) + "s";
        }
    }

    public interface RunnableWithView<T extends View> {
        void run(T view);
    }

    public static <T extends View> void runOnGlobalLayout(final T view, final RunnableWithView<T> runnable) {
        try {
            assert view.getViewTreeObserver() != null;
            if (view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi") // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        runnable.run(view);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public static void runOnGlobalLayout(final View view, final Runnable runnable) {
        try {
            assert view.getViewTreeObserver() != null;
            if (view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi") // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        runnable.run();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void setupNumericEditText(final AlertDialog dialog,
                                            final EditText editText,
                                            @Nullable final DialogInterface.OnClickListener onDone) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (onDone != null &&
                    (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                     event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // the user is done typing.
                    onDone.onClick(dialog, 0);
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
    }


    @SuppressLint("InlinedApi")
    public static void setupNumericEditText(final EditText editText,
                                            @Nullable final DialogInterface.OnClickListener onDone) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (onDone != null &&
                    (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                     event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // the user is done typing.
                    onDone.onClick(null, 0);
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
    }

    @SuppressLint("InlinedApi")
    public static void setupNumericEditText(final EditText editText, @Nullable final View.OnClickListener onDone) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (onDone != null &&
                    (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                     event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // the user is done typing.
                    onDone.onClick(editText);
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
    }

    @SuppressLint("InlinedApi")
    public static void setupNumericEditText(final EditText editText, @Nullable final Runnable runnable) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (runnable != null &&
                    (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                     event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // the user is done typing.
                    runnable.run();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
    }

    public static boolean isGpsOn() {
        return Settings.Secure.isLocationProviderEnabled(CFApp.getContext().getContentResolver(),
                                                         LocationManager.GPS_PROVIDER);
    }

    /**
     * Creates a TouchListener that repeats a specified action as long as the view is being pressed.
     *
     * @param runnable    The code to be repeated.
     * @param delayMillis The interval between repetitions.
     * @return A new OnTouchListener setup to handle long press to repeat.
     */
    public static View.OnTouchListener getRepeatActionListener(final Runnable runnable, final int delayMillis) {
        return getRepeatActionListener(runnable, delayMillis, null);
    }

    /**
     * Creates a TouchListener that repeats a specified action as long as the view is being pressed.
     *
     * @param runnable     The code to be repeated.
     * @param delayMillis  The interval between repetitions.
     * @param onUpRunnable Code to run when the button is no longer being pressed.
     * @return A new OnTouchListener setup to handle long press to repeat.
     */
    public static View.OnTouchListener getRepeatActionListener(final Runnable runnable,
                                                               final int delayMillis,
                                                               @Nullable final Runnable onUpRunnable) {
        final Handler mHandler = new Handler();
        final Runnable repeatRunnable = new Runnable() {
            @Override
            public void run() {
                runnable.run();
                mHandler.postDelayed(this, delayMillis);
            }
        };
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.removeCallbacks(repeatRunnable);
                        runnable.run();
                        mHandler.postDelayed(repeatRunnable, 200);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mHandler.removeCallbacks(repeatRunnable);
                        if (onUpRunnable != null) onUpRunnable.run();
                        break;
                }
                return false;
            }
        };
    }

    /**
     * Computes the md5 hex hash for the specified string
     *
     * @param s The string to hash.
     * @return The hexadecimal hash.
     */
    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e);
        }
        return "";
    }

    private static String versionName;

    public static String getAppVersionName() {
        if (versionName == null) {
            versionName = "";
            try {
                //noinspection ConstantConditions
                PackageInfo pInfo = CFApp.getContext()
                                         .getPackageManager()
                                         .getPackageInfo(CFApp.getContext().getPackageName(), 0);
                versionName = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Exception", e);
            }
        }
        return versionName;
    }

    private static int versionCode = -1;

    public static int getAppVersionCode() {
        if (versionCode == -1) {
            try {
                //noinspection ConstantConditions
                PackageInfo pInfo = CFApp.getContext()
                                         .getPackageManager()
                                         .getPackageInfo(CFApp.getContext().getPackageName(), 0);
                versionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Exception", e);
            }
        }
        return versionCode;
    }

    public static void runOnBackground(final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {

            @Nullable
            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }
        }.execute();
    }

    public static void pauseRunningAppForDebugPurposesDontLeaveThisHangingAround() {
        if (CFApp.isTestDevice()) {
            Log.w(TAG, "#### PAUSED ####");
            try {
                boolean block = true;
                //noinspection ConstantConditions
                while (block) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }

    public static void toast(final String message) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CFApp.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static void ltoast(final String message) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CFApp.getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static void runOnUIThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static void setVisibility(int visibility, View...views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }
}