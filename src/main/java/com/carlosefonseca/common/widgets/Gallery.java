package com.carlosefonseca.common.widgets;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.Rembrandt;
import com.carlosefonseca.common.utils.TaskUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Gallery extends ViewPager {

    private LayoutInflater layoutInflater;
    private int res;
    private int bottomMargin;
    private int rightMargin;
    private OnClickListener clickListener;
    private int width;
    private float density;
    private boolean scaling;

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

    public void setupWithImageList(List<File> imageList) {
        setAdapter(new GalleryAdapter().withFileList(imageList));
        layoutInflater = LayoutInflater.from(getContext());
    }

    public void setupWithUrlList(List<String> imageList) {
        setAdapter(new GalleryAdapter().withUrlList(imageList));
        layoutInflater = LayoutInflater.from(getContext());
    }

    public void setImageOverlay(int res, int bottomMargin, int rightMargin) {
        this.res = res;
        this.bottomMargin = bottomMargin;
        this.rightMargin = rightMargin;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    public void setImageWidth(int width) {
        this.width = width;
        getLayoutParams().height = (int) (width / 2 + 24 * density);
    }

    class GalleryAdapter extends PagerAdapter {

        private final Rembrandt rembrandt;
        @Nullable private List<File> imageList;
        @Nullable private ArrayList<String> urlList;
        private LinkedList<View> recycledViews = new LinkedList<View>();

        GalleryAdapter() {
            rembrandt = new Rembrandt(getContext());
        }

        public GalleryAdapter withFileList(List<File> imageList) {
            this.imageList = new ArrayList<>(imageList);
            this.urlList = null;
            return this;
        }

        public GalleryAdapter withUrlList(List<String> imageList) {
            this.urlList = new ArrayList<>(imageList);
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
        public Object instantiateItem(ViewGroup container, int position) {
            View view = recycledViews.poll();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.gallery_styled_image_view, container, false);
                if (scaling) {
                    ((ImageView) ((ViewGroup) view).getChildAt(0)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
}
