package com.carlosefonseca.common.utils;

import android.os.Handler;
import android.os.Looper;
import bolts.AggregateException;
import bolts.Continuation;
import bolts.Task;

public final class TaskUtils {
    private static final String TAG = CodeUtils.getTag(TaskUtils.class);

    public static final Continuation<Void, Void> LogErrorContinuation = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> voidTask) throws Exception {
            final Exception error = voidTask.getError();
            if (error != null) {
                Log.w(TAG, error);
                if (error instanceof AggregateException) {
                    for (Exception exception : ((AggregateException) error).getErrors()) {
                        Log.w(TAG, exception);
                    }
                }
            }
            return null;
        }
    };

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
            @Override
            public T then(Task<T> task) throws Exception {
                taskCompletionSource.setResult(task.getResult());
                return null;
            }
        });

        return taskCompletionSource.getTask();
    }

    public static <T> Continuation<T, T> getPassThruLogErrorContinuation() {
        return new Continuation<T, T>() {
            @Override
            public T then(Task<T> voidTask) throws Exception {
                final Exception error = voidTask.getError();
                if (error != null) {
                    Log.w(TAG, "Errors during task execution", error);
                    if (error instanceof AggregateException) {
                        for (Exception exception : ((AggregateException) error).getErrors()) {
                            Log.w(TAG, exception);
                        }
                    }
                    throw error;
                } else {
                    return voidTask.getResult();
                }
            }
        };
    }
}