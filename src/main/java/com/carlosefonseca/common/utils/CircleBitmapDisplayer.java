/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.carlosefonseca.common.utils;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Can display bitmap with rounded corners. This implementation works only with ImageViews wrapped
 * in ImageViewAware.
 * <br />
 * This implementation is inspired by
 * <a href="http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/">
 * Romain Guy's article</a>. It rounds images using custom drawable drawing. Original bitmap isn't changed.
 * <br />
 * <br />
 * If this implementation doesn't meet your needs then consider
 * <a href="https://github.com/vinc3m1/RoundedImageView">RoundedImageView</a> or
 * <a href="https://github.com/Pkmmte/CircularImageView">CircularImageView</a> projects for usage.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.5.6
 */
public class CircleBitmapDisplayer implements BitmapDisplayer, DrawableMaker<CircleBitmapDisplayer.CircleDrawable> {

    protected final int margin;

    public CircleBitmapDisplayer() {
        this(0);
    }

    public CircleBitmapDisplayer(int marginPixels) {
        this.margin = marginPixels;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        imageAware.setImageDrawable(getDrawable(bitmap));
    }

    @NonNull
    public CircleDrawable getDrawable(@NonNull Bitmap bitmap) {return new CircleDrawable(bitmap, margin);}

    public static class CircleDrawable extends Drawable {

        protected float cornerRadius;
        protected final int margin;

        protected final RectF mRect = new RectF(), mBitmapRect;
        protected final BitmapShader bitmapShader;
        protected final Paint paint;

        public CircleDrawable(Bitmap bitmap, int margin) {
            this.margin = margin;

            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            if (bitmap.getWidth() > bitmap.getHeight()) {
                mBitmapRect = new RectF((bitmap.getWidth() / 2) - (bitmap.getHeight() / 2 - margin),
                                        margin,
                                        (bitmap.getWidth() / 2) + (bitmap.getHeight() / 2 - margin),
                                        bitmap.getHeight() - margin);
            } else {
                mBitmapRect = new RectF(margin,
                                        (bitmap.getHeight() / 2) - (bitmap.getWidth() / 2 - margin),
                                        bitmap.getWidth() - margin,
                                        (bitmap.getHeight() / 2) + (bitmap.getWidth() / 2 - margin));
            }

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);
            cornerRadius = (bounds.width() - margin) / 2f;

            // Resize the original bitmap to fit the new bound
            Matrix shaderMatrix = new Matrix();
            shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
            bitmapShader.setLocalMatrix(shaderMatrix);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
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
}
