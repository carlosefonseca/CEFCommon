package com.carlosefonseca.common.utils;

import android.os.Handler;
import android.os.Looper;
import bolts.AggregateException;
import bolts.Continuation;
import bolts.Task;
import org.jetbrains.annotations.Nullable;

public final class TaskUtils {
    private static final String TAG = CodeUtils.getTag(TaskUtils.class);

    public static final Continuation<Void, Void> LogErrorContinuation = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> voidTask) throws Exception {
            logTaskError(voidTask);
            return null;
        }
    };

    public static boolean logTaskError(Task<?> voidTask) {
        final Exception error = voidTask.getError();
        logAggregateException(error);
        return error != null;
    }

    /**
     * Checks the task for errors and performs AggregateException-aware logging.
     *
     * @param tag  A tag for the error logs.
     * @param msg  A message to prepend to the exceptions
     * @param task The task to check.
     * @return True if there are errors, false otherwise.
     */
    public static boolean hasErrors(String tag, String msg, Task<?> task) {
        if (task.isFaulted()) {
            Log.w(tag, msg);
            TaskUtils.logAggregateException(task.getError());
            return true;
        }
        return false;
    }


    public static void logAggregateException(Exception error) {
        if (error != null) {
            if (error instanceof AggregateException) {
                Log.w(TAG, new RuntimeException(error.getMessage()));
                for (Exception exception : ((AggregateException) error).getErrors()) {
                    Log.w(TAG, "--------");
                    Log.w(TAG, exception);
                }
            } else {
                Log.w(TAG, error);
            }
        }
    }

    @Nullable
    public static String logAggregateExceptionToString(Exception error) {
        if (error != null) {
            if (error instanceof AggregateException) {
                StringBuilder s = new StringBuilder(error.getMessage());
                for (Exception exception : ((AggregateException) error).getErrors()) {
                    s.append("--------");
                    s.append(exception);
                }
                return s.toString();
            } else {
                return error.getMessage();
            }
        }
        return null;
    }

    private TaskUtils() {}

    public static <T> Task<T> runTaskWithTimeout(Task<T> t, int timeout) {

        final Task<T>.TaskCompletionSource taskCompletionSource = Task.create();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                taskCompletionSource.trySetError(new RuntimeException("Timeout!"));
            }
        }, timeout);

        t.continueWith(new Continuation<T, Object>() {
            @Nullable
            @Override
            public T then(Task<T> task) throws Exception {
                taskCompletionSource.setResult(task.getResult());
                return null;
            }
        });

        return taskCompletionSource.getTask();
    }

    /**
     * TaskUtils.<Object>getPassThruLogErrorContinuation()
     */
    public static <T> Continuation<T, T> getPassThruLogErrorContinuation() {
        return new Continuation<T, T>() {
            @Override
            public T then(Task<T> task) throws Exception {
                if (hasErrors(TAG, "Errors during task execution", task)) {
                    throw task.getError();
                } else {
                    return task.getResult();
                }
            }
        };
    }

    /**
     * TaskUtils.<Object>getPassThruLogErrorContinuation()
     */
    public static <T> Continuation<T, T> getPassThruLogErrorContinuation(final String tag, final String message) {
        return new Continuation<T, T>() {
            @Override
            public T then(Task<T> task) throws Exception {
                if (hasErrors(tag, message, task)) {
                    throw task.getError();
                } else {
                    return task.getResult();
                }
            }
        };
    }

    /**
     * TaskUtils.<Object>getPassThruLogErrorContinuation()
     */
    public static Continuation<Void, Void> getLogErrorContinuation(final String tag, final String message) {
        return new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (hasErrors(tag, message, task)) {
                    throw task.getError();
                } else {
                    return task.getResult();
                }
            }
        };
    }

    public static <T> String toString(Task<T> task) {
        if (task == null) return "null";
        if (task.isCompleted()) return "[Task completed: " + task.getResult() + "]";
        if (task.isFaulted()) {
            return "[Task is Faulted: " + logAggregateExceptionToString(task.getError());
        }
        if (task.isCancelled()) {
            return "[Task was canceled]";
        }
        return "[Task: Unknown state R:" + task.getResult() + ", E:" + task.getError().getMessage();
    }
}