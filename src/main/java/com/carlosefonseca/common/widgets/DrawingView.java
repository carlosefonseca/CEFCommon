package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.support.annotation.NonNull;

/**
 * Provides a view that allows simple finger drawing. Also provides ways to extract a bitmap with the drawing and a
 * way to clear the drawing.
 */
public class DrawingView extends View {

    protected Paint mPaint = new Paint();
    protected Bitmap mBitmap;
    protected Canvas mCanvas;
    protected Path mPath = new Path();
    protected Paint mBitmapPaint;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Math.max(2.0f, 2.0f * getResources().getDisplayMetrics().density));
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        reset(w, h);
    }

    private void reset(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((int) getResources().getDisplayMetrics().density);
        mCanvas.drawRect(0, 0, w - 1, h - 1, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        mPath.reset();
        reset(mBitmap.getWidth(), mBitmap.getHeight());
        invalidate();
    }

    public Bitmap getBitmap() {
        final int margin = (int) getResources().getDisplayMetrics().density;
        return Bitmap.createBitmap(mBitmap,
                                   margin,
                                   margin,
                                   mBitmap.getWidth() - (2 * margin),
                                   mBitmap.getHeight() - (2 * margin));
    }

    public Bitmap getCroppedBitmap() {
        final int margin = (int) getResources().getDisplayMetrics().density;
        final Rect frame = new Rect();
        // top
        topLoop:
        for (int y = margin; y < mBitmap.getHeight() - margin; y++) {
            for (int x = margin; x < mBitmap.getWidth() - margin; x++) {
                if (mBitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    frame.top = y;
                    break topLoop;
                }
            }
        }
        // bottom
        bottomLoop:
        for (int y = mBitmap.getHeight() - 1 - margin; y >= margin && y > frame.top; y--) {
            for (int x = margin; x < mBitmap.getWidth() - margin; x++) {
                if (mBitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    frame.bottom = y;
                    break bottomLoop;
                }
            }
        }
        // left
        leftLoop:
        for (int x = margin; x < mBitmap.getWidth() - 1 - margin; x++) {
            for (int y = frame.top; y < frame.bottom; y++) {
                if (mBitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    frame.left = x;
                    break leftLoop;
                }
            }
        }
        rightLoop:
        for (int x = mBitmap.getWidth() - 1 - margin; x >= margin && x > frame.left; x--) {
            for (int y = frame.top; y < frame.bottom; y++) {
                if (mBitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    frame.right = x;
                    break rightLoop;
                }
            }
        }
        return Bitmap.createBitmap(mBitmap, frame.left, frame.top, frame.width(), frame.height());
    }
}
