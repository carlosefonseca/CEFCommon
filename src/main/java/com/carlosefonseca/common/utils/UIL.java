package com.carlosefonseca.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.carlosefonseca.common.CFApp;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloaderImpl;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static com.carlosefonseca.common.utils.ImageUtils.dp2px;

public final class UIL {

    private static final String TAG = CodeUtils.getTag(UIL.class);

    private static File sExternalFilesDir;
    private static HashSet<String> sAssets;
    private static HashMap<File, ZipFile> sObb;
    private static ImageLoader sIL;
    @Nullable private static ZipResourceFile sApkExpansionZipFile;

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

        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context);
        if (CFApp.isTestDevice()) builder.writeDebugLogs();

        try {
            sApkExpansionZipFile = APKExpansionSupport.getAPKExpansionZipFile(context);
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
            sApkExpansionZipFile = null;
        }

        BaseImageDownloaderImpl sImageDownloader = null;
        if (sApkExpansionZipFile != null && sApkExpansionZipFile.getAllEntries().length > 0) {
            sImageDownloader = new BaseImageDownloaderImpl(context, sApkExpansionZipFile);
        }

        ImageLoaderConfiguration config = builder.threadPriority(Thread.NORM_PRIORITY - 2)
                                                 .memoryCacheSize((int) (CodeUtils.getFreeMem() / 6))
                                                 .diskCache(diskCache)
                                                 .tasksProcessingOrder(QueueProcessingType.LIFO)
                                                 .imageDownloader(sImageDownloader)
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

    static Pattern pattern = Pattern.compile("(assets|file|obb)://.*", Pattern.CASE_INSENSITIVE);

    @Nullable
    public static String getUri(@Nullable String str) {
        if (str == null) return null;
        if (pattern.matcher(str).matches()) return str;

        final String uri;
        if (str.startsWith("http")) { // Full URL
            String lastSegmentOfURL = NetworkingUtils.getLastSegmentOfURL(str);
            if (sAssets.contains(lastSegmentOfURL)) {
                uri = ImageDownloader.Scheme.ASSETS.wrap(lastSegmentOfURL);
            } else if (sApkExpansionZipFile != null && sApkExpansionZipFile.contains(lastSegmentOfURL)) {
                uri = BaseImageDownloaderImpl.obbScheme + lastSegmentOfURL;
            } else {
                uri = str.replaceAll(" ", "%20");
            }

        } else {
            if (!str.startsWith("/")) { // only filename
                if (sAssets.contains(str)) {
                    uri = ImageDownloader.Scheme.ASSETS.wrap(str);
                } else if (sApkExpansionZipFile != null && sApkExpansionZipFile.contains(str)) {
                    uri = BaseImageDownloaderImpl.obbScheme + str;
                } else { // set full path to ext/files
                    final String path = sExternalFilesDir + "/" + str;
                    if (new File(path).exists()) {
                        uri = ImageDownloader.Scheme.FILE.wrap(path);
                    } else {
                        uri = null;
                        Log.w(TAG, "File " + str + " does not exist!");
                    }
                }
            }
            // we have a full path
            else {
                if (new File(str).exists()) {
                    uri = ImageDownloader.Scheme.FILE.wrap(str);
                } else {
                    if (sAssets.contains(str)) {
                        uri = ImageDownloader.Scheme.ASSETS.wrap(str);
                    } else if (sApkExpansionZipFile != null && sApkExpansionZipFile.contains(str)) {
                        uri = BaseImageDownloaderImpl.obbScheme + str;
                    } else {
                        uri = null;
                        Log.w(TAG, "File " + str + " does not exist!");
                    }
                }
            }
        }
        return uri;
    }

    @Nullable
    public static String getUri(@Nullable File file) {
        if (file == null) return null;
        final String uri;
        if (file.exists()) {
            uri = ImageDownloader.Scheme.FILE.wrap(file.getAbsolutePath());
        } else {
            String name = file.getName();
            if (sAssets.contains(name)) {
                uri = ImageDownloader.Scheme.ASSETS.wrap(name);
            } else if (sApkExpansionZipFile != null && sApkExpansionZipFile.contains(name)) {
                uri = BaseImageDownloaderImpl.obbScheme + name;
            } else {
                Log.w(TAG, "File " + file.getAbsolutePath() + " doesn't exist on ext or assets!");
                uri = null;
            }
        }
        return uri;
    }

    public static boolean existsOnPackage(String file) {
        return sAssets.contains(file) || (sApkExpansionZipFile != null && sApkExpansionZipFile.contains(file));
    }

    @Nullable
    public static Bitmap loadSync(@Nullable String str) {return loadSync(str, 0, 0);}

    @Nullable
    public static Bitmap loadSync(@Nullable File file) {return loadSync(file, 0, 0);}

    @Nullable
    public static Bitmap loadSync(@Nullable String str, int widthPx, int heightPx) {
        if (str == null) return null;
        String uri = getUri(str);
        ImageSize targetImageSize = widthPx > 0 && heightPx > 0 ? new ImageSize(widthPx, heightPx) : null;
        return sIL.loadImageSync(uri, targetImageSize);
    }

    @Nullable
    public static Bitmap loadSync(@Nullable File file, int widthPx, int heightPx) {
        if (file == null) return null;
        String uri = getUri(file);
        ImageSize targetImageSize = widthPx > 0 && heightPx > 0 ? new ImageSize(widthPx, heightPx) : null;
        return sIL.loadImageSync(uri, targetImageSize);
    }

    @Nullable
    public static Bitmap loadSyncDP(@Nullable String str, int widthDp, int heightDp) {
        if (str == null) return null;
        String uri = getUri(str);
        ImageSize targetImageSize = widthDp > 0 && heightDp > 0 ? new ImageSize(dp2px(widthDp), dp2px(heightDp)) : null;
        return sIL.loadImageSync(uri, targetImageSize);
    }

    public static void load(@Nullable String str, @NonNull ImageLoadingListener loadingListener) {
        load(str, 0, 0, loadingListener);
    }

    public static void load(@Nullable String str, int widthPx, int heightPx, @NonNull ImageLoadingListener loadingListener) {
        if (str == null) return;
        String uri = getUri(str);
        ImageSize targetImageSize = widthPx > 0 && heightPx > 0 ? new ImageSize(widthPx, heightPx) : null;
        sIL.loadImage(uri, targetImageSize, loadingListener);
    }

    public static void display(@Nullable String str, @NonNull ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), null, null);
        }
    }

    public static void displayPhoto(@Nullable String str, @NonNull ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), mOptionsForPhotos, null);
        }
    }

    public static void displayIcon(@Nullable String str, @NonNull ImageView imageView) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), new ImageViewAware(imageView), mOptionsForIcons, null);
        }
    }

    public static void display(@Nullable String str, @Nullable ImageView imageView, ImageLoadingListener listener) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str), imageView != null ? new ImageViewAware(imageView) : null, null, listener);
        }
    }

    public static void display(@Nullable String str, @Nullable ImageView imageView, DisplayImageOptions displayImageOptions) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str),
                             imageView != null ? new ImageViewAware(imageView) : null,
                             displayImageOptions,
                             null);
        }
    }

    public static void display(@Nullable String str, @Nullable ImageView imageView, ImageLoadingListener listener, DisplayImageOptions displayImageOptions) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str),
                             imageView != null ? new ImageViewAware(imageView) : null,
                             displayImageOptions,
                             listener);
        }
    }

    static void display_(@Nullable String str, @Nullable ImageView imageView, ImageLoadingListener listener, DisplayImageOptions displayImageOptions) {
        sIL.displayImage(UIL.getUri(str),
                         imageView != null ? new ImageViewAware(imageView) : null,
                         displayImageOptions,
                         listener);
    }

    public static void displayPhoto(@Nullable String str, @Nullable ImageView imageView, ImageLoadingListener listener) {
        if (StringUtils.isNotBlank(str)) {
            sIL.displayImage(UIL.getUri(str),
                             imageView != null ? new ImageViewAware(imageView) : null,
                             mOptionsForPhotos,
                             listener);
        }
    }

    @Nullable
    public static Bitmap getIcon(@Nullable String str, int w, int h) {
        if (StringUtils.isBlank(str)) return null;
        return sIL.loadImageSync(getUri(str), new ImageSize(w, h), mOptionsForIcons);
    }

    @Nullable
    public static Bitmap getIconDP(@Nullable String str, int w, int h) {
        if (StringUtils.isBlank(str)) return null;
        return sIL.loadImageSync(getUri(str), new ImageSize(dp2px(w), dp2px(h)), mOptionsForIcons);
    }

    public static DisplayImageOptions getDisplayOptions(BitmapDisplayer displayer) {
        return new DisplayImageOptions.Builder().cloneFrom(mOptionsForPhotos).displayer(displayer).build();
    }
}
