package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.carlosefonseca.common.utils.UIL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ZoomableArrayAdapter extends PagerAdapter {

    private final Context context;
    private final List<File> objects;
    private final View.OnClickListener onClickListener;


    public ZoomableArrayAdapter(Context context, List<File> objects, View.OnClickListener onClickListener) {
        this.context = context;
        this.objects = objects != null ? objects : new ArrayList<File>();
        this.onClickListener = onClickListener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imageView = new TouchImageView(context);
        Bitmap photo = UIL.loadSync(UIL.getUri(objects.get(position)));
        imageView.setImageBitmap(photo);
        container.addView(imageView, MATCH_PARENT, MATCH_PARENT);
        if (onClickListener != null) imageView.setOnClickListener(onClickListener);
        return imageView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}