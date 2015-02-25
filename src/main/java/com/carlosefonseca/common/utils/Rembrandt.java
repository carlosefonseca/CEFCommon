package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.*;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.CFApp;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching and
 * setting on ImageViews.
 */
@Deprecated
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
    private int mPlaceholder;

    private HashMap<ImageView, String> mMapping = new HashMap<>();
    private Transform mTransform;
    private OnBitmap mNotify;
    private boolean mHideIfNull;
    @Nullable private Point mMaxSize;
    private boolean mMeasureFirst;

    DisplayImageOptions options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                                                   .cacheInMemory(true)
                                                                   .cacheOnDisk(true)
                                                                   .considerExifParams(true)
                                                                   .imageScaleType(ImageScaleType.EXACTLY)
                                                                   .bitmapConfig(Bitmap.Config.RGB_565)
                                                                   .build();
    private File mExternalFilesDir = CFApp.getContext().getExternalFilesDir(null);

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
        this.mPlaceholder = drawable;
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

    static SimpleImageLoadingListener animateFirstDisplayListener = new SimpleImageLoadingListener() {

        final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    };


    public Task<Void> into(final ImageView view, final short animation) {
        final Task<Void> run;
        if (null == mUrl && null == mFile) {
            if (mPlaceholder != 0) {
                placePlaceholder(view);
            } else {
                view.setImageBitmap(null);
                if (mHideIfNull) view.setVisibility(View.GONE);
            }
            mMapping.remove(view);
            run = Task.forResult(null);
        } else {
            view.setVisibility(View.VISIBLE);

            String uri = null;
            if (mUrl != null) {
                uri = UIL.getUri(mUrl);
            } else if (mFile != null) {
                uri = UIL.getUri(mFile);
            } else {
                Log.wtf(TAG, new RuntimeException("wtf"));
            }

            ImageLoader.getInstance().displayImage(uri, view, options, animateFirstDisplayListener);
            return Task.forResult(null);

/*
            if (mMeasureFirst && mMaxSize == null) {
                final String url = mUrl;
                final File file = mFile;
                final int placeholder = mPlaceholder;
                final HashMap<ImageView, String> mapping = mMapping;
                final Transform transform = mTransform;
                final OnBitmap notify = mNotify;
                final LruCache<String, Bitmap> cache = mCache;

                final Task<Void>.TaskCompletionSource source = Task.create();
                CodeUtils.runOnGlobalLayout(view, new CodeUtils.RunnableWithView<ImageView>() {
                    @Override
                    public void run(ImageView view) {
                        Log.v(TAG + ".run",
                              "{%s} View: %s URL: %s File: %s MaxSize: %s",
                              Rembrandt.this.hashCode(),
                              Integer.toHexString(System.identityHashCode(view)),
                              url,
                              file,
                              null);
                        Rembrandt.run(view, url, file, null, placeholder, animation, mapping, transform, notify, cache)
                                 .continueWith(new Continuation<Void, Object>() {
                                     @Nullable
                                     @Override
                                     public Object then(Task<Void> task) throws Exception {
                                         if (task.isCompleted()) {
                                             source.setResult(task.getResult());
                                         } else if (task.isFaulted()) {
                                             source.setError(task.getError());
                                         } else if (task.isCancelled()) {
                                             source.setCancelled();
                                         }
                                         return null;
                                     }
                                 });
                    }
                });
                run = source.getTask();
            } else {
                Log.v(TAG + ".run",
                      "{%s} View: %s URL: %s File: %s MaxSize: %s",
                      hashCode(),
                      Integer.toHexString(System.identityHashCode(view)),
                      mUrl,
                      mFile,
                      mMaxSize);
                run = run(view, mUrl, mFile, mMaxSize, mPlaceholder, animation, mMapping, mTransform, mNotify, mCache);
            }
*/
        }
        mUrl = null;
        mFile = null;
        mPlaceholder = 0;
        return run;
    }

    private static Task<Void> run(final ImageView view,
                                  @Nullable final String url,
                                  final File file,
                                  @Nullable final Point maxSize,
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
                    Log.v(TAG, "CANCELED Image loading of " + url + ". View is mapped to another path.");
                    return null;
                }
                int mWidth = maxSize != null
                             ? maxSize.x
                             : view.getLayoutParams().width == WRAP_CONTENT ? 0 : Math.max(view.getMeasuredWidth(), 0);
                int mHeight = maxSize != null
                              ? maxSize.y
                              : view.getLayoutParams().height == WRAP_CONTENT
                                ? 0
                                : Math.max(view.getMeasuredHeight(), 0);

                Log.v(TAG, "Size: %dx%d", mWidth, mHeight);

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
                    bitmap = UIL.loadSync(UIL.getUri(file), mWidth, mHeight);
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
        return UIL.loadSync(url, widthPx, heightPx);
    }

    @Nullable
    public static Bitmap bitmapFromFile(@Nullable File file, int widthPx, int heightPx) {
        return UIL.loadSync(UIL.getUri(file), widthPx, heightPx);
    }

    @Nullable
    public static Bitmap bitmapFromUrl(@NotNull String url, int widthPx, int heightPx) throws IOException {
        return UIL.loadSync(url, widthPx, heightPx);
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
            final Bitmap bitmap = mCache.get("mPlaceholder");
            Task<Bitmap> placeholderTask;
            if (bitmap == null) {
                placeholderTask = Task.callInBackground(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                                                     Rembrandt.this.mPlaceholder);
                        final Bitmap bitmap1 = mTransform.bitmap(bitmap);
                        mCache.put("mPlaceholder", bitmap);
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
        view.setImageResource(mPlaceholder);
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

    public Rembrandt maxSize(int widthPixels, int heightPixels) {
        if (widthPixels > 0 && heightPixels > 0) {
            mMaxSize = new Point(widthPixels, heightPixels);
        } else {
            mMaxSize = null;
        }
        return this;
    }

    public Rembrandt measureFirst() {
        mMeasureFirst = true;
        return this;
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
