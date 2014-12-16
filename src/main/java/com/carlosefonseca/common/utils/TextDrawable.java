/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carlosefonseca.common.utils;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;

public class TextDrawable extends Drawable {
    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_TEXTSIZE = 15;
    private Paint mPaint;
    private String mText;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    private TextPaint textPaint;

    public TextDrawable(Resources res, CharSequence text) {
        mText = text.toString();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setTextAlign(Align.CENTER);
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                                   DEFAULT_TEXTSIZE,
                                                   res.getDisplayMetrics());
        mPaint.setTextSize(textSize);
        mIntrinsicWidth = (int) (mPaint.measureText(mText, 0, mText.length()) + .5);
        mIntrinsicHeight = mPaint.getFontMetricsInt(null);
        textPaint = new TextPaint(mPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        Rect textBounds = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), textBounds);
        canvas.drawText(mText, bounds.centerX(), bounds.centerY() - textBounds.centerY(), mPaint);
    }

    @Override
    public int getOpacity() {
        return mPaint.getAlpha();
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mPaint.setColorFilter(filter);
    }

    public Paint getPaint() {
        return mPaint;
    }
}