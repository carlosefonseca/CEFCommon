package com.carlosefonseca.common.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import bolts.Task;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching
 * and setting on ImageViews.
 */
public class Gogh {

    @IntDef({NOT_ANIMATED, FADE_IN, CROSS_FADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationMode {}

    private static final short NOT_ANIMATED = 0;
    private static final short FADE_IN = 1;
    private static final short CROSS_FADE = 2;

    private String mUrl;
    private DrawableMaker<?> mDisplayer;
    private ImageView mView;
    private short mAnimation;
    private DisplayImageOptions mOptions;
    private DisplayImageOptions.Builder mOptionsBuilder;
    private boolean mHideIfNull;
    private OnBitmap mOnBitmap;
    private Task<Bitmap>.TaskCompletionSource mTaskSource;


    private Gogh(String url) {
        mUrl = StringUtils.stripToNull(url);
    }

    public static Gogh loadPhotoURI(@Nullable String url) {
        return new Gogh(url).photo();
    }

    public static Gogh loadIconURI(@Nullable String url) {
        return new Gogh(url).icon();
    }

    public static Gogh loadPhoto(@Nullable String url) {
        return loadPhotoURI(UIL.getUri(url));
    }

    public static Gogh loadPhoto(@Nullable File file) {
        return loadPhotoURI(UIL.getUri(file));
    }

    public static Gogh loadIcon(@Nullable File file) {
        return loadIconURI(UIL.getUri(file));
    }

    public static Gogh loadIcon(@Nullable String url) {
        return loadIconURI(UIL.getUri(url));
    }

    private Gogh photo() {
        mOptions = UIL.mOptionsForPhotos;
        return this;
    }

    private Gogh icon() {
        mOptions = UIL.mOptionsForIcons;
        return this;
    }

    public DisplayImageOptions.Builder getOptionsBuilder() {
        if (mOptionsBuilder == null) {
            mOptionsBuilder = new DisplayImageOptions.Builder().cloneFrom(mOptions);
            mOptions = null;
        }
        return mOptionsBuilder;
    }

    // PLACEHOLDER

    public Gogh showImageOnLoading(int imageRes) {
        getOptionsBuilder().showImageOnLoading(imageRes);
        return this;
    }

    public Gogh showImageOnLoading(Drawable drawable) {
        getOptionsBuilder().showImageOnLoading(drawable);
        return this;
    }

    public Gogh showImageForEmptyUri(int imageRes) {
        getOptionsBuilder().showImageForEmptyUri(imageRes);
        return this;
    }

    public Gogh showImageForEmptyUri(Drawable drawable) {
        getOptionsBuilder().showImageForEmptyUri(drawable);
        return this;
    }

    public Gogh showImageOnFail(int imageRes) {
        getOptionsBuilder().showImageOnFail(imageRes);
        return this;
    }

    public Gogh showImageOnFail(Drawable drawable) {
        getOptionsBuilder().showImageOnFail(drawable);
        return this;
    }

    // PLACEHOLDER END

    @Deprecated
    public Gogh placeholder(int drawable) {
        return showImageForEmptyUri(drawable).showImageOnFail(drawable);
    }

    public Gogh roundCorners(int radius) {
        return displayer(GoghHelper.getRoundCornersBitmapDisplayer(radius));
    }

    public Gogh circle() {
        return circle(true);
    }

    public Gogh circle(boolean circle) {
        return circle ? displayer(GoghHelper.getCircleBitmapDisplayer(0)) : this;
    }

    public Gogh square() {return square(true);}

    public Gogh square(boolean square) {
        return square ? displayer(new DrawableMaker<Drawable>() {
            @NonNull
            @Override
            public Drawable getDrawable(@NonNull Bitmap bitmap) {
                return new SquareDrawable(bitmap);
            }
        }) : this;
    }

    private Gogh displayer(DrawableMaker<?> displayer) {
        mDisplayer = displayer;
        return this;
    }

    public Gogh hideIfNull() {
        return hideIfNull(true);
    }

    public Gogh hideIfNull(boolean hide) {
        mHideIfNull = hide;
        return this;
    }

    public interface OnBitmap {
        void bitmap(Bitmap bitmap);
    }

    public Gogh onBitmap(OnBitmap runnable) {
        mOnBitmap = runnable;
        return this;
    }

    public void into(final ImageView view, @AnimationMode short animated) {
        mView = view;
        mAnimation = animated;
        GoghHelper.run(this);
    }

    /**
     * Specify the destination view, placing the image without any animation
     */
    public void into(final ImageView view) {
        into(view, NOT_ANIMATED);
    }

    public void fadeIn(final ImageView view) {
        into(view, FADE_IN);
    }

    public void xFade(final ImageView view) {
        into(view, CROSS_FADE);
    }

    public Task<Bitmap> taskInto(final ImageView view) {
        mTaskSource = Task.create();
        into(view);
        return mTaskSource.getTask();
    }
    public Task<Bitmap> taskFadeIn(final ImageView view) {
        mTaskSource = Task.create();
        into(view, FADE_IN);
        return mTaskSource.getTask();
    }

    public Task<Bitmap> taskXFade(final ImageView view) {
        mTaskSource = Task.create();
        into(view, CROSS_FADE);
        return mTaskSource.getTask();
    }

    public String getUri() {
        return mUrl;
    }

    public static class GoghHelper {
        private static final String TAG = CodeUtils.getTag(GoghHelper.class);
        public static final int XFADE_MILLIS = 500;
        private static final int FADE_MILLIS = 100;


        protected static void run(Gogh g) {
            DisplayImageOptions options = g.mOptions;
            DisplayImageOptions.Builder optionsBuilder = g.mOptionsBuilder;
            if (g.mHideIfNull) {
                if (g.mUrl == null) {
                    g.mView.setVisibility(View.GONE);
                } else {
                    g.mView.setVisibility(View.VISIBLE);
                }
            }

            ImageLoadingListener imageLoadingListener = null;
            if (g.mOnBitmap != null || g.mTaskSource != null) {
                imageLoadingListener = new MySimpleImageLoadingListener(g.mTaskSource, g.mOnBitmap);
            }

            if (g.mAnimation != NOT_ANIMATED || g.mDisplayer != null) {
                MySimpleImageDisplayer displayer = new MySimpleImageDisplayer(g.mAnimation, g.mDisplayer);
                if (optionsBuilder == null) optionsBuilder = new DisplayImageOptions.Builder().cloneFrom(options);
                optionsBuilder.displayer(displayer).resetViewBeforeLoading(g.mAnimation != CROSS_FADE);
            }

            if (optionsBuilder != null) options = optionsBuilder.build();
            UIL.display_(g.mUrl, g.mView, imageLoadingListener, options);
        }

        static LruCache<Integer, CFRoundedBitmapDisplayer> sRoundCornersBitmapDisplayerCache =
                new LruCache<Integer, CFRoundedBitmapDisplayer>(5) {
                    @Override
                    protected CFRoundedBitmapDisplayer create(Integer key) {
                        return new CFRoundedBitmapDisplayer(key);
                    }
                };
        static LruCache<Integer, CircleBitmapDisplayer> sCircleBitmapDisplayerCache =
                new LruCache<Integer, CircleBitmapDisplayer>(5) {
                    @Override
                    protected CircleBitmapDisplayer create(Integer key) {
                        return new CircleBitmapDisplayer(key);
                    }
                };

        public static CFRoundedBitmapDisplayer getRoundCornersBitmapDisplayer(int cornerRadius) {
            return sRoundCornersBitmapDisplayerCache.get(cornerRadius);
        }


        public static CircleBitmapDisplayer getCircleBitmapDisplayer(int margin) {
            return sCircleBitmapDisplayerCache.get(margin);
        }


        private static class MySimpleImageDisplayer implements BitmapDisplayer {
            private final short mAnimation;
            @Nullable private final DrawableMaker<?> mTransform;

            public MySimpleImageDisplayer(short animation, @Nullable DrawableMaker<?> transform) {
                mAnimation = animation;
                mTransform = transform;
            }

            @Override
            public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                Drawable drawable = mTransform != null
                                    ? mTransform.getDrawable(bitmap)
                                    : new BitmapDrawable(imageAware.getWrappedView().getResources(), bitmap);

                if (mAnimation == NOT_ANIMATED || loadedFrom == LoadedFrom.MEMORY_CACHE) {
                    imageAware.setImageDrawable(drawable);
                } else {
                    CrossFadeBitmapDisplayer.setImageDrawableWithXFade(imageAware, drawable, 250);
                }
            }
        }

        private static class MySimpleImageLoadingListener extends SimpleImageLoadingListener {
            @Nullable private final Task<Bitmap>.TaskCompletionSource mTaskSource;
            @Nullable private final OnBitmap listener;

            public MySimpleImageLoadingListener(@Nullable Task<Bitmap>.TaskCompletionSource taskSource, @Nullable OnBitmap listener) {
                mTaskSource = taskSource;
                this.listener = listener;
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (mTaskSource != null) {
                    mTaskSource.setError(new RuntimeException(String.valueOf(failReason), failReason.getCause()));
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null) {
                    listener.bitmap(loadedImage);
                }
                if (mTaskSource != null) {
                    mTaskSource.setResult(loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (mTaskSource != null) {
                    mTaskSource.setCancelled();
                }
            }
        }
    }
}