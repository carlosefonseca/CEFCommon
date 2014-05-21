package com.carlosefonseca.common.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

import static com.carlosefonseca.common.utils.ImageUtils.dp2px;


/**
 * Creates a balloon shape like those on certain messaging apps.
 */
public class FoldedBoxShape extends Shape {
    private Path path;

    public FoldedBoxShape() { }

    @Override
    protected void onResize(float width, float height) {
        int h1 = (int) (height * 0.25);
        int point = dp2px(1);
        int hb = (int) (height - h1 - point);

        path = new Path();
        path.moveTo(0, 0);

        path.lineTo(width, 0);
        path.lineTo(width, hb);
        path.lineTo(height * 0.1f, hb);
        path.lineTo(height * 0.1f, hb + point);
        path.lineTo(height * .3f, hb + point);
        path.lineTo(height * .3f, height);
        path.lineTo(0, hb);
        path.close();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, paint);
    }
}