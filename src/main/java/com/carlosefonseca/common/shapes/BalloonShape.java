package com.carlosefonseca.common.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;

import static java.lang.Math.min;
import static com.carlosefonseca.common.utils.ImageUtils.dp2px;


/**
 * Creates a balloon shape like those on certain messaging apps.
 */
public class BalloonShape extends Shape {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    public static int dist = dp2px(15);
    public static float corner = dp2px(30);

    private int direction = LEFT;

    private Path path;

    /**
     * Creates a new Balloon Shape pointing in the given direction.
     *
     * @param direction Either {@link #LEFT} or {@link #RIGHT}
     */
    public BalloonShape(int direction) {
        this.direction = direction;
    }

    @Override
    protected void onResize(float width, float height) {
        float corner = min(height / 2, BalloonShape.corner);
        if (direction == LEFT) {
            RectF bounds = new RectF(dist, 0, width, height);

            path = new Path();
            // Linha de cima
            path.arcTo(new RectF(-dist, height - (2 * corner), dist, bounds.bottom), 0, 90);
            // Linha de baixo
            path.arcTo(new RectF(-dist - corner, bounds.bottom - (2 * corner), dist + corner, bounds.bottom), 90, -45);

            path.addRoundRect(bounds, corner, corner, Path.Direction.CCW);

            path.close();
        } else if (direction == RIGHT) {
            RectF bounds = new RectF(0, 0, width - dist, height);

            path = new Path();
            // Linha de cima
            path.arcTo(new RectF(bounds.right, height - (2 * corner), width + dist, bounds.bottom), 180, -90);
            // Linha de baixo
            path.arcTo(new RectF(bounds.right - corner, height - (2 * corner), width + dist + corner, bounds.bottom), 90, 45);

            path.addRoundRect(bounds, corner, corner, Path.Direction.CW);

            path.close();
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, paint);
    }
}
