package com.carlosefonseca.common.utils;

import android.widget.Toast;
import com.carlosefonseca.common.BuildConfig;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

import static android.util.Log.*;
import static com.carlosefonseca.apache.commons.lang3.StringUtils.defaultString;
import static com.carlosefonseca.apache.commons.lang3.StringUtils.isNotEmpty;

@SuppressWarnings("UnusedDeclaration")
public final class Log {

    private Log() {}

    public interface RemoteLogger {
        public void setUserEmail(String email);

        public void put(String key, String value);

        public void put(String key, boolean value);

        public void put(String key, double value);

        public void put(String key, int value);

        public boolean log(int priority, String tag, String message, @Nullable Throwable tr);
    }

    private static RemoteLogger remoteLogger;

    public static int sRemoteMinPriority = INFO;

    private static boolean consoleLogging = BuildConfig.DEBUG;

    public static boolean isConsoleLogging() {
        return consoleLogging;
    }

    public static void setConsoleLogging(boolean consoleLogging) {
        Log.consoleLogging = consoleLogging;
    }

    public static void setRemoteLogger(RemoteLogger remoteLogger) {
        Log.remoteLogger = remoteLogger;
    }

    public static void setRemoteLogger(RemoteLogger remoteLogger, int minPriority) {
        Log.remoteLogger = remoteLogger;
        sRemoteMinPriority = minPriority;
    }


    public static void setUserEmail(String email) {
        if (remoteLogger != null) remoteLogger.setUserEmail(email);
        android.util.Log.i(tag(1), "User Email: " + email);
    }

    public static void put(String key, boolean value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    public static void put(String key, int value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    public static void put(String key, long value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    public static void put(String key, String value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    public static void put(String tag, String key, String value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag, key + ": " + value);
    }

    public static void put(String key, float value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    public static void put(String key, double value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(tag(1), key + ": " + value);
    }

    /**
     * Logs something to remote logger or to console.
     *
     * @param priority One of {@link android.util.Log}.
     * @param tag      Tag for log.
     * @param frm      Text to log or a format string.
     * @param args     Arguments for the format string and/or a throwable to log (throwable must be last argument)
     * @return The priority.
     */
    private static int log(int priority, String tag, String frm, @Nullable Object... args) {
        boolean logged = false;

        // last arg is a throwable?
        Throwable tr = ((args != null) && (args.length > 0) && (args[args.length - 1] instanceof Throwable))
                       ? (Throwable) args[args.length - 1]
                       : null;


        if ((remoteLogger != null && priority >= sRemoteMinPriority) || consoleLogging ||
            priority >= android.util.Log.ERROR) {

            // create a message, either with format and args, or just format, of with fallback string.
            String msg = defaultString(isNotEmpty(frm) && args != null && args.length - (tr != null ? 1 : 0) > 0
                                       ? String.format(frm, args)
                                       : frm, "<no message given>");

            // optional remote log
            if (remoteLogger != null && priority >= sRemoteMinPriority) {
                logged = remoteLogger.log(priority, tag, msg, tr);
            }

            // if didn't remote log but consoleLogging enabled or error, do a console log
            if (!logged && (consoleLogging || priority >= android.util.Log.ERROR)) {
                // log to console using plain android Log.
                android.util.Log.println(priority, tag, msg);
                if (tr != null) android.util.Log.println(priority, tag, getStackTraceString(tr));
            }
        }
        return priority;
    }

    // AUTO TAGGED

    public static int v() {
        return sRemoteMinPriority <= VERBOSE || consoleLogging ? v(tagP(), "<-") : -1;
    }
    public static int v(String msg) {
        return sRemoteMinPriority <= VERBOSE || consoleLogging ? v(tagP(), msg) : -1;
    }

    public static int d() {
        return sRemoteMinPriority <= DEBUG || consoleLogging ? d(tagP(), "<-") : -1;
    }

    public static int d(String msg) {
        return sRemoteMinPriority <= DEBUG || consoleLogging ? d(tagP(), msg) : -1;
    }

    public static int i(String msg) {
        return sRemoteMinPriority <= INFO || consoleLogging ? i(tagP(), msg) : -1;
    }

    public static int w(String msg) {
        return sRemoteMinPriority <= WARN || consoleLogging ? w(tagP(), msg) : -1;
    }

    public static int e(String msg) {
        return sRemoteMinPriority <= ERROR || consoleLogging ? e(tagP(), msg, (Object[])null) : -1;
    }

    public static int wtf(String msg) {
        return sRemoteMinPriority <= ASSERT || consoleLogging ? wtf(tagP(), msg) : -1;
    }

    // TAG GIVEN

    public static int v(String tag, String msg) {
        return v(tag, msg, (Object[]) null);
    }

//    public static int v(String tag, String msg, @Nullable Throwable tr) {
//        return log(VERBOSE, tag, msg, tr);
//    }

    public static int v(String tag, String f, Object... args) {
        return log(VERBOSE, tag, f, args);
    }

    public static int d(String tag, String msg) {
        return log(DEBUG, tag, msg, (Object[]) null);
    }

    public static int d(String tag, String msg, Object... args) {
        return log(DEBUG, tag, msg, args);
    }

    public static int i(String tag, String msg) {
        return log(INFO, tag, msg, (Object[]) null);
    }

    public static int i(String tag, String msg, Object... args) {
        return log(INFO, tag, msg, args);
    }

    public static int w(String tag, String msg) {
        return log(WARN, tag, msg, (Object[]) null);
    }

    public static int w(String tag, String msg, Object... args) {
        return log(WARN, tag, msg, args);
    }

    public static int w(String tag, Throwable tr) {
        return log(WARN, tag, tr.getMessage(), tr);
    }

    public static int e(String tag, String msg) {
        return log(ERROR, tag, "", new RuntimeException(msg));
    }

    public static int e(String tag, String msg, Object... args) {
        return log(ERROR, tag, msg, args);
    }

    public static int e(String tag, Throwable tr) {
        return log(ERROR, tag, tr.getMessage(), tr);
    }

    public static int wtf(String tag, String msg) {
        return log(ASSERT, tag, msg, (Object[]) null);
    }

    public static int wtf(String tag, String msg, Object... args) {
        return log(ASSERT, tag, msg, args);
    }

    public static int wtf(String tag, Throwable tr) {
        return log(ASSERT, tag, "WTF", tr);
    }

    public static boolean isLoggable(String s, int i) {
        return android.util.Log.isLoggable(s, i);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    public static void t(String message) {
        Toast.makeText(CFApp.getContext(), message, Toast.LENGTH_SHORT).show();
        i(tag(1), message);
    }


    static Pattern sSplitRegEx = Pattern.compile("\\.");
    public static String tag(int i) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3 + i];
        String[] split = sSplitRegEx.split(stackTraceElement.getClassName());
        return String.format("%s.%s:%d %s()",
                             split[1],
                             split[split.length - 1],
                             stackTraceElement.getLineNumber(),
                             stackTraceElement.getMethodName());
    }

    public static String tag() {
        return tag(1);
    }

    public static String tagP() {
        return tag(2);
    }

    public static void crash() {
        throw new RuntimeException("Crash Test");
    }
}
