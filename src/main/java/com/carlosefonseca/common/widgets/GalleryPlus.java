package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.ImageUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class GalleryPlus extends FrameLayout {

    private Gallery galleryView;
    private ImageView arrowLeftView;
    private ImageView arrowRightView;
    private boolean hideIfEmpty;
    @Nullable private Double aspectRatio;

    public GalleryPlus(Context context) {
        super(context);
        init();
    }

    public GalleryPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.cef_gallery_plus, this);
        galleryView = (Gallery) findViewById(R.id.gallery);
        arrowLeftView = (ImageView) findViewById(R.id.arrow_left);
        arrowRightView = (ImageView) findViewById(R.id.arrow_right);
        galleryView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) { setArrowsForPage(position); }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    protected void setArrowsForPage(int position) {
        final int count = galleryView.getAdapter().getCount();
        if (count > 1) {
            if (position == 0 && arrowLeftView.getVisibility() == VISIBLE) {
                arrowLeftView.setVisibility(GONE);
            } else if (position > 0 && arrowLeftView.getVisibility() == GONE) {
                arrowLeftView.setVisibility(VISIBLE);
            }
            if (position == count - 1 && arrowRightView.getVisibility() == VISIBLE) {
                arrowRightView.setVisibility(GONE);
            } else if (position < count - 1 && arrowRightView.getVisibility() == GONE) {
                arrowRightView.setVisibility(VISIBLE);
            }
        } else {
            arrowLeftView.setVisibility(GONE);
            arrowRightView.setVisibility(GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (aspectRatio != null) {
            final int h = (int) (getMeasuredWidth() * aspectRatio);
            galleryView.getLayoutParams().height = h;
            setMeasuredDimension(widthMeasureSpec, h);
        }
    }

    public boolean isEmpty() {
        return galleryView.getAdapter().getCount() == 0;
    }


/*
    protected void setSingleImage(boolean singleImage) {
        arrowLeftView.setVisibility(singleImage ? GONE : VISIBLE);
        arrowRightView.setVisibility(singleImage ? GONE : VISIBLE);
    }
*/

    public void setupWithImageList(Collection<File> imageList) {
        galleryView.setupWithImageList(imageList);
        afterSetup(imageList);
    }

    public void setupWithUrlList(Collection<String> imageList) {
        galleryView.setupWithUrlList(imageList);
        afterSetup(imageList);
    }

    public <T> void setupWithUrlsForObjects(List<String> urls,
                                            List<T> objects,
                                            Gallery.OnItemClickListener<T> clickListener) {
        galleryView.setupWithUrlsForObjects(urls, objects, clickListener);
        afterSetup(urls);
    }

    protected void afterSetup(Collection<?> urls) {
//        setSingleImage(urls.size() < 2);
        hideIfEmpty(urls.size());
        setArrowsForPage(galleryView.getCurrentItem());
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        galleryView.setOnClickListener(listener);
    }

    private void hideIfEmpty(int size) {
        setVisibility(hideIfEmpty && size == 0 ? GONE : VISIBLE);
    }

    public boolean isHideIfEmpty() {
        return hideIfEmpty;
    }

    public void setHideIfEmpty(boolean hideIfEmpty) {
        this.hideIfEmpty = hideIfEmpty;
    }

    @Nullable
    public Double getAspectRatio() {
        return aspectRatio;
    }

    /**
     * Ex: 3/5 <-> height / width
     *
     * @param aspectRatio
     */
    public void setAspectRatio(@Nullable Double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setAspectRatioFromImage(File image) {
        final double aspectRatio1 = ImageUtils.getAspectRatio(image);
        if (aspectRatio1 != 0) setAspectRatio(aspectRatio1);
    }

    public void setScaling(boolean scaling) {
        galleryView.setScaling(scaling);
    }

    public boolean isScaling() {
        return galleryView.isScaling();
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        galleryView.setCurrentItem(item, smoothScroll);
    }
}
