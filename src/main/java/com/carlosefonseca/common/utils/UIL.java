package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.String;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public final class UIL {

    private static final String TAG = CodeUtils.getTag(UIL.class);

    private static File sExternalFilesDir;
    private static HashSet<String> sAssets;

    private UIL() {}

    public static void initUIL(Context context) {
        // The name for images is the last part of the URL
        FileNameGenerator fileNameGenerator = new FileNameGenerator() {
            @Override
            public String generate(String imageUri) {
                return imageUri.substring(imageUri.lastIndexOf("/") + 1);
            }
        };

        // Do disk cache on the files dir
        sExternalFilesDir = context.getExternalFilesDir(null);
        UnlimitedDiscCache diskCache = new UnlimitedDiscCache(sExternalFilesDir, null, fileNameGenerator);

        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2)
                                                             .diskCache(diskCache)
                                                             .tasksProcessingOrder(QueueProcessingType.LIFO)
                                                             .writeDebugLogs()
                                                             .build();
        ImageLoader.getInstance().init(config);

        sAssets = ResourceUtils.getAssets(context);
    }

    public static DisplayImageOptions mOptionsForPhotos = new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                                                                    .cacheInMemory(true)
                                                                                    .cacheOnDisk(true)
                                                                                    .considerExifParams(true)
                                                                                    .imageScaleType(ImageScaleType.EXACTLY)
                                                                                    .bitmapConfig(Bitmap.Config.RGB_565)
                                                                                    .build();

    static DisplayImageOptions mOptionsForIcons = new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                                                                   .cacheInMemory(true)
                                                                                   .cacheOnDisk(true)
                                                                                   .considerExifParams(false)
                                                                                   .imageScaleType(ImageScaleType.EXACTLY)
                                                                                   .bitmapConfig(Bitmap.Config.ARGB_8888)
                                                                                   .build();

    static SimpleImageLoadingListener sAnimateFirstDisplayListener = new SimpleImageLoadingListener() {

        final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    };

    public static String getUri(String str) {
        final String uri;
        if (str.startsWith("http")) { // Full URL
            String lastSegmentOfURL = NetworkingUtils.getLastSegmentOfURL(str);
            if (sAssets.contains(lastSegmentOfURL)) {
                uri = "assets://" + lastSegmentOfURL;
            } else {
                uri = str;
            }

        } else {
            if (!str.startsWith("/")) { // only filename
                if (sAssets.contains(str)) {
                    uri = "assets://" + str;
                } else { // set full path to ext/files
                    uri = "file://" + sExternalFilesDir + "/" + str;
                }
            }
            // we have a full path
            else {
                uri = "file://" + str;
            }
        }
        return uri;
    }

    @Nullable
    public static String getUri(File file) {
        final String uri;
        if (file.exists()) {
            uri = "file://" + file.getAbsolutePath();
        } else {
            String name = file.getName();
            if (sAssets.contains(name)) {
                uri = "assets://" + name;
            } else {
                Log.w(TAG, "File " + file.getAbsolutePath() + " doesn't exist on ext or assets!");
                uri = null;
            }
        }
        return uri;
    }

    @Nullable
    public static Bitmap loadSync(String str) {return loadSync(str, 0, 0);}

    @Nullable
    public static Bitmap loadSync(String str, int widthPx, int heightPx) {
        if (str == null) return null;

        String uri = getUri(str);

        ImageSize targetImageSize = widthPx > 0 && heightPx > 0 ? new ImageSize(widthPx, heightPx) : null;

        return ImageLoader.getInstance().loadImageSync(uri, targetImageSize);
    }

    public static void display(String str, ImageView imageView) {
        ImageLoader.getInstance().displayImage(getUri(str), imageView);
    }
}
