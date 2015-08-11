package com.carlosefonseca.common.utils;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import bolts.Task;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.carlosefonseca.common.utils.ImageUtils.dp2px;

/**
 * This class is basically a re-packaging of multiple Bitmap methods in ImageUtils, in an interface similar to
 * Square's Picasso. It doesn't have half the features of Picasso but does what I need, which is downloading, caching
 * and setting on ImageViews.
 */
public class Gogh {
    private static final short NOT_ANIMATED = 0;
    private static final short FADE_IN = 1;
    private static final short CROSS_FADE = 2;
    public static final int DP_5 = dp2px(5);

    private String mUrl;
    private int mPlaceholder;
    private BitmapDisplayer mDisplayer;
    private ImageView mView;
    private short mAnimation;
    private DisplayImageOptions mOptions;
    private boolean mHideIfNull;
    private OnBitmap listener;
    private OnBitmap mOnBitmap;
    private Task<Bitmap>.TaskCompletionSource mTaskSource;


    private Gogh(String url) {
        mUrl = StringUtils.stripToNull(url);
    }

    public static Gogh loadPhotoURI(@Nullable String url) {
        return new Gogh(url);
    }

    public static Gogh loadIconURI(@Nullable String url) {
        return new Gogh(url);
    }

    public static Gogh load(@Nullable String url) {
        return new Gogh(UIL.getUri(url));
    }

    public static Gogh load(@Nullable File file) {
        return new Gogh(UIL.getUri(file));
    }

    public static Gogh loadPhoto(@Nullable String url) {
        return new Gogh(UIL.getUri(url)).photo();
    }

    public static Gogh loadIcon(@Nullable File file) {
        return new Gogh(UIL.getUri(file)).icon();
    }

    public static Gogh loadIcon(@Nullable String url) {
        return new Gogh(UIL.getUri(url)).icon();
    }

    private Gogh photo() {
        mOptions = UIL.mOptionsForPhotos;
        return this;
    }

    private Gogh icon() {
        mOptions = UIL.mOptionsForIcons;
        return this;
    }

    public Gogh placeholder(int drawable) {
        this.mPlaceholder = drawable;
        return this;
    }

    public Gogh roundCorners(int radius) {
        return displayer(GoghHelper.getRoundCornersBitmapDisplayer(radius));
    }

    public Gogh circle() {
        return displayer(GoghHelper.getCircleBitmapDisplayer(0));
    }

    public Gogh circle(boolean circle) {
        return circle ? displayer(GoghHelper.getCircleBitmapDisplayer(0)) : displayer(null);
    }

    private Gogh displayer(BitmapDisplayer displayer) {
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

    public void into(final ImageView view, short animated) {
        mView = view;
        mAnimation = animated;
        GoghHelper.run(this);
    }

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



    public static class GoghHelper {
        private static final String TAG = CodeUtils.getTag(GoghHelper.class);
        public static final int XFADE_MILLIS = 500;
        private static final int FADE_MILLIS = 100;


        protected static void run(Gogh g) {
            if (g.mHideIfNull) {
                if (g.mUrl == null) {
                    g.mView.setVisibility(View.GONE);
                } else {
                    g.mView.setVisibility(View.VISIBLE);
                }
            } else if (g.mPlaceholder != 0) {
                if (g.mUrl == null) {
                    g.mView.setImageResource(g.mPlaceholder);
                }
            }

            ImageLoadingListener imageLoadingListener = null;
            switch (g.mAnimation) {
                case NOT_ANIMATED:
                    imageLoadingListener = null;
                    break;
                case FADE_IN:
                    imageLoadingListener = animateFirstDisplayListener;
                    break;
                case CROSS_FADE:
                    imageLoadingListener = crossFadeDisplayListener;
                    break;
            }
            if (g.mOnBitmap != null || g.mTaskSource != null) {
                imageLoadingListener = new MySimpleImageLoadingListener(g.mTaskSource, imageLoadingListener, g.mOnBitmap);
            }

            if (g.mDisplayer != null) {
                g.mOptions = new DisplayImageOptions.Builder().cloneFrom(g.mOptions).displayer(g.mDisplayer).build();
            }

            UIL.display(g.mUrl, g.mView, imageLoadingListener, g.mOptions);
        }

        static LruCache<Integer, RoundedBitmapDisplayer> sRoundCornersBitmapDisplayerCache =
                new LruCache<Integer, RoundedBitmapDisplayer>(5) {
                    @Override
                    protected RoundedBitmapDisplayer create(Integer key) {
                        return new RoundedBitmapDisplayer(key);
                    }
                };
        static LruCache<Integer, CircleBitmapDisplayer> sCircleBitmapDisplayerCache =
                new LruCache<Integer, CircleBitmapDisplayer>(5) {
                    @Override
                    protected CircleBitmapDisplayer create(Integer key) {
                        return new CircleBitmapDisplayer(key);
                    }
                };

        public static BitmapDisplayer getRoundCornersBitmapDisplayer(int cornerRadius) {
            return sRoundCornersBitmapDisplayerCache.get(cornerRadius);
        }


        public static CircleBitmapDisplayer getCircleBitmapDisplayer(int margin) {
            return sCircleBitmapDisplayerCache.get(margin);
        }


        static SimpleImageLoadingListener animateFirstDisplayListener = new SimpleImageLoadingListener() {

            final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) {
                    final ImageView imageView = (ImageView) view;
                    boolean firstDisplay = !displayedImages.contains(imageUri);
                    if (firstDisplay) {
                        CodeUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                FadeInBitmapDisplayer.animate(imageView, 500);
                                displayedImages.add(imageUri);
                            }
                        });
                    }
                }
            }
        };

        static SimpleImageLoadingListener crossFadeDisplayListener = new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(final String imageUri, View view, final Bitmap loadedImage) {
                if (loadedImage != null) {
                    final ImageView imageView = (ImageView) view;
                    CodeUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            CrossFadeBitmapDisplayer.animate(imageView, loadedImage, 500);
                        }
                    });
                }
            }
        };

        private static class MySimpleImageLoadingListener extends SimpleImageLoadingListener {
            @Nullable private final Task<Bitmap>.TaskCompletionSource mTaskSource;
            @Nullable ImageLoadingListener base;
            @Nullable OnBitmap listener;

            public MySimpleImageLoadingListener(@Nullable ImageLoadingListener base, @Nullable OnBitmap listener) {
                this.base = base;
                this.listener = listener;
                mTaskSource = null;
            }

            public MySimpleImageLoadingListener(@Nullable Task<Bitmap>.TaskCompletionSource taskSource, @Nullable ImageLoadingListener base, @Nullable OnBitmap listener) {
                mTaskSource = taskSource;
                this.base = base;
                this.listener = listener;
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (base != null) {
                    base.onLoadingFailed(imageUri, view, failReason);
                }
                if (mTaskSource != null) {
                    mTaskSource.setError(new RuntimeException(String.valueOf(failReason), failReason.getCause()));
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null) {
                    listener.bitmap(loadedImage);
                }
                if (base != null) {
                    base.onLoadingComplete(imageUri, view, loadedImage);
                }
                if (mTaskSource != null) {
                    mTaskSource.setResult(loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (base != null) {
                    base.onLoadingCancelled(imageUri, view);
                }
                if (mTaskSource != null) {
                    mTaskSource.setCancelled();
                }
            }
        }
    }
}