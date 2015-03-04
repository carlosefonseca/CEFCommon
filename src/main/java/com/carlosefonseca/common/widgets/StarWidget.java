package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import com.carlosefonseca.common.utils.CodeUtils;

public class StarWidget extends ImageView {

    private static final String TAG = CodeUtils.getTag(StarWidget.class);
    private RectF rectF = new RectF();
    private Point centerPoint = new Point(0,0);
    private Paint p;
    private int color = Color.RED;

    public StarWidget(Context context) {
        super(context);
        setup();
    }

    public StarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public StarWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.CENTER_INSIDE);

        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawPath(createStar(22, centerPoint, centerPoint.x, centerPoint.x * 0.8), p);
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rectF.set(left, top, right, bottom);
        centerPoint.set((int) rectF.width()/2, (int) rectF.height()/2);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        p.setColor(color);
        invalidate();
    }

    public static Path createStar(int arms, Point center, double rOuter, double rInner) {
        double angle = Math.PI / arms;

        Path path = new Path();

        for (int i = 0; i < 2 * arms; i++) {
            double r = (i & 1) == 0 ? rOuter : rInner;
            Point p = new Point(((int) (center.x + Math.cos(i * angle) * r)), (int) (center.y + Math.sin(i * angle) * r));
            if (i == 0) {
                path.moveTo(p.x, p.y);
            } else {
                path.lineTo(p.x, p.y);
            }
        }
        path.close();
        return path;
    }
}
