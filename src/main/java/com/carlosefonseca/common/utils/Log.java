package com.carlosefonseca.common.utils;

import android.widget.Toast;
import com.carlosefonseca.common.CFApp;

import java.util.regex.Pattern;

import static android.util.Log.*;

@SuppressWarnings("UnusedDeclaration")
public final class Log {

    private Log() {}

    public interface RemoteLogger {
        public void setUserEmail(String email);

        public void put(String key, String value);

        public void put(String key, boolean value);

        public void put(String key, double value);

        public void put(String key, int value);

        public boolean log(int priority, String tag, String message, Throwable tr);
    }

    private static RemoteLogger remoteLogger;

    public static int sRemoteMinPriority = INFO;

    private static boolean consoleLogging;

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
        android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), "User Email: " + email);
    }

    public static void put(String key, boolean value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    public static void put(String key, int value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    public static void put(String key, long value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    public static void put(String key, String value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    public static void put(String key, float value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    public static void put(String key, double value) {
        if (remoteLogger != null) remoteLogger.put(key, value);
        if (consoleLogging) android.util.Log.i(Thread.currentThread().getStackTrace()[1].getClassName(), key + ": " + value);
    }

    private static int log(int priority, String tag, String msg, Throwable tr) {
        if (remoteLogger == null || priority < sRemoteMinPriority || !remoteLogger.log(priority, tag, msg, tr)) {
            // If no remoteLogger, too low to do remote log or if remote log didn't log to logcat: do android log
            if (consoleLogging || priority >= android.util.Log.ERROR) {
                android.util.Log.println(priority, tag, msg == null ? "<no message given>" : msg);
                if (tr != null) android.util.Log.println(priority, tag, getStackTraceString(tr));
            }
        }
        return priority;
    }

    // AUTO TAGGED

    public static int v() {
        return sRemoteMinPriority <= VERBOSE || consoleLogging ? v(tagP(), "<-", null) : -1;
    }
    public static int v(String msg) {
        return sRemoteMinPriority <= VERBOSE || consoleLogging ? v(tagP(), msg, null) : -1;
    }

    public static int d() {
        return sRemoteMinPriority <= DEBUG || consoleLogging ? d(tagP(), "<-", null) : -1;
    }

    public static int d(String msg) {
        return sRemoteMinPriority <= DEBUG || consoleLogging ? d(tagP(), msg, null) : -1;
    }

    public static int i(String msg) {
        return sRemoteMinPriority <= INFO || consoleLogging ? i(tagP(), msg, null) : -1;
    }

    public static int w(String msg) {
        return sRemoteMinPriority <= WARN || consoleLogging ? w(tagP(), msg, null) : -1;
    }

    public static int e(String msg) {
        return sRemoteMinPriority <= ERROR || consoleLogging ? e(tagP(), msg, null) : -1;
    }

    public static int wtf(String msg) {
        return sRemoteMinPriority <= ASSERT || consoleLogging ? wtf(tagP(), msg, null) : -1;
    }

    // TAG GIVEN

    public static int v(String tag, String msg) {
        return v(tag, msg, null);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return log(VERBOSE, tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return log(DEBUG, tag, msg, null);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return log(DEBUG, tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return log(INFO, tag, msg, null);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return log(INFO, tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return log(WARN, tag, msg, null);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return log(WARN, tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return log(WARN, tag, tr.getMessage(), tr);
    }

    public static int e(String tag, String msg) {
        return log(ERROR, tag, "", new RuntimeException(msg));
    }

    public static int e(String tag, String msg, Throwable tr) {
        return log(ERROR, tag, msg, tr);
    }

    public static int e(String tag, Throwable tr) {
        return log(ERROR, tag, tr.getMessage(), tr);
    }

    public static int wtf(String tag, String msg) {
        return log(ASSERT, tag, msg, null);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return log(ASSERT, tag, msg, tr);
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
        i(Thread.currentThread().getStackTrace()[1].getClassName(), message);
    }


    static Pattern sSplitRegEx = Pattern.compile("\\.");
    public static String tag(int i) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3 + i];
        String[] split = sSplitRegEx.split(stackTraceElement.getClassName());
        return String.format("%s.%s.%s:%d",
                             split[1],
                             split[split.length - 1],
                             stackTraceElement.getMethodName(),
                             stackTraceElement.getLineNumber());
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
