package com.nostra13.universalimageloader.core.download;

import android.content.Context;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.carlosefonseca.common.utils.CodeUtils;

import java.io.IOException;
import java.io.InputStream;

public class BaseImageDownloaderImpl extends BaseImageDownloader {

    private static final java.lang.String TAG = CodeUtils.getTag(BaseImageDownloaderImpl.class);

    private ZipResourceFile mApkExpansionZipFile;
    public static final String obbScheme = "obb://";

    public BaseImageDownloaderImpl(Context context, ZipResourceFile apkExpansionZipFile) {
        super(context);
        this.mApkExpansionZipFile = apkExpansionZipFile;
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(obbScheme)) {
            String fileName = imageUri.substring(obbScheme.length());
            return mApkExpansionZipFile.getInputStream(fileName);
        } else {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
    }

    public boolean contains(String filename) {
        return mApkExpansionZipFile.contains(filename);
    }
}
