package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import bolts.Task;
import com.carlosefonseca.common.CFActivity;
import junit.framework.Assert;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Wrapper that allows photo or video capturing that returns a Task.
 *
 * <p>Requires that the caller activity be a {@link CFActivity}, since it uses {@link ActivityStateListener}.
 *
 * <p>Call {@link #photoTo(File)} or {@link #videoTo(File)} to get a task that will resolve when the camera creates a file.
 */
public class SimplePhotoVideoCaptureTask {
    private static final String TAG = CodeUtils.getTag(SimplePhotoVideoCaptureTask.class);

    public static final int PHOTO = 1;
    public static final int VIDEO = 2;

    @NonNull private final CFActivity mActivity;
    private File mFile;
    private int mType;

    private final int mRequestCode = new Random().nextInt(0xFFFF);
    private Task<File>.TaskCompletionSource mTaskCompletionSource;
    private AtomicBoolean mAttached = new AtomicBoolean();


    /* PUBLIC API */

    public SimplePhotoVideoCaptureTask(@NonNull CFActivity activity) {
        mActivity = activity;
    }
    /**
     * Captures a photo.
     *
     * @param file The file to write the image to, or a directory to create an image with the timestamp as the filename.
     * @return Task with the file, if the file exists.
     */
    public Task<File> photoTo(File file) {
        return captureTo(file, PHOTO);
    }

    /**
     * Captures a video.
     *
     * @param file The file to write the video to, or a directory to create a video with the timestamp as the filename.
     * @return Task with the file, if the file exists.
     */
    public Task<File> videoTo(File file) {
        return captureTo(file, VIDEO);
    }

    /* IMPLEMENTATION STUFF */

    /**
     * Create the intent to capture a photo. Override to provide an alternative activity.
     */
    @NonNull
    protected Intent getPhotoIntent(Context context, File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * Create the intent to capture a video. Override to provide an alternative activity.
     */
    @NonNull
    protected Intent getVideoIntent(Context context, File f) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }


    private Task<File> captureTo(@NonNull File file, int type) {
        Assert.assertNotNull(file);
        if (file.isDirectory()) {
            file.mkdirs();
            mFile = new File(file, getFormattedTimestamp() + "." + getExtension(type));
        } else {
            mFile = file;
        }
        mType = type;
        return start().getTask();
    }

    @NonNull
    private String getFormattedTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }


    protected Task<File> getTask() {
        return mTaskCompletionSource.getTask();
    }

    /**
     * Creates and starts the camera intent.
     */
    protected SimplePhotoVideoCaptureTask start() {
        mTaskCompletionSource = Task.create();
        attachListener(mActivity);
        mActivity.startActivityForResult(getCaptureIntent(mActivity, mFile, mType), mRequestCode);
        return this;
    }

    /**
     * Creates an intent for the given type of media.
     */
    @NonNull
    private Intent getCaptureIntent(Context context, File f, int type) {
        Intent intent;
        switch (type) {
            case PHOTO:
                intent = getPhotoIntent(context, f);
                break;
            case VIDEO:
                intent = getVideoIntent(context, f);
                break;
            default:
                throw new NotImplementedException("type " + type + " not implemented");
        }
        if (intent.resolveActivity(context.getPackageManager()) != null) return intent;
        throw new RuntimeException("Can't resolve activity for intent " + intent);
    }

    /**
     * Attaches the onActivityResult listener to the activity.
     */
    private void attachListener(final CFActivity activity) {
        if (!mAttached.compareAndSet(false, true)) return;

        ActivityStateListener.SimpleInterface listener = new ActivityStateListener.SimpleInterface() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == mRequestCode) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (mFile.exists()) {
                            mTaskCompletionSource.setResult(mFile);
                        } else {
                            mTaskCompletionSource.setError(new FileNotFoundException("No file returned from camera"));
                        }
                    } else {
                        mTaskCompletionSource.setCancelled();
                    }
                }
            }
        };
        activity.getActivityStateListener().addListener(listener);
    }

    @NonNull
    private String getExtension(int type) {
        switch (type) {
            case PHOTO:
                return "jpg";
            case VIDEO:
                return "mp4";
            default:
                throw new NotImplementedException("type '" + type + "' not supported");
        }
    }
}
