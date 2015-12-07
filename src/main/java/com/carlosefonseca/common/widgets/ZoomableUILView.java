package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.UIL;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import android.support.annotation.Nullable;

import java.io.File;

public class ZoomableUILView extends ZoomableImageView {

    private static final String TAG = CodeUtils.getTag(ZoomableUILView.class);
    private Bitmap mBitmap;
    private Object mLastUrlOrFile;

    public ZoomableUILView(Context context) {
        super(context);
    }

    public ZoomableUILView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized void setImageUrl(@Nullable String url) {
        setImageBitmap(getBitmap(url));
    }

    @Nullable
    protected Bitmap getBitmap(@Nullable Object urlOrFile) {
        if (mLastUrlOrFile != null && CodeUtils.equals(urlOrFile, mLastUrlOrFile)) {
            return mBitmap;
        }

        if (urlOrFile == null) {
            return null;
        }

        mLastUrlOrFile = urlOrFile;
        String uri;
        if (urlOrFile instanceof String) {
            uri = UIL.getUri((String) urlOrFile);
        } else if (urlOrFile instanceof File) {
            uri = UIL.getUri((File) urlOrFile);
        } else {
            throw new RuntimeException("" + urlOrFile.getClass().getName() + " isn't valid.");
        }
        DisplayMetrics d = getResources().getDisplayMetrics();

        mBitmap = ImageLoader.getInstance().loadImageSync(uri, new ImageSize(d.widthPixels * 2, d.heightPixels * 2));
        if (mBitmap != null) return mBitmap;

        //noinspection SuspiciousNameCombination
        mBitmap = ImageLoader.getInstance().loadImageSync(uri, new ImageSize(d.heightPixels, d.heightPixels));
        if (mBitmap != null) return mBitmap;

        mBitmap = ImageLoader.getInstance().loadImageSync(uri, new ImageSize(d.widthPixels, d.heightPixels));
        return mBitmap;
    }

    public synchronized void setImageFile(File file) {
        setImageBitmap(getBitmap(file));
    }
}
