package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.carlosefonseca.common.utils.ImageUtils;
import com.carlosefonseca.common.R;
import org.jetbrains.annotations.NotNull;

/**
 * Draws a Pie-like progress widget.
 * <p/>
 * To update, call {@link #updateTo(double)} with the percentage you want.
 * <p/>
 * Customization possibilities:
 * <ul><li>The color via {@link #setColor(int)} or using the XML tag {@code android:color};</li>
 * <li>The stroke width via {@link #setStrokeWidth(int)} or using the XML tag {@code app:strokeWidth}
 * (xmlns:app="http://schemas.android.com/apk/res-auto")</li></ul>
 */
public class PieView extends ImageView {
    private Paint p;
    private RectF rectF;
    private float currentPosition = 0;
    private float strokeW;
    private Matrix matrix;
    private int pivot;
    private int strokeWidthDp;
    private boolean clear;

    public PieView(Context context) {
        super(context);
        createView();
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
        getXmlProperties(attrs);
    }

    public PieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createView();
        getXmlProperties(attrs);
    }

    /** Obtains the custom XML properties for the NavBar. */
    protected void getXmlProperties(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PieView);
        if (a != null) {
            setColor(a.getColor(R.styleable.PieView_android_color, Color.BLACK));
            setStrokeWidth(a.getDimension(R.styleable.PieView_pieStrokeWidth, 2));
            a.recycle();
        }
    }

    protected void createView() {
        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.WHITE);

        setStrokeWidth(2);

        if (isInEditMode()) updateTo(0.33f);

        rectF = new RectF(0, 0, 0, 0);

        matrix = getImageMatrix();
        if (matrix == null) matrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);
    }

    protected void setStrokeWidth(float dimension) {
        if (isInEditMode()) strokeWidthDp = (int) (dimension / 1);
        strokeW = dimension;
        p.setStrokeWidth(dimension);
    }

    public void setStrokeWidth(int strokeWidthDp) {
        this.strokeWidthDp = strokeWidthDp;
        strokeW = isInEditMode() ? strokeWidthDp : ImageUtils.dp2px(strokeWidthDp);
        p.setStrokeWidth(strokeW);
    }

    public void setColor(int color) {
        p.setColor(color);
    }

    public void updateTo(double percentage) {
        currentPosition = (float) (percentage * 360);
        invalidate();
    }

    public void relativeUpdate(double percentage) {
        currentPosition += percentage * 360;
        invalidate();
    }

    public float getCurrentPercentage() {
        return currentPosition / 360;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int w = right - left;
            matrix.reset();
            matrix.postRotate(270, w / 2, w / 2);
            setScaleType(ScaleType.MATRIX);
            setImageMatrix(matrix);
            float s = (float) Math.ceil((strokeW + 1) / 2);
            rectF.set(s, s, w - s, w - s);
            pivot = (w) / 2;
        }
    }


    @Override
    protected void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.rotate(-90, pivot, pivot);

        // DRAW COLORED CIRCLE
        p.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 0, 360, false, p);


        p.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, 0, currentPosition, true, p);
    }
}
