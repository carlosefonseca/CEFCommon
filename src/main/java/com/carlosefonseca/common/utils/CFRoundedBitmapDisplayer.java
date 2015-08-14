package com.carlosefonseca.common.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class CFRoundedBitmapDisplayer extends RoundedBitmapDisplayer
        implements DrawableMaker<RoundedBitmapDisplayer.RoundedDrawable> {
    public CFRoundedBitmapDisplayer(int cornerRadiusPixels) {
        super(cornerRadiusPixels);
    }

    public CFRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        super(cornerRadiusPixels, marginPixels);
    }

    @NonNull
    @Override
    public RoundedDrawable getDrawable(@NonNull Bitmap bitmap) {
        return new RoundedDrawable(bitmap, cornerRadius, margin);
    }
}
