package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.carlosefonseca.common.utils.NetworkingUtils.getLastSegmentOfURL;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching and
 * setting on ImageViews.
 */
public class Rembrandt {
    private static final String TAG = CodeUtils.getTag(Rembrandt.class);
    public static final int XFADE_MILLIS = 500;
    private static final int FADE_MILLIS = 100;

    LruCache<String, Bitmap> mCache = new ImageUtils.BitmapCache();

    private static final short NOT_ANIMATED = 0;
    private static final short FADE_IN = 1;
    private static final short CROSS_FADE = 2;

//    private final Context mContext;
    private String mUrl;
    private File mFile;
    private int placeholder;

    private HashMap<ImageView, String> mMapping = new HashMap<>();
    private Transform mTransform;
    private OnBitmap mNotify;

    public Rembrandt(Context context) {
        /*mContext = context*/;
    }

    /**
     * Same as new Rembrandt(context). For Picasso compatibility.
     */
    public static Rembrandt with(Context context) {
        return new Rembrandt(context);
    }

    public Rembrandt load(String url) {
        mUrl = StringUtils.stripToNull(url);
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
        return into(view, NOT_ANIMATED);
    }

    public Task<Void> fadeIn(final ImageView view) {
        return into(view, FADE_IN);
    }

    public Task<Void> xFade(final ImageView view) {
        return into(view, CROSS_FADE);
    }

    public Task<Void> into(final ImageView view, short animation) {
        final Task<Void> run;
        if (null == mUrl && null == mFile) {
            view.setImageBitmap(null);
            mMapping.remove(view);
            run = Task.forResult(null);
        } else {
            run = run(view, mUrl, mFile, placeholder, animation, mMapping, mTransform, mNotify, mCache);
        }
        mUrl = null;
        mFile = null;
        placeholder = 0;
        return run;
    }

    private static Task<Void> run(final ImageView view,
                                  @Nullable final String url,
                                  final File file,
                                  final int placeholder,
                                  final short animation,
                                  final HashMap<ImageView, String> mapping,
                                  @Nullable final Transform transform,
                                  @Nullable final OnBitmap notify,
                                  final LruCache<String, Bitmap> cache) {

        Assert.assertTrue("URL and File are null!", url != null || file != null);
        Assert.assertNotNull("ImageView is null!", view);

        final String path = url != null ? url : file.getAbsolutePath();
        if (path.equals(view.getTag())) return Task.forResult(null);
        if (animation == NOT_ANIMATED || animation == FADE_IN) {
            view.setImageBitmap(null);
        }
//        view.setTag(path);
        mapping.put(view, path);

        Bitmap bitmap = cache.get(path);
        if (bitmap != null) {
            if (notify != null) notify.bitmap(bitmap);
            setImageBitmapOnView(bitmap, view, animation == FADE_IN ? NOT_ANIMATED : animation);
            return Task.forResult(null);
        }


        return Task.callInBackground(new Callable<Bitmap>() {
            @Nullable
            @Override
            public Bitmap call() throws Exception {
                if (!path.equals(mapping.get(view))) {
                    Log.v(TAG, "CANCELED Image loading of " + url);
                    return null;
                }
                Bitmap bitmap;
                if (url != null) {
                    if (!ImageUtils.isImage(url)) {
//                        throw new RuntimeException("Url is not an image: " + url);
                        Log.w(TAG, "Url is not an image: " + url);
                        return null;
                    }
                    if (url.startsWith("http://")) {
                        bitmap = bitmapFromUrl(url);
                    } else {
                        bitmap = bitmapFromFile(url);
                    }
                } else {
                    bitmap = ImageUtils.getCachedPhoto(file, 0, 0, null);
                }
                if (transform != null) bitmap = transform.bitmap(bitmap);
                cache.put(path, bitmap);
                if (notify != null) notify.bitmap(bitmap);
                return bitmap;
            }
        }).continueWith(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> bitmapTask) throws Exception {
                if (!path.equals(mapping.get(view))) {
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

                setImageBitmapOnView(result, view, animation);
                mapping.remove(view);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(TaskUtils.LogErrorContinuation);
    }

    @Nullable
    private static Bitmap bitmapFromFile(String url) {
        final File file1 = url.startsWith("/") ? new File(url) : ResourceUtils.getFullPath(url);
        return ImageUtils.getCachedPhoto(file1, 0, 0, null);
    }

    private static Bitmap bitmapFromUrl(String url) throws IOException {
        final File fullPath = ResourceUtils.getFullPath(getLastSegmentOfURL(url));
        Bitmap cachedPhoto = ImageUtils.tryPhotoFromFileOrAssets(fullPath, -1, -1);
        if (cachedPhoto != null) return cachedPhoto;
        Log.i(TAG, "Downloading image " + url);
        Bitmap bitmap = NetworkingUtils.loadBitmap(url);
        new ImageUtils.ImageWriter(fullPath, bitmap).execute();
        return bitmap;
    }

    private static void setImageBitmapOnView(@NotNull Bitmap result, final ImageView view, short animated) {
        switch (animated) {

            case FADE_IN:
                AnimationUtils.setImageBitmapWithXFade(view, result, FADE_MILLIS);
                break;

            case CROSS_FADE:
                AnimationUtils.setImageBitmapWithXFade(view, result, XFADE_MILLIS);
                break;

            case NOT_ANIMATED:
            default:
                view.setImageBitmap(result);
                break;
        }
    }

    public interface Transform {
        Bitmap bitmap(Bitmap bitmap);
    }

    /**
     * Allows modification of the bitmap, after being loaded. This modified bitmap will be cached and this code will
     * not be run again when loading from cache.
     * @see #notify(com.carlosefonseca.common.utils.Rembrandt.OnBitmap)
     */
    public Rembrandt transform(Transform transform) {
        this.mTransform = transform;
        return this;
    }

    public interface OnBitmap {
        void bitmap(Bitmap bitmap);
    }

    /**
     * Allows something to be run after the bitmap is loaded, either from cache or from the source.
     * @see #transform(com.carlosefonseca.common.utils.Rembrandt.Transform)
     */
    public Rembrandt notify(OnBitmap listener) {
        this.mNotify = listener;
        return this;
    }
}
