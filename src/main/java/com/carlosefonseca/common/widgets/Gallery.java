package com.carlosefonseca.common.widgets;


import android.content.Context;
import android.graphics.*;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Gallery extends ViewPager {

    private static final java.lang.String TAG = CodeUtils.getTag(Gallery.class);

    private int res;
    private int bottomMargin;
    private int rightMargin;
    private int width;
    private float density;
    private boolean scaling;
    private int duration = -1;

    public Gallery(Context context) {
        super(context);
        init();
    }

    public Gallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
    }

    public boolean isScaling() {
        return scaling;
    }

    public void setScaling(boolean scaling) {
        this.scaling = scaling;
    }

    public void setupWithImageList(Collection<File> imageList) {
        setAdapter(new GalleryAdapter(getContext(), scaling).withFileList(imageList));
    }

    public void setupWithUrlList(Collection<String> imageList) {
        setAdapter(new GalleryAdapter(getContext(), scaling).withUrlList(imageList));
    }

    public void setImageOverlay(int res, int bottomMargin, int rightMargin) {
        this.res = res;
        this.bottomMargin = bottomMargin;
        this.rightMargin = rightMargin;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        ((GalleryAdapter) getAdapter()).setClickListener(listener);
    }

    public void setImageWidth(int width) {
        this.width = width;
        getLayoutParams().height = (int) (width / 2 + 24 * density);
    }


    public static class GalleryAdapter extends PagerAdapter {

        private final Rembrandt rembrandt;
        private final boolean scaling;
        @Nullable private List<File> imageList;
        @Nullable private ArrayList<String> urlList;
        private LinkedList<View> recycledViews = new LinkedList<View>();
        private LayoutInflater layoutInflater;
        private OnClickListener clickListener;

        public GalleryAdapter(Context context) {
            this(context, false);
        }

        GalleryAdapter(Context context, boolean scaling) {
            this.scaling = scaling;
            rembrandt = new Rembrandt(context);
            layoutInflater = LayoutInflater.from(context);
        }

        public void setClickListener(OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public GalleryAdapter withFileList(Collection<File> imageList) {
            this.imageList = new ArrayList<>(imageList);
            this.urlList = null;
            return this;
        }

        public GalleryAdapter withUrlList(Collection<String> imageList) {
            this.urlList = new ArrayList<>(imageList);
            ListUtils.removeNullElements(this.urlList);
            this.imageList = null;
            return this;
        }

        @Override
        public int getCount() {
            return imageList != null ? imageList.size() : urlList != null ? urlList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = recycledViews.poll();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.gallery_styled_image_view, container, false);
                if (scaling) {
                    ((ImageView) ((ViewGroup) view).getChildAt(0)).setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
            if (view != null) {
                final ImageView imageView = (ImageView) view.findViewById(R.id.image);

                if (urlList != null) {
                    rembrandt.load(urlList.get(position));
                } else if (imageList != null) {
                    rembrandt.load(imageList.get(position));
                }
                rembrandt.fadeIn(imageView).continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        if (task.getError() != null) {
                            imageView.setBackgroundColor(Color.GRAY);
                        } else {
                            imageView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(TaskUtils.LogErrorContinuation);

                imageView.setTag(position);
                imageView.setOnClickListener(clickListener);

                container.addView(view);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            recycledViews.add((View) object);
        }
    }


    public interface OnItemClickListener<T> {
        void onClick(T item);
    }

    public <T> void setupWithUrlsForObjects(List<String> urls,
                                            final List<T> objects,
                                            final OnItemClickListener<T> clickListener) {
        setupWithUrlList(urls);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(objects.get((Integer) v.getTag()));
            }
        });
    }



    /* CUSTOM SCROLL */



    public static class CustomSpeedScroller extends Scroller {

        private int mDuration;

        public CustomSpeedScroller(Context context, int duration) {
            super(context);
            this.mDuration = duration;
        }


        public CustomSpeedScroller(Context context, Interpolator interpolator, int duration) {
            super(context, interpolator);
            this.mDuration = duration;
        }


        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }


    public void setScrollDuration(int duration) {
        if (this.duration == duration) return;
        this.duration = duration;
        setScroller(new CustomSpeedScroller(getContext(), duration));
    }

    public void setScrollInterpolatorAndDuration(Interpolator interpolator, int duration) {
        this.duration = duration;
        setScroller(new CustomSpeedScroller(getContext(), interpolator, duration));
    }

    public void setScroller(Scroller scroller) {
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            // scroller.setFixedDuration(5000);
            mScroller.set(this, scroller);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {
            Log.w(TAG, ignored);
        }
        this.setOffscreenPageLimit(2);
    }
}
