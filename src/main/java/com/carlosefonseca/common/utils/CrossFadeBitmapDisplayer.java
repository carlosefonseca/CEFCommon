/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich, Daniel Martí
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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.*;
import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Displays image with "fade in" animation
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com), Daniel Martí
 * @since 1.6.4
 */
public class CrossFadeBitmapDisplayer implements BitmapDisplayer {

    private final int durationMillis;


    /**
     * @param durationMillis Duration of "fade-in" animation (in milliseconds)
     */
    public CrossFadeBitmapDisplayer(int durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        BitmapDrawable drawable = new BitmapDrawable(imageAware.getWrappedView().getContext().getResources(), bitmap);

        setImageDrawableWithXFade(imageAware, drawable, durationMillis);
    }

    public void display(Drawable drawable, ImageAware imageAware, LoadedFrom loadedFrom) {
        setImageDrawableWithXFade(imageAware, drawable, durationMillis);
    }

    /**
     * Animates {@link ImageView} with "fade-in" effect
     *
     * @param imageView      {@link ImageView} which display image in
     * @param drawable
     * @param durationMillis The length of the animation in milliseconds
     */
    public static void animate(View imageView, Drawable drawable, int durationMillis) {
        if (imageView != null) {
            setImageDrawableWithXFade((ImageView) imageView, drawable, durationMillis);
        }
    }

    /**
     * Animates {@link ImageView} with "fade-in" effect
     *
     * @param imageView      {@link ImageView} which display image in
     * @param drawable
     * @param durationMillis The length of the animation in milliseconds
     */
    public static void animate(View imageView, Bitmap bitmap, int durationMillis) {
        if (imageView != null) {
            BitmapDrawable drawable = new BitmapDrawable(imageView.getContext().getResources(), bitmap);
            setImageDrawableWithXFade((ImageView) imageView, drawable, durationMillis);
        }
    }

    public static void setImageDrawableWithXFade(final ImageView imageView, final Drawable drawable, int millis) {
        setImageDrawableWithXFade(null, imageView, drawable, millis);
    }

    public static void setImageDrawableWithXFade(final ImageAware imageAware, final Drawable drawable, int millis) {
        setImageDrawableWithXFade(imageAware, null, drawable, millis);
    }

    protected static void setImageDrawableWithXFade(final ImageAware imageAware, ImageView imageView, final Drawable drawable, int millis) {
        if (imageAware != null) {
            imageView = (ImageView) imageAware.getWrappedView();
        }

        Drawable currentDrawable = imageView.getDrawable();
        if (currentDrawable == null) {
            currentDrawable = new ColorDrawable(Color.TRANSPARENT);
        } else if (currentDrawable instanceof TransitionDrawable) {
            currentDrawable = ((TransitionDrawable) currentDrawable).getDrawable(1);
        }
        Drawable[] arrayDrawable = new Drawable[2];
        arrayDrawable[0] = currentDrawable;
        arrayDrawable[1] = drawable;
        TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
        transitionDrawable.setCrossFadeEnabled(true);
        if (imageAware != null) {
            imageAware.setImageDrawable(transitionDrawable);
        } else {
            imageView.setImageDrawable(transitionDrawable);
        }
        transitionDrawable.startTransition(millis);
    }
}
