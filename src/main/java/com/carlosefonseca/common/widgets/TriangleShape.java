package com.carlosefonseca.common.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

public class TriangleShape extends Shape {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    private Path path;
    private final int pointing;

    /**
     * Creates a Triangle Shape
     * @param pointing Either {@link #UP}, {@link #RIGHT}, {@link #DOWN} or {@link #LEFT}
     */
    public TriangleShape(int pointing) {
        this.pointing = pointing;
    }

    @Override
    protected void onResize(float width, float height) {
        path = new Path();
        switch (pointing) {
            case UP:
                path.moveTo(0, height);
                path.lineTo(width / 2, 0);
                path.lineTo(width, height);
                break;

            case RIGHT:
                path.moveTo(0, 0);
                path.lineTo(width, height / 2);
                path.lineTo(0, height);
                break;

            case DOWN:
                path.moveTo(0, 0);
                path.lineTo(width / 2, height);
                path.lineTo(width, 0);
                break;

            case LEFT:
                path.moveTo(width, 0);
                path.lineTo(0, height / 2);
                path.lineTo(width, height);
                break;
        }
        path.close();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (path == null) onResize(canvas.getClipBounds().right, canvas.getClipBounds().bottom);
        canvas.drawPath(path, paint);
    }
}
