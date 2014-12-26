package com.carlosefonseca.common.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.ImageUtils;
import com.carlosefonseca.common.utils.ZoomZoomableRembrandtController;
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
    private ZoomZoomableRembrandtController zoomRembrandtController;

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
            galleryView.setLayoutParams(galleryView.getLayoutParams());
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

    public void setupWithFiles(List<File> files, Gallery.OnViewItemClickListener<File> clickListener) {
        galleryView.setupWithFiles(files, clickListener);
        afterSetup(files);
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
        hideIfEmpty(galleryView.getAdapter().getCount());
//        hideIfEmpty(urls.size());
        setArrowsForPage(galleryView.getCurrentItem());
    }

    /**
     * Sets a click listener on the images of the Gallery.
     *
     * @param listener
     * @see com.carlosefonseca.common.widgets.Gallery#setOnClickListener(android.view.View.OnClickListener)
     */
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
     */
    public void setAspectRatio(@Nullable Double aspectRatio) {
        this.aspectRatio = aspectRatio;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            if (aspectRatio != null) {
                layoutParams.height = (int) (aspectRatio * layoutParams.width);
                galleryView.getLayoutParams().height = (int) (aspectRatio * layoutParams.width);
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                galleryView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
        requestLayout();
        invalidate();
    }

    public void setAspectRatioFromImage(File image) {
        final double aspectRatio1 = ImageUtils.getAspectRatio(image);
        if (aspectRatio1 != 0) setAspectRatio(aspectRatio1);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        galleryView.setCurrentItem(item, smoothScroll);
    }

    public Gallery getGalleryView() {
        return galleryView;
    }

    public void setBasicZoom(@Nullable Activity activity) {
        if (activity != null) {
            if (zoomRembrandtController == null) {
                ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();
                zoomRembrandtController = new ZoomZoomableRembrandtController(rootView);
            }
            if (galleryView.getAdapter().isUrlList()) {
                galleryView.setOnUrlItemClickListener(new Gallery.OnViewItemClickListener<String>() {
                    @Override
                    public void onClick(View v, String item) {
                        zoomRembrandtController.zoomFromView(((GalleryPage) v).getImageView(), item);
                    }
                });
            } else if (galleryView.getAdapter().isFileList()) {
                galleryView.setOnFileItemClickListener(new Gallery.OnViewItemClickListener<File>() {
                    @Override
                    public void onClick(View v, File item) {
                        zoomRembrandtController.zoomFromView(((GalleryPage) v).getImageView(), item);
                    }
                });
            }
        } else {
            galleryView.setOnClickListener(null);
            zoomRembrandtController = null;
        }
    }
}
