package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.carlosefonseca.common.utils.Log.tag;
import static com.carlosefonseca.common.utils.NetworkingUtils.getLastSegmentOfURL;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching and
 * setting on ImageViews.
 */
public class Rembrandt {
    private static final String TAG = CodeUtils.getTag(Rembrandt.class);

    static LruCache<String, Bitmap> mCache = new ImageUtils.BitmapCache();

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
        if (null == mUrl && null == mFile) {
            Log.w(TAG, "No url or file specified for Rembrandt to load.");
            return Task.cancelled();
        }
        return run(view, mUrl, mFile, placeholder, true);
    }

    public Task<Void> into(final ImageView view, final boolean animated) {
        if (null == mUrl && null == mFile) {
            Log.w(tag(1), "No url or file specified for Rembrandt to load.");
            return Task.cancelled();
        }

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
        view.setVisibility(View.INVISIBLE);
        view.setTag(path);

        final Bitmap bitmap = mCache.get(path);
        if (bitmap != null) {
            setImageBitmapOnView(bitmap, view, false);
            return Task.forResult(null);
        }

        view.setImageBitmap(null);

        return Task.callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                if (!view.getTag().equals(path)) {
                    Log.v(TAG, "CANCELED Image loading of " + url);
                    return null;
                }
                Bitmap bitmap;
                if (url != null) {
                    if (!ImageUtils.isImage(url)) {
                        throw new RuntimeException("Url is not an image: " + url);
                    }
                    if (url.startsWith("http://")) {
                        bitmap = bitmapFromUrl(url, path);
                    } else {
                        bitmap = bitmapFromFile(url);
                    }
                } else {
                    bitmap = ImageUtils.getCachedPhoto(file, 0, 0, null);
                }
                mCache.put(path, bitmap);
                return bitmap;
            }
        }).continueWith(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> bitmapTask) throws Exception {
                if (!view.getTag().equals(path)) {
                    Log.v(TAG, "CANCELED Image loading of " + url);
                    return null;
                }
                final Bitmap result = bitmapTask.getResult();

                if (result == null || bitmapTask.getError() != null) {
                    if (placeholder != 0) view.setImageResource(placeholder);
                    if (bitmapTask.getError() != null) {
                        Log.w(TAG, bitmapTask.getError());
                        throw bitmapTask.getError();
                    }
                    return null;
                }

                setImageBitmapOnView(result, view, animated);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(TaskUtils.LogErrorContinuation);
    }

    private static Bitmap bitmapFromFile(String url) {
        final File file1 = url.startsWith("/") ? new File(url) : ResourceUtils.getFullPath(url);
        return ImageUtils.getCachedPhoto(file1, 0, 0, null);
    }

    private static Bitmap bitmapFromUrl(String url, String path) throws IOException {
        final File fullPath = ResourceUtils.getFullPath(getLastSegmentOfURL(url));
        Bitmap cachedPhoto = ImageUtils.tryPhotoFromFileOrAssets(fullPath, -1, -1);
        if (cachedPhoto != null) return cachedPhoto;
        Log.i(TAG, "Downloading image " + url);
        Bitmap bitmap = NetworkingUtils.loadBitmap(url);
        new ImageUtils.ImageWriter(fullPath, bitmap).execute();
        return bitmap;
    }

    private static void setImageBitmapOnView(@NotNull Bitmap result, final ImageView view, boolean animated) {
        view.setImageBitmap(result);
        if (!animated) {
            view.setVisibility(View.VISIBLE);
            return;
        }

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(100);
        alphaAnimation.setAnimationListener(new AnimationUtils.AnimationListenerImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }
        });
        view.startAnimation(alphaAnimation);
    }
}
