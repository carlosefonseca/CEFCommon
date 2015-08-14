package com.carlosefonseca.common.utils;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class SquareDrawable extends Drawable {

    protected final RectF mRect = new RectF(), mBitmapRect;
    protected final BitmapShader bitmapShader;
    protected final Paint paint;

    public SquareDrawable(Bitmap bitmap) {
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        if (bitmap.getWidth() > bitmap.getHeight()) {
            mBitmapRect = new RectF((bitmap.getWidth() / 2) - (bitmap.getHeight() / 2),
                                    0,
                                    (bitmap.getWidth() / 2) + (bitmap.getHeight() / 2),
                                    bitmap.getHeight());
        } else {
            mBitmapRect = new RectF(0,
                                    (bitmap.getHeight() / 2) - (bitmap.getWidth() / 2),
                                    bitmap.getWidth(),
                                    (bitmap.getHeight() / 2) + (bitmap.getWidth() / 2));
        }

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mRect.set(0, 0, bounds.width(), bounds.height());

        // Resize the original bitmap to fit the new bound
        Matrix shaderMatrix = new Matrix();
        shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.CENTER);
        bitmapShader.setLocalMatrix(shaderMatrix);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(mRect, paint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }
}
