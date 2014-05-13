package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;

/**
 * Image View that displays a "Pie" graphic.
 * <p/>
 * Use {@link #updateTo(float)} to change the filled part of the graphic.
 */
public class LoadingPieView extends ImageView {
    private Paint p;
    private RectF rectF;
    private float currentPosition = 0;
    private int strokeW;
    private Matrix matrix;
    private int pivot;
    private int strokeWidthDp;
    private boolean clear;

    public LoadingPieView(Context context) {
        super(context);
        createView();
    }

    public LoadingPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public LoadingPieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createView();
    }

    public void createView() {
        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(strokeW);

        setStrokeWidth(2);

        rectF = new RectF(0, 0, 0, 0);

        matrix = getImageMatrix();
        if (matrix == null)
            matrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);
    }

    public void setStrokeWidth(int strokeWidthDp) {
        this.strokeWidthDp = strokeWidthDp;
        strokeW = (int) (getResources().getDisplayMetrics().density * strokeWidthDp);
        p.setStrokeWidth(strokeW);
    }

    public void setColor(int color) {
        p.setColor(color);
    }

    public void updateTo(float percentage) {
        if (percentage * 360 < currentPosition) {
            clear = true;
        }
        currentPosition = percentage * 360;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int w = right - left;
//            matrix = new Matrix();
            matrix.reset();
            matrix.postRotate(270, w / 2, w / 2);
            setScaleType(ScaleType.MATRIX);
            setImageMatrix(matrix);
            rectF.set(strokeW/2, strokeW/2, w-(strokeW/2), w-(strokeW/2));
            pivot = (w) / 2;
        }
    }



    @Override
    protected void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);
//        int width = (int) (canvas.getWidth() * ImageUtils.getDensity() - strokeW);
//        int width = canvas.getWidth();

        canvas.rotate(-90, pivot, pivot);

        if (clear) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            clear = false;
        }

        // DRAW COLORED CIRCLE
        p.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 0, 360, false, p);


        p.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, 0, currentPosition, true, p);
    }
}
