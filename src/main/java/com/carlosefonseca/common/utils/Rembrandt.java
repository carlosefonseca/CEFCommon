package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.*;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.apache.commons.lang3.StringUtils;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
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

    private final Context mContext;
    private String mUrl;
    private File mFile;
    private int placeholder;

    private HashMap<ImageView, String> mMapping = new HashMap<>();
    private Transform mTransform;
    private OnBitmap mNotify;
    private boolean mHideIfNull;

    public Rembrandt(Context context) {
        mContext = context;
    }

    /**
     * Creates a new instance reusing the context and cache from the argument.
     */
    public Rembrandt(Rembrandt rembrandt) {
        mContext = rembrandt.mContext;
        mCache = rembrandt.mCache;
    }

    /**
     * Same as new Rembrandt(context).
     */
    public static Rembrandt with(Context context) {
        return new Rembrandt(context);
    }

    /**
     * Same as new Rembrandt(Rembrandt).
     * @see #Rembrandt(Rembrandt)
     */
    public static Rembrandt with(Rembrandt rembrandt) {
        return new Rembrandt(rembrandt);
    }

    public Rembrandt load(@Nullable String url) {
        mUrl = StringUtils.stripToNull(url);
        mFile = null;
        return this;
    }

    public Rembrandt load(@Nullable File file) {
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
            if (placeholder != 0) {
                placePlaceholder(view);
            } else {
                view.setImageBitmap(null);
                if (mHideIfNull) view.setVisibility(View.GONE);
            }
            mMapping.remove(view);
            run = Task.forResult(null);
        } else {
            view.setVisibility(View.VISIBLE);
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

        Assert.assertTrue("URL and File are null!", url != null || file != null); // take care on into()
        Assert.assertNotNull("ImageView is null!", view);

        final String path = url != null ? url : file.getAbsolutePath();
        if (path.equals(view.getTag())) return Task.forResult(null);
        if (animation == NOT_ANIMATED || animation == FADE_IN) {
//            Log.v("setImageBitmap(null)");
            view.setImageBitmap(null);
        }
//        view.setTag(path);
        mapping.put(view, path);

        Bitmap bitmap = cache.get(path);
        if (bitmap != null) {
//            Log.v("Got from cache");
            if (notify != null) notify.bitmap(bitmap);
            setImageBitmapOnView(bitmap, view, animation == FADE_IN ? NOT_ANIMATED : animation);
            return Task.forResult(null);
        } else {
//            Log.v("DIDN'T get from cache");
            if (placeholder != 0) view.setImageResource(placeholder);
        }


        return Task.callInBackground(new Callable<Bitmap>() {
            @Nullable
            @Override
            public Bitmap call() throws Exception {
                if (!path.equals(mapping.get(view))) {
                    Log.v(TAG, "CANCELED Image loading of " + url);
                    return null;
                }
                int mWidth = view.getLayoutParams().width == WRAP_CONTENT ? 0 : Math.max(view.getMeasuredWidth(), 0);
                int mHeight = view.getLayoutParams().height == WRAP_CONTENT ? 0 : Math.max(view.getMeasuredHeight(), 0);

                if (mWidth == 0 || mHeight == 0) {
                    mWidth = 0;
                    mHeight = 0;
                }

                Bitmap bitmap;
                if (url != null) {
                    if (!ImageUtils.isImage(url)) {
                        Log.w(TAG, "Url is not an image: " + url);
                        return null;
                    }
                    if (url.startsWith("http://")) {
                        bitmap = bitmapFromUrl(url, mWidth, mHeight);
                    } else {
                        bitmap = bitmapFromFile(url, mWidth, mHeight);
                    }
                } else {
                    bitmap = ImageUtils.getCachedPhotoPx(file, mWidth, mHeight, null);
                }
                if (bitmap != null) {
                    if (transform != null) bitmap = transform.bitmap(bitmap);
                    cache.put(path, bitmap);
                    if (notify != null) notify.bitmap(bitmap);
                }
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
    public static Bitmap bitmapFromFile(String url, int widthPx, int heightPx) {
        final File file1 = url.startsWith("/") ? new File(url) : ResourceUtils.getFullPath(url);
        return bitmapFromFile(file1, widthPx, heightPx);
    }

    @Nullable
    public static Bitmap bitmapFromFile(@Nullable File file, int widthPx, int heightPx) {
        return ImageUtils.getCachedPhotoPx(file, widthPx, heightPx, null);
    }

    @Nullable
    public static Bitmap bitmapFromUrl(String url, int widthPx, int heightPx) throws IOException {
        final File fullPath = ResourceUtils.getFullPath(getLastSegmentOfURL(url));
        Bitmap cachedPhoto = ImageUtils.tryPhotoFromFileOrAssetsPx(fullPath, widthPx, heightPx);
        if (cachedPhoto != null) return cachedPhoto;
        Log.i(TAG, "Downloading image " + url);
        Bitmap bitmap = NetworkingUtils.loadBitmap(url);
        if (bitmap != null && fullPath != null) ImageUtils.writeImageInBackground(fullPath, bitmap);
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

    private void placePlaceholder(final ImageView view) {
/*
        if (mTransform != null) {
            final Bitmap bitmap = mCache.get("placeholder");
            Task<Bitmap> placeholderTask;
            if (bitmap == null) {
                placeholderTask = Task.callInBackground(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                                                     Rembrandt.this.placeholder);
                        final Bitmap bitmap1 = mTransform.bitmap(bitmap);
                        mCache.put("placeholder", bitmap);
                        return bitmap1;
                    }
                });
            } else {
                placeholderTask = Task.forResult(bitmap);
            }
            placeholderTask.continueWith(new Continuation<Bitmap, Void>() {
                @Override
                public Void then(Task<Bitmap> task) throws Exception {
                    view.setImageBitmap(task.getResult());
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
*/
        view.setImageResource(placeholder);
    }

    public Rembrandt hideIfNull() {
        mHideIfNull = true;
        return this;
    }

    public Rembrandt hideIfNull(boolean hide) {
        mHideIfNull = hide;
        return this;
    }

    public void destroy() {
        mCache = null;
        mMapping = null;
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

    public static class CircleCropTransform implements Transform {
        private Bitmap mMask;
        private int mSide;
        public final int mMargin;

        public CircleCropTransform(int side, int margin) {
            this.mSide = side;
            this.mMargin = margin;
        }

        @Override
        public Bitmap bitmap(Bitmap original) {
            Bitmap mask = getMask();
            Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            final Rect rect = new Rect(0, 0, mask.getWidth(), mask.getHeight());
            rect.inset(mMargin, mMargin);
            mCanvas.drawBitmap(original, ImageUtils.getCenterSquare(original), rect, null);
            mCanvas.drawBitmap(mask, 0, 0, paint);
            paint.setXfermode(null);
            return result;
        }

        private Bitmap getMask() {
            if (mMask == null) {
                mMask = Bitmap.createBitmap(mSide, mSide, Bitmap.Config.ARGB_8888);
                final Canvas canvasMask = new Canvas(mMask);
                final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                maskPaint.setColor(Color.GREEN);
                maskPaint.setStyle(Paint.Style.FILL);
                canvasMask.drawCircle(mSide / 2, mSide / 2, mSide / 2 - mMargin, maskPaint);
            }
            return mMask;
        }
    }
}
