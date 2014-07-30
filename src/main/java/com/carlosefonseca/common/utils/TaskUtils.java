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
        final Exception error = task.getError();
        if (error != null) {
            Log.w(tag, msg);
            TaskUtils.logAggregateException(error);
            return true;
        }
        return false;
    }


    public static void logAggregateException(Exception error) {
        if (error != null) {
            Log.w(TAG, error);
            if (error instanceof AggregateException) {
                for (Exception exception : ((AggregateException) error).getErrors()) {
                    Log.w(TAG, "--------");
                    Log.w(TAG, exception);
                }
            }
        }
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
}
