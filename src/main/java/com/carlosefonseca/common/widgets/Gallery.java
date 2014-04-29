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
import android.widget.ImageView;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.ImageUtils;

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

    public void setupWithImageList(List<File> imageList) {
        setAdapter(new GalleryAdapter(imageList));
        layoutInflater = LayoutInflater.from(getContext());
    }

    public void setImageOverlay(int res, int bottomMargin, int rightMargin) {
        this.res = res;
        this.bottomMargin = bottomMargin;
        this.rightMargin = rightMargin;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        clickListener = l;
    }

    public void setImageWidth(int width) {
        this.width = width;
        getLayoutParams().height = (int) (width / 2 + 24 * density);
    }

    class GalleryAdapter extends PagerAdapter {

        private final List<File> imageList;
        private LinkedList<View> recycledViews = new LinkedList<View>();

        GalleryAdapter(List<File> imageList) {
            this.imageList = new ArrayList<File>(imageList);
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = recycledViews.poll();
            if (view == null) {
//                if (res != 0) {
//                    view = layoutInflater.inflate(R.layout.gallery_styled_overlayed_image_view, null);
//                } else
                    view = layoutInflater.inflate(R.layout.gallery_styled_image_view, null);
            }
            if (view != null) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
//                if (res != 0 && ((ImageViewWithOverlay)imageView).getOverlayImage() == 0) {
//                    ((ImageViewWithOverlay)imageView).setOverlayImage(res);
//                    ((ImageViewWithOverlay)imageView).setMarginsToBottomRightCorner(bottomMargin, rightMargin);
//                }


                Bitmap photo = ImageUtils.getCachedPhoto(imageList.get(position), 500, 500, null);
                    imageView.setImageBitmap(photo);
                if (photo != null) {
                    imageView.setBackgroundColor(Color.GRAY);
                } else {
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                }

                imageView.setTag(position);
//                imageView.setAdjustViewBounds(true);
                imageView.setOnClickListener(clickListener);

//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                imageView.getLayoutParams().width = width;
                container.addView(view);//, new ViewGroup.LayoutParams(width, (int) (width / 2 + 24 * density)));
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            recycledViews.add((View) object);
        }
    }


}
