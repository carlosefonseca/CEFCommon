package com.carlosefonseca.common.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public interface DrawableMaker<T extends Drawable> {
    @NonNull
    T getDrawable(@NonNull Bitmap bitmap);
}
