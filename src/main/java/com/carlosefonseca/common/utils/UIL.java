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
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.apache.commons.lang3.StringUtils;
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
    private static ImageLoader sIL;

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
                                                             .memoryCacheSize((int) (CodeUtils.getFreeMem()/6))
                                                             .diskCache(diskCache)
                                                             .tasksProcessingOrder(QueueProcessingType.LIFO)
                                                             .writeDebugLogs()
                                                             .build();
        sIL = ImageLoader.getInstance();
        sIL.init(config);

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

    public static SimpleImageLoadingListener getAnimateFirstDisplayListener() {
        return new SimpleImageLoadingListener() {
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
    }

    @Nullable
    public static String getUri(@Nullable String str) {
        if (str == null) return null;
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

        return sIL.loadImageSync(uri, targetImageSize);
    }

    public static void display(@Nullable String str, ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), null, null);
        }
    }

    public static void displayPhoto(@Nullable String str, ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), mOptionsForPhotos, null);
        }
    }

    public static void displayIcon(@Nullable String str, ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), mOptionsForIcons, null);
        }
    }

    public static void display(@Nullable String str, ImageView imageView, ImageLoadingListener listener) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), null, listener);
        }
    }

    public static void displayPhoto(@Nullable String str, ImageView imageView, ImageLoadingListener listener) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), mOptionsForPhotos, listener);
        }
    }

    @Nullable
    public static Bitmap getIcon(@Nullable String str, int w, int h) {
        if (StringUtils.isNotBlank(str)) {
            return sIL.loadImageSync(getUri(str), new ImageSize(w, h), mOptionsForIcons);
        }
        return null;
    }
}
