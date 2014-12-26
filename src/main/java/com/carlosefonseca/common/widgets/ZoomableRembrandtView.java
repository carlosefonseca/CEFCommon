package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.Rembrandt;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ZoomableRembrandtView extends ZoomableImageView {

    private static final String TAG = CodeUtils.getTag(ZoomableRembrandtView.class);

    public ZoomableRembrandtView(Context context) {
        super(context);
    }

    public ZoomableRembrandtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized void setImageUrl(@Nullable String url) {
        try {
            setImageBitmap(Rembrandt.bitmapFromUrl(url, 0, 0));
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    public synchronized void setImageFile(File file) {
        setImageBitmap(Rembrandt.bitmapFromFile(file, 0, 0));
    }
}
