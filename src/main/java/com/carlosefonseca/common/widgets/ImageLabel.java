package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;

/**
 * Custom view that contains an {@link android.widget.ImageView} and an {@link com.carlosefonseca.common.widgets
 * .AutoResizeTextView}. It manages layout in a way that you can set only the text or
 * only the image and they will be centered. If both label and image are set, the label will be bellow the image.
 * Selected state will be passed on to the views so you can attach selected background states or text color states.
 * Works nice on tabs.
 */

public class ImageLabel extends LinearLayout {

    ImageView imageView;
    AutoResizeTextView labelView;

    public ImageLabel(Context context) {
        super(context);
        init();
    }

    public ImageLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        imageView = new ImageView(getContext());
        imageView.setVisibility(GONE);

        labelView = new AutoResizeTextView(getContext());
        labelView.setGravity(Gravity.CENTER_HORIZONTAL);
        labelView.setVisibility(GONE);
        labelView.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        addView(imageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        addView(labelView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    ////////////////////////////////////////////////////////

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageDrawable(Drawable drawable) {
        imageView.setVisibility(drawable == null ? GONE : VISIBLE);
        imageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        imageView.setVisibility(bm == null ? GONE : VISIBLE);
        imageView.setImageBitmap(bm);

    }

    public void setImageResource(int resId) {
        imageView.setVisibility(resId == 0 ? GONE : VISIBLE);
        imageView.setImageResource(resId);
    }

    ////////////////////////////////////////////////////////

    public TextView getLabelView() {
        return labelView;
    }

    public void setText(CharSequence cs) {
        labelView.setVisibility(StringUtils.isEmpty(cs) ? GONE : VISIBLE);
        labelView.setText(cs);
    }

    public void setTextColor(ColorStateList stateList) {
        labelView.setTextColor(stateList);
    }

    public void setTextColor(int color) {
        labelView.setTextColor(color);
    }

    public void setTextSize(float size) {
        labelView.setTextSize(size);
    }

    public void setTextSize(int unit, float size) {
        labelView.setTextSize(unit, size);
    }

    public void setTextSizeRes(int dimensionResource) {
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelView.getResources().getDimension(dimensionResource));
    }

    ////////////////////////////////////////////////////////


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        labelView.setSelected(selected);
        imageView.setSelected(selected);
    }
}
