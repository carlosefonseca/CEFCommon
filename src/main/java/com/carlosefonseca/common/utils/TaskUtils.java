package com.carlosefonseca.common.utils;

import android.os.Handler;
import android.os.Looper;
import bolts.Continuation;
import bolts.Task;

public final class TaskUtils {
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
}
