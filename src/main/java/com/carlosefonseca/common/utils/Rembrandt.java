package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import junit.framework.Assert;

import java.io.File;
import java.util.concurrent.Callable;

import static com.carlosefonseca.common.utils.NetworkingUtils.getLastSegmentOfURL;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching and
 * setting on ImageViews.
 */
public class Rembrandt {
    private static final String TAG = CodeUtils.getTag(Rembrandt.class);
    private final Context mContext;
    private String mUrl;
    private File mFile;
    private int placeholder;

    public Rembrandt(Context context) {
        mContext = context;
    }

    /**
     * Same as new Rembrandt(context). For Picasso compatibility.
     */
    public static Rembrandt with(Context context) {
        return new Rembrandt(context);
    }

    public Rembrandt load(String url) {
        mUrl = url;
        mFile = null;
        return this;
    }

    public Rembrandt load(File file) {
        mFile = file;
        mUrl = null;
        return this;
    }

    public Rembrandt placeholder(int drawable) {
        this.placeholder = drawable;
        return this;
    }

    public Task<Void> into(final ImageView view) {
        return run(view, mUrl, mFile, placeholder, true);
    }

    public Task<Void> into(final ImageView view, final boolean animated) {
        final Task<Void> run = run(view, mUrl, mFile, placeholder, animated);
        mUrl = null;
        mFile = null;
        placeholder = 0;
        return run;
    }

    private static Task<Void> run(final ImageView view,
                                  final String url,
                                  final File file,
                                  final int placeholder,
                                  final boolean animated) {
        Assert.assertTrue("URL and File are null!", url != null || file != null);
        Assert.assertNotNull("ImageView is null!", view);

        final String path = url != null ? url : file.getAbsolutePath();
        if (path.equals(view.getTag())) return Task.forResult(null);
        view.setImageBitmap(null);
        view.setVisibility(View.INVISIBLE);

        return Task.callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                if (url != null) {
                    if (!ImageUtils.isImage(url)) {
                        throw new RuntimeException("Url is not an image: " + url);
                    }
                    if (url.startsWith("http://")) {
                        final File fullPath = ResourceUtils.getFullPath(getLastSegmentOfURL(url));
                        Bitmap cachedPhoto = ImageUtils.tryPhotoFromFileOrAssets(fullPath, -1, -1);
                        if (cachedPhoto != null) return cachedPhoto;
                        Log.i(TAG, "Downloading image " + url);
                        Bitmap bitmap = NetworkingUtils.loadBitmap(url);
                        new ImageUtils.ImageWriter(fullPath, bitmap).execute();
                        return bitmap;
                    } else {
                        final File file = url.startsWith("/") ? new File(url) : ResourceUtils.getFullPath(url);
                        return ImageUtils.getCachedPhoto(file, 0, 0, null);
                    }
                } else {
                    return ImageUtils.getCachedPhoto(file, 0, 0, null);
                }
            }
        }).continueWith(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> task) throws Exception {
                final Bitmap result = task.getResult();

                if (result == null || task.getError() != null) {
                    if (placeholder != 0) view.setImageResource(placeholder);
                    if (task.getError() != null) {
                        Log.w(TAG, task.getError());
                        throw task.getError();
                    }
                    return null;
                }

                view.setImageBitmap(result);
                if (animated) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(100);
                    Animation.AnimationListener listener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    };
                    alphaAnimation.setAnimationListener(listener);
                    view.startAnimation(alphaAnimation);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(TaskUtils.LogErrorContinuation);
    }
}
