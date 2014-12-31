package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.Rembrandt;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ZoomableRembrandtView extends ZoomableImageView {

    private static final String TAG = CodeUtils.getTag(ZoomableRembrandtView.class);
    private Bitmap bitmap;
    private Object mLastUrlOrFile;

    public ZoomableRembrandtView(Context context) {
        super(context);
    }

    public ZoomableRembrandtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized void setImageUrl(@Nullable String url) {
        setImageBitmap(getBitmap(url));
    }

    @Nullable
    protected Bitmap getBitmap(@Nullable Object urlOrFile) {
        if (mLastUrlOrFile != null && CodeUtils.equals(urlOrFile, mLastUrlOrFile)) {
            return bitmap;
        }

        if (urlOrFile == null) {
            return null;
        }

        mLastUrlOrFile = urlOrFile;
        if (urlOrFile instanceof String) {
            try {
                bitmap = Rembrandt.bitmapFromUrl((String) urlOrFile, 0, 0);
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage(), e);
                return null;
            }
        } else if (urlOrFile instanceof File) {
            bitmap = Rembrandt.bitmapFromFile((File) urlOrFile, 0, 0);
        }
        return bitmap;

    }

    public synchronized void setImageFile(File file) {
        setImageBitmap(getBitmap(file));
    }
}
