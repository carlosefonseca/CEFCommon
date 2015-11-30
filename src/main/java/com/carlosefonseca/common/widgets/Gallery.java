package com.carlosefonseca.common.widgets;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.apache.commons.collections4.CollectionUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Gallery extends ViewPager {

    private static final java.lang.String TAG = CodeUtils.getTag(Gallery.class);

    private int bottomMargin;
    private int rightMargin;
    private int width;
    private float density;
    private int duration = -1;
    private ImageView.ScaleType scaleType;

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
        setOffscreenPageLimit(2);
    }

    /**
     * Scale in X and Y independently, so that image matches view exactly.
     * This may change the aspect ratio of the image.
     */
    public void stretch() {
        this.scaleType = ImageView.ScaleType.FIT_XY;
    }

    /**
     * Maintains the image aspect ratio but scales so that the image fits entirely inside the view (no crop).
     */
    public void scaleDown() {
        this.scaleType = ImageView.ScaleType.FIT_CENTER;
    }

    /**
     * Scales up the image so that it fills the view entirely. Maintains aspect ratio. Centers the image on the view.
     */
    public void scaleUp() {
        this.scaleType = ImageView.ScaleType.CENTER_CROP;
    }

    public void setScaleType(ImageView.ScaleType scaleType) {this.scaleType = scaleType;}

    @NonNull
    public GalleryAdapter getOrCreateAdapter() {
        GalleryAdapter adapter = getAdapter();
        if (adapter == null) {
            adapter = new GalleryAdapter(getContext(), scaleType /*rembrandt*/);
            setAdapter(adapter);
        }
        return adapter;
    }

    @Override
    public GalleryAdapter getAdapter() {
        return (GalleryAdapter) super.getAdapter();
    }

    public void setupWithImageList(Collection<File> imageList) {
        GalleryAdapter adapter = getOrCreateAdapter();
        adapter.withFileList(imageList);
        adapter.notifyDataSetChanged();
    }

    public void setupWithUrlList(@Nullable Collection<String> imageList) {
        GalleryAdapter adapter = getOrCreateAdapter();
        adapter.withUrlList(imageList);
        adapter.notifyDataSetChanged();
    }

    public void setImageOverlay(int res, int bottomMargin, int rightMargin) {
        this.bottomMargin = bottomMargin;
        this.rightMargin = rightMargin;
    }

    /**
     * Sets a click listener on the images.
     */
    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        getOrCreateAdapter().setClickListener(listener);
    }

    public void setImageWidth(int width) {
        this.width = width;
        getLayoutParams().height = (int) (width / 2 + 24 * density);
    }

    public static class GalleryAdapter extends PagerAdapter implements OnClickListener {

        @Nullable private List<File> imageList;
        @Nullable private ArrayList<String> urlList;
        private LinkedList<View> recycledViews = new LinkedList<View>();
        private LayoutInflater layoutInflater;
        @Nullable private OnClickListener clickListener;
        //        protected int gallery_layout = R.layout.gallery_styled_image_view;
        protected int gallery_layout = R.layout.gallery_page;
        private ImageView.ScaleType scaleType;

        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v);
        }

        public GalleryAdapter(Context context) {
            this(context, null);
        }

        protected GalleryAdapter(Context context, @Nullable ImageView.ScaleType scaleType) {
            this.scaleType = scaleType;
            layoutInflater = LayoutInflater.from(context);
        }

        public void setClickListener(@Nullable OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public GalleryAdapter withFileList(Collection<File> imageList) {
            this.imageList = new ArrayList<>(imageList);
            this.urlList = null;
            return this;
        }

        public GalleryAdapter withUrlList(@Nullable Collection<String> imageList) {
            this.urlList = imageList == null ? new ArrayList<String>() : new ArrayList<>(imageList);
            ListUtils.removeNullElements(this.urlList);
            this.imageList = null;
            return this;
        }

        @Override
        public int getItemPosition(Object view) {
            int i = -1;
            Object object = ((View) view).getTag();
            if (imageList != null && object instanceof File) {
                i = imageList.indexOf(object);
            } else if (urlList != null && object instanceof String) {
                i = urlList.indexOf(object);
            }
            return i == -1 ? POSITION_NONE : i;
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
            final ImageView imageView;
            GalleryPage view = (GalleryPage) recycledViews.poll();
            if (view == null) {
                view = (GalleryPage) layoutInflater.inflate(gallery_layout, container, false);
                imageView = view.imageView;
                if (scaleType != null) view.setScaleType(scaleType);
            } else {
                imageView = view.imageView;
            }

            String uri = null;
            if (urlList != null) {
                String url = urlList.get(position);
                view.set(url, position);
                uri = UIL.getUri(url);
            } else if (imageList != null) {
                File file = imageList.get(position);
                view.set(file, position);
                uri = UIL.getUri(file);
            }

            ImageLoader.getInstance()
                       .displayImage(uri, imageView, UIL.mOptionsForPhotos, new SimpleImageLoadingListener() {
                           @Override
                           public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                               imageView.setBackgroundColor(Color.GRAY);
                           }

                           @Override
                           public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                               ResourceUtils.setBackground(imageView, null);
                           }
                       });

            view.setOnClickListener(this);

            container.addView(view);

            return view;
        }

        @Nullable
        private OnClickListener getClickListener() {
            return clickListener;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            recycledViews.add((View) object);
        }

        public boolean isFileList() { return CollectionUtils.isNotEmpty(this.imageList); }

        public boolean isUrlList() { return CollectionUtils.isNotEmpty(this.urlList); }

        @Nullable
        public List<File> getImageList() {
            return imageList;
        }

        @Nullable
        public List<String> getUrlList() {
            return urlList;
        }

        public void clear() {
            imageList = null;
            urlList = null;
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener<T> {
        void onClick(T item);
    }

    public interface OnViewItemClickListener<T> {
        void onClick(View v, T item);
    }

    public <T> void setupWithUrlsForObjects(List<String> urls,
                                            final List<T> objects,
                                            final OnItemClickListener<T> clickListener) {
        setupWithUrlList(urls);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // View tag stores the original object
                // Image View tag stores the position
                int position = getIndexOfGalleryPage(v);
                clickListener.onClick(objects.get(position));
            }
        });
    }

    private static int getIndexOfGalleryPage(View v) {
        return v instanceof GalleryPage ? ((GalleryPage) v).getPosition() : (int) v.findViewById(R.id.image).getTag();
    }

    public void setupWithFiles(List<File> files, final OnViewItemClickListener<File> clickListener) {
        setupWithImageList(files);
        setOnFileItemClickListener(clickListener);
    }

    public void setupWithUrls(List<String> list, final OnViewItemClickListener<String> clickListener) {
        setupWithUrlList(list);
        setOnUrlItemClickListener(clickListener);
    }

    protected void setOnFileItemClickListener(final OnViewItemClickListener<File> clickListener) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v, ((GalleryPage) v).file);
            }
        });
    }

    protected void setOnUrlItemClickListener(final OnViewItemClickListener<String> clickListener) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v, ((GalleryPage) v).url);
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
