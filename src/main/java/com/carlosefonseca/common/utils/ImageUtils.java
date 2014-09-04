package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.NetworkingUtils.getLastSegmentOfURL;

/**
 * Util methods for manipulating images.
 */
@SuppressWarnings("UnusedDeclaration")
public final class ImageUtils {

    private static final String TAG = getTag(ImageUtils.class);

    private static float density = 1f;
    private static DisplayMetrics displayMetrics;
    private static int screenLayout;
    private static File cacheDir;

    static {
        Context c = CFApp.getContext();
        try {
            cacheDir = c.getCacheDir();
            displayMetrics = c.getResources().getDisplayMetrics();
            density = c.getResources().getDisplayMetrics().density;
            screenLayout = c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    private static HashSet<String> imagesOnAssets;

    private ImageUtils() {}

    /**
     * Given a {@code BitmapFactory.Options} of an image and the desired size for that image, calculates the adequate
     * InSampleSize value.
     */
    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight && width > reqWidth) {
//            if (width > height) {
            inSampleSize = Math.min(Math.round((float) height / (float) reqHeight), Math.round((float) width / (float) reqWidth));
//            } else {
//                inSampleSize = Math.round((float) width / (float) reqWidth);
//            }
        }
        return inSampleSize;
    }

/*
    public static void setDensity(Activity activity) {
        Log.i(TAG, "Width dp: " + ((1.0 * displayMetrics.widthDP) / density) + " density: " + density);
    }
*/

    public static float getDensity() {
        return density;
    }

    public static int dp2px(int dp) {
        return Math.round(dp * density);
    }

    public static int dp2px(double dp) {
        return (int) Math.round(dp * density);
    }

    public static DisplayMetrics getDisplayMetrics() {
        if (displayMetrics == null) {
            displayMetrics = CFApp.getContext().getResources().getDisplayMetrics();
//            throw new IllegalStateException("setDensity not yet invoked. Display Metrics aren't yet available.");
        }
        return displayMetrics;
    }


    public static Bitmap decodeSampledBitmapFromFileOnAssets(Context c, String path, int reqWidth, int reqHeight)
            throws IOException {
        InputStream stream = c.getAssets().open(path);

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = null;

        if (reqWidth > 0 && reqHeight > 0) {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            BitmapFactory.decodeStream(stream, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            stream.reset();
            //	    stream.close();
            //	    stream = SharedObjects.getContext().getAssets().open(path);
        }

        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        return bitmap;
    }

    @Nullable
    private static Bitmap getPhotoFromAssets(String path) {
        try {
            InputStream stream = CFApp.getContext().getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
            return null;
        }
    }


    @Nullable
    public static Bitmap decodeSampledBitmapFromFile(File path, int reqWidth, int reqHeight) {
        if (path == null) return null;
        return decodeSampledBitmapFromFile(path.getAbsolutePath(), reqWidth, reqHeight);
    }


    /**
     * Tries to get an image from the cache folder. If not found, tries to get the original image, scale it and save it to the
     * cache asynchronously.
     *
     * @param file     The path to the image file.
     * @param widthDp  Minimum widthDp in DP's.
     * @param heightDp Minimum heightDp in DP's.
     * @param sizeName An optional name to use for size of the the cached image.
     * @return Scaled and rotated image.
     * @see #getPhotoFromFile
     */
    @Nullable
    public static Bitmap getCachedPhoto(@Nullable File file, int widthDp, int heightDp, @Nullable String sizeName) {
        Bitmap bitmap = null;
        if (file == null) return null;
        String name = file.getName();
        String cacheName;
        File cacheFile = null;

        if (widthDp > 0 && heightDp > 0) {
            if (sizeName == null) {
                cacheName = name.substring(0, name.length() - 4) + "-" + widthDp + "x" + heightDp + ".png";
            } else {
                cacheName = name.substring(0, name.length() - 4) + sizeName + ".png";
            }
            cacheFile = new File(cacheDir, cacheName);
            if (cacheFile.exists()) {
                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            }
        }

        if (bitmap == null) {
            bitmap = getPhotoFromFileOrAssets(file, widthDp, heightDp);
            if (bitmap == null) return null;
            if (cacheFile != null) new ImageWriter(cacheFile, bitmap).execute();
        }
        return bitmap;
    }

    @Nullable
    private static Bitmap getPhotoFromFileOrAssets(File file, int widthDp, int heightDp) {
        final Bitmap bitmap = tryPhotoFromFileOrAssets(file, widthDp, heightDp);
        if (bitmap == null) Log.w(TAG, "IMAGE DOES NOT EXIST " + file);
        return bitmap;
    }

    @Nullable
    static Bitmap tryPhotoFromFileOrAssets(File file, int widthDp, int heightDp) {
        Bitmap bitmap = null;
        if (file.exists()) {
            bitmap = getPhotoFromFile(file.getAbsolutePath(), widthDp, heightDp);
        } else if (getImagesOnAssets().contains(file.getName())) {
            bitmap = getPhotoFromAssets(file.getName(), widthDp, heightDp);
        }
        return bitmap;
    }

    /**
     * Gets the aspect ratio (h/w) of an image be it on the full path or on the assets folder.
     * @return The ratio (h/w) or 0 if there's a problem with the image.
     */
    public static double getAspectRatio(File file) {
        final BitmapFactory.Options imageBounds = getImageBounds(file);
        if (imageBounds == null) return 0;
        return 1.0 * imageBounds.outHeight / imageBounds.outWidth;
    }

    @Nullable
    static BitmapFactory.Options getImageBounds(File file) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        options.inInputShareable = true;
        if (file.exists()) {
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } else if (getImagesOnAssets().contains(file.getName())) {
            try {
                InputStream stream = CFApp.getContext().getAssets().open(file.getName());
                BitmapFactory.decodeStream(stream, null, options);
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage(), e);
                return null;
            }
        } else {
            return null;
        }
        return options;
    }


//            return 1.0 * options.outHeight / options.outWidth;

    @Nullable
    public static Bitmap getPhotoFromFileOrAssets(File file) {
        return getPhotoFromFileOrAssets(file, -1, -1);
    }

    public static class BitmapCanvas {
        public final Bitmap bitmap;
        public final Canvas canvas;

        public BitmapCanvas(Bitmap bitmap, Canvas canvas) {
            this.bitmap = bitmap;
            this.canvas = canvas;
        }
    }

    public static ImageUtils.BitmapCanvas canvasFromBitmap(Bitmap bitmap) {
        final Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final BitmapCanvas bitmapCanvas = new BitmapCanvas(bitmap1, new Canvas(bitmap1));
        bitmapCanvas.canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG));
        return bitmapCanvas;
    }

    public static interface RunnableWithBitmap {
        public void run(Bitmap bmp);
    }

    public static void getCachedPhotoAsync(final File file,
                                           final int widthDp,
                                           final int heightDp,
                                           final RunnableWithBitmap runnable) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Nullable
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getCachedPhoto(file, widthDp, heightDp, null);
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                runnable.run(bmp);
            }
        }.execute();
    }

    @Nullable
    public static AsyncTask<Void, Void, Bitmap> getCachedPhotoAsync(final String filenameOrUrl,
                                                                    final int widthDp,
                                                                    final int heightDp,
                                                                    final RunnableWithBitmap runnable) {
        if (filenameOrUrl.startsWith("http://")) {
            return new AsyncTask<Void, Void, Bitmap>() {
                @Nullable
                @Override
                protected Bitmap doInBackground(Void... params) {
                    File fullPath = ResourceUtils.getFullPath(getLastSegmentOfURL(filenameOrUrl));
                    Bitmap cachedPhoto = getCachedPhoto(fullPath, widthDp, heightDp, null);
                    if (cachedPhoto != null) return cachedPhoto;
                    try {
                        Bitmap bitmap = NetworkingUtils.loadBitmap(filenameOrUrl);
                        new ImageWriter(fullPath, bitmap);
                        return bitmap;
                    } catch (IOException e) {
                        Log.e(TAG, "" + e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bmp) {
                    runnable.run(bmp);
                }
            }.execute();
        } else {
            File file = filenameOrUrl.startsWith("/") ? new File(filenameOrUrl) : ResourceUtils.getFullPath(filenameOrUrl);
            getCachedPhotoAsync(file, widthDp, heightDp, runnable);
        }
        return null;
    }

    @Nullable
    public static Bitmap getPhotoFromFileOrAssets(String filenameOrUrl) {
        if (filenameOrUrl == null) return null;
        return getPhotoFromFileOrAssets(convertSomeFileReferenceToFile(filenameOrUrl), -1, -1);
    }


    /**
     * Convenience method to convert filenames or URLs to an existing file on disk that will then be loaded with {@link
     * #getCachedPhoto(java.io.File, int, int, String)}.
     * <p/>
     * Accepted {@code filenameOrUrl} options:
     * <ul>
     * <li>http://example.com/image.png</li>
     * <li>/sdcard/somefolder/image.png</li>
     * <li>image.png</li>
     * </ul>
     * <p/>
     * This method doesn't resize
     *
     * @param filenameOrUrl File "reference".
     */
    @Nullable
    public static Bitmap getCachedPhoto(String filenameOrUrl) {
        return getCachedPhoto(filenameOrUrl, -1, -1);
    }


    /**
     * Convenience method to convert filenames or URLs to an existing file on disk that will then be loaded with {@link
     * #getCachedPhoto(java.io.File, int, int, String)}.
     * <p/>
     * Accepted {@code filenameOrUrl} options:
     * <ul>
     * <li>http://example.com/image.png</li>
     * <li>/sdcard/somefolder/image.png</li>
     * <li>image.png</li>
     * </ul>
     *
     * @param filenameOrUrl File "reference".
     * @param widthDp       Desired width.
     * @param heightDp      Desired height.
     */
    @Nullable
    public static Bitmap getCachedPhoto(final String filenameOrUrl, final int widthDp, final int heightDp) {
        if (filenameOrUrl == null) return null;
        return getCachedPhoto(convertSomeFileReferenceToFile(filenameOrUrl), widthDp, heightDp, null);
    }

    /**
     * Convenience method to convert filenames or URLs to an existing file on disk that will then be loaded with {@link
     * #getResizedIcon(java.io.File, int, int)}.
     * <p/>
     * Accepted {@code filenameOrUrl} options:
     * <ul>
     * <li>http://example.com/image.png</li>
     * <li>/sdcard/somefolder/image.png</li>
     * <li>image.png</li>
     * </ul>
     *
     * @param filenameOrUrl File "reference".
     * @param widthDp       Desired width.
     * @param heightDp      Desired height.
     */

    @Nullable
    public static Bitmap getResizedIcon(final String filenameOrUrl, final int widthDp, final int heightDp) {
        if (filenameOrUrl == null) return null;
        return getResizedIcon(convertSomeFileReferenceToFile(filenameOrUrl), widthDp, heightDp);
    }

    /**
     * Converts filenames or URLs to an existing file on disk.
     * <p/>
     * Accepted {@code filenameOrUrl} options:
     * <ul>
     * <li>http://example.com/image.png</li>
     * <li>/sdcard/somefolder/image.png</li>
     * <li>image.png</li>
     * </ul>
     *
     * @param filenameOrUrl File object pointing to
     */
    public static File convertSomeFileReferenceToFile(@NotNull String filenameOrUrl) {
        File file;
        if (filenameOrUrl.startsWith("http://")) {
            file = ResourceUtils.getFullPath(getLastSegmentOfURL(filenameOrUrl));
        } else {
            file = filenameOrUrl.startsWith("/") ? new File(filenameOrUrl) : ResourceUtils.getFullPath(filenameOrUrl);
        }
        return file;
    }

    @Nullable
    public static Bitmap getPhotoFromAssets(String name, int width, int height) {
        try {
            return decodeSampledBitmapFromFileOnAssets(CFApp.getContext(), name, width, height);
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tries to get an image from the cache folder. If not found, tries to get the original image, scale it and save it to the
     * cache asynchronously.
     *
     * @param file     The path to the image file.
     * @param widthDp  Minimum width in DP's.
     * @param heightDp Minimum height in DP's.
     * @return Scaled image.
     * @see #getPhotoFromFile
     */
    @Nullable
    public static Bitmap getResizedIcon(@Nullable File file, final int widthDp, final int heightDp) {
        Bitmap bitmap = null;
        if (file == null) return null;
        String name = file.getName();
        String cacheName;

        cacheName = name.substring(0, name.length() - 4) + "-" + widthDp + "x" + heightDp + ".png";

        File cacheFile = new File(cacheDir, cacheName);
        if (cacheFile.exists()) {
            bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
        }

        if (bitmap == null) {   // cache doesn't exist

            Bitmap bitmap1 = null;
            if (file.exists()) {
                // Load full size from sd
                bitmap1 = BitmapFactory.decodeFile(file.getAbsolutePath());
            } else if (getImagesOnAssets().contains(name)) {
                // Load full size from assets
                bitmap1 = getPhotoFromAssets(name);
            }

            if (bitmap1 != null) {

                if (widthDp != -1 || heightDp != -1) {
                    // desired size
                    int widthPx;
                    int heightPx;
                    if (bitmap1.getWidth() >= bitmap1.getHeight()) {
                        widthPx = (int) (widthDp * density);
                        heightPx = /*heightDp != -1 ? (int) (heightDp * density) :*/ widthPx * bitmap1.getHeight() /
                                                                                     bitmap1.getWidth();
                    } else {
                        heightPx = (int) (heightDp * density);
                        widthPx = /*widthDp != -1 ? (int) (widthDp * density) : */heightPx * bitmap1.getWidth() /
                                                                                  bitmap1.getHeight();
                    }

                    if (widthPx == bitmap1.getWidth() && heightPx == bitmap1.getHeight()) {
                        return bitmap1;
                    }

                    if (widthPx == 0 && heightPx == 0) {
                        throw new IllegalArgumentException(String.format(
                                "width(%d) and height(%d) must be > 0. Provided values: widthDp:%d heightDp:%d",
                                widthPx,
                                heightPx,
                                widthDp,
                                heightDp));
                    }

                    // bitmap size != desired size
                    bitmap = scaleAndCrop(bitmap1, widthPx, heightPx);
                    bitmap1.recycle();

                    new ImageWriter(cacheFile, bitmap).execute();
                } else {
                    bitmap = bitmap1;
                }
            } else {
                Log.w(TAG, "IMAGE DOES NOT EXIST " + file);
            }
        }
        return bitmap;
    }

    public static Bitmap scaleAndCrop(Bitmap bitmap1, int widthPx, int heightPx) {
        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(/*Paint.FILTER_BITMAP_FLAG*/);
        paint.setAntiAlias(true);

        canvas.drawBitmap(bitmap1, null, new Rect(0, 0, widthPx, heightPx), paint);
        return bitmap;
    }

    /**
     * Configuration.SCREENLAYOUT_SIZE_LARGE, Configuration.SCREENLAYOUT_SIZE_NORMAL...
     */
    public static int getScreenLayout() {
        return screenLayout;
    }

    public static HashSet<String> getImagesOnAssets() {
        if (imagesOnAssets == null) {
            try {
                imagesOnAssets = new HashSet<String>(Arrays.asList(CFApp.getContext().getAssets().list("")));
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
        return imagesOnAssets;
    }

    /**
     * Saves an image to the cache, asynchronously.
     */
    static class ImageWriter extends AsyncTask<Void, Void, Void> {
        private final File file;
        private final Bitmap bitmap;

        public ImageWriter(File file, Bitmap bitmap) {
            this.file = file;
            this.bitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
            } catch (Exception e) {
                Log.e(TAG, "Bitmap file:" + file, e);
            }
            return null;
        }
    }


    /**
     * Obtains an image, scaled down to be at least the requested size and rotated according to the EXIF on the file.<br/>
     * It's aware of the device density.
     *
     * @param path The path to the image file.
     * @return Scaled and rotated image or null if no image was found.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Nullable
    public static Bitmap getPhotoFromFile(String path) {
        try {
            int orientation = getCameraPhotoOrientation(path);
            Bitmap bitmap = BitmapFactory.decodeFile(path, null);
            return rotate(bitmap, orientation);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Obtains an image, scaled down to be at least the requested size and rotated according to the EXIF on the file.<br/>
     * It's aware of the device density.
     *
     * @param path   The path to the image file.
     * @param width  Desired width in DP's.
     * @param height Desired height in DP's.
     * @return Scaled and rotated image or null if no image was found.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Nullable
    public static Bitmap getPhotoFromFile(String path, int width, int height) {
        int orientation = getCameraPhotoOrientation(path);
        Bitmap bitmap;
        if (width > 0 && height > 0) {
            if (displayMetrics == null) {
                Log.w(TAG, "Device density not accurate. Please call setDensity() from an activity before this.");
            }
            if (orientation == 90 || orientation == 270) {
                int x = width;
                width = height;
                height = x;
            }

            width *= density;
            height *= density;

            bitmap = decodeSampledBitmapFromFile(path, width * 2, height * 2);
        } else {
            bitmap = BitmapFactory.decodeFile(path);
        }
        /*

        if (bitmap == null) {
            Log.e(TAG, "Image "+path+" not found.");
            return null;
        }

        float originalImgRatio = (float) (1.0 * bitmap.getWidth() / bitmap.getHeight());
        float desiredSizeRatio = (float) (1.0 * width / height);

        int finalWidth;
        int finalHeight;

        if (originalImgRatio > desiredSizeRatio) {
            finalHeight = height;
            finalWidth = (int) (height * originalImgRatio);
        } else {
            finalWidth = width;
            finalHeight = (int) (finalWidth / originalImgRatio);
        }

        Log.i(TAG, "getPhoto " + path + " " + width + "x" + height + " -> " + finalWidth + "x" + finalHeight+" orientation: "+orientation);
        bitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
        */
        bitmap = rotate(bitmap, orientation);

        return bitmap;
    }


    /**
     * Obtains a scaled down image from the file system.
     * The scaling uses the size parameters to calculate the inSampleSize and, therefore, it will have at least that size.
     *
     * @param path      The path to the image file.
     * @param reqWidth  Minimum width.
     * @param reqHeight Minimum height.
     * @return Scaled image.
     */
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        options.inInputShareable = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * Creates a squared thumbnail image from a source image and saves it to disk.
     * Subsequent requests to the same image with the same size will return the cached image.
     * The thumbnail will be a center-cropped version of the original, scaled to the specified size and rotated according to the
     * EXIF.
     *
     * @param c    A context (not the application one)
     * @param path The path to the image file.
     * @param side The side of the final image.
     * @return Image
     * @see #getSquareThumbnail(android.content.Context, String, int) Non-cached version.
     */
    @Nullable
    @Deprecated
    public static Bitmap getCachedSquareThumbnail(Context c, String path, int side) {
        Bitmap bitmap;
        String name = new File(path).getName();
        File cacheFile = new File(cacheDir, name.substring(0, name.length() - 4) + side + ".png");
        if (!cacheFile.exists()) {
            try {
                bitmap = getSquareThumbnail(c, path, side);
                new ImageWriter(cacheFile, bitmap).execute();
            } catch (Exception e) {
                Log.e(TAG, "Error generating thumbnail", e);
                return null;
            }
        } else {
            Log.v(TAG, "Loading from cache " + path);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inInputShareable = true;
            bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
        }
        return bitmap;
    }

    /**
     * Creates a thumbnail image from a source image.
     * The thumbnail will be a center-cropped version of the original, scaled to the specified size and rotated according to the
     * EXIF.
     *
     * @param c    A context (not the application one)
     * @param path The path to the image file.
     * @param side The side of the final image.
     * @see #getCachedSquareThumbnail(android.content.Context, String, int) Disk-cached version.
     */
    private static Bitmap getSquareThumbnail(Context c, String path, int side) {
        Bitmap bitmap = decodeSampledBitmapFromFile(path, side, side);
        bitmap = cropSquare(bitmap);
        bitmap = Bitmap.createScaledBitmap(bitmap, side, side, true);
        bitmap = rotate(bitmap, path);
        return bitmap;
    }

    /**
     * Creates a square, cropped image from the center of the source image.
     *
     * @param bitmap Source bitmap.
     * @return New image.
     */
    public static Bitmap cropSquare(Bitmap bitmap) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int x, y, side;
        if (originalWidth > originalHeight) {
            x = (originalWidth - originalHeight) / 2;
            y = 0;
            side = originalHeight;
        } else {
            x = 0;
            y = (originalHeight - originalWidth) / 2;
            side = originalWidth;
        }
        Log.v("getThumbnail", "x:" + x + " y:" + y + " side:" + side);
        bitmap = Bitmap.createBitmap(bitmap, x, y, side, side);
        return bitmap;
    }


    @Deprecated
    public static Bitmap getThumbnail(Bitmap bitmap, int side) {
        int iw = bitmap.getWidth();
        int ih = bitmap.getHeight();
        int x, y, w, h;
        if (iw > ih) {
            x = (iw - ih) / 2;
            y = 0;
            w = ih;
            h = ih;
        } else {
            x = 0;
            y = (ih - iw) / 2;
            h = iw;
            w = iw;
        }
        bitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
        bitmap = Bitmap.createScaledBitmap(bitmap, side, side, true);
        Log.i("getThumbnail",
              "x:" + x + " y:" + y + " w:" + w + " h:" + h + " | scaled: " + bitmap.getWidth() + " x " + bitmap.getHeight());
        return bitmap;
    }

    /**
     * Calculates the orientation of an image on disk based on the EXIF information.
     *
     * @param imagePath The path to the image.
     * @return Returns 0, 90, 180 or 270.
     */
    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }


//            Log.v(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            Log.e(TAG, "Error getCameraPhotoOrientation", e);
        }
        return rotate;
    }

    /**
     * Creates a rotated copy of an image according to the EXIF information.
     *
     * @param bitmap The source image.
     * @param path   The path on disk.
     * @return A rotated bitmap.
     */
    public static Bitmap rotate(Bitmap bitmap, String path) {
        int orientation = getCameraPhotoOrientation(path);
        bitmap = rotate(bitmap, orientation);

        return bitmap;
    }

    /**
     * Rotates an image by a given number of degrees.
     *
     * @param bitmap      The source bitmap.
     * @param orientation The amount of degrees to rotate.
     * @return Rotated bitmap.
     */
    public static Bitmap rotate(Bitmap bitmap, int orientation) {
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            // create a new bitmap from the original using the matrix to transform the result
            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bitmap1;
        }
        return bitmap;
    }

    /**
     * Creates a new bitmap from the original resource and paints the visible parts with the given color.
     * The alpha channel is the only part of the original resource that is used for the painting.
     * <p/>
     * Note: I tried saving a painted file on disk and loading that one each time. Got 1ms improvement but more used memory for
     * repeated calls.
     *
     * @param c     A context.
     * @param resId The original image with alpha channel.
     * @param color The new Color
     * @return Bitmap with new color.
     */
    public static Bitmap createRecoloredBitmap(Context c, int resId, int color) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap source = BitmapFactory.decodeResource(c.getResources(), resId);
        return createRecoloredBitmap(source, color);
    }

    /**
     * Wraps {@link #createRecoloredBitmap(android.content.Context, int, int)} in a {@link
     * android.graphics.drawable.BitmapDrawable}.
     *
     * @param context A context.
     * @param resId   The original image with alpha channel.
     * @param color   The new Color
     * @return Bitmap with new color.
     */
    public static BitmapDrawable createRecoloredDrawable(Context context, int resId, int color) {
        return new BitmapDrawable(context.getResources(), createRecoloredBitmap(context, resId, color));
    }

    /**
     * Wraps {@link #createRecoloredBitmap(android.graphics.Bitmap, int)} in a {@link
     * android.graphics.drawable.BitmapDrawable}.
     *
     * @param bitmap The original image with alpha channel.
     * @param color  The new Color
     * @return Bitmap with new color.
     */
    public static BitmapDrawable createRecoloredDrawable(Context context, Bitmap bitmap, int color) {
        return new BitmapDrawable(context.getResources(), createRecoloredBitmap(bitmap, color));
    }


    /**
     * Creates a new bitmap from the original resource and paints the visible parts with the given color.
     * The alpha channel is the only part of the original resource that is used for the painting.
     *
     * @param source A context.
     * @param color  The original image with alpha channel.
     * @return Bitmap with new color.
     */
    public static Bitmap createRecoloredBitmap(Bitmap source, int color) {
        Bitmap mask = source.extractAlpha();

        Bitmap targetBitmap = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawBitmap(mask, 0, 0, paint);

        return targetBitmap;
    }

    /**
     * Creates a new bitmap from the original resource and paints the visible parts with the given color.
     * The alpha channel is the only part of the original resource that is used for the painting.
     * <p/>
     * Note: I tried saving a painted file on disk and loading that one each time. Got 1ms improvement but more used memory for
     * repeated calls.
     *
     * @param c     A context.
     * @param resId The original image with alpha channel.
     * @param color The new Color
     * @return Bitmap with new color.
     */
    public static Bitmap createRecoloredBitmapMultiply(Context c, int resId, int color) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap source = BitmapFactory.decodeResource(c.getResources(), resId);
        return createRecoloredBitmapMultiply(source, color);
    }


    /**
     * Creates a new bitmap from the original resource and paints the visible parts with the given color.
     * The alpha channel is the only part of the original resource that is used for the painting.
     *
     * @param source A context.
     * @param color  The original image with alpha channel.
     * @return Bitmap with new color.
     */
    public static Bitmap createRecoloredBitmapMultiply(Bitmap source, int color) {
        final Bitmap blend = createRecoloredBitmap(source, color);

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        p.setShader(new BitmapShader(blend, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Canvas c = new Canvas();
        final Bitmap targetBitmap = source.copy(Bitmap.Config.ARGB_8888, true);
        c.setBitmap(targetBitmap);
        c.drawBitmap(source, 0, 0, null);
        c.drawRect(0, 0, source.getWidth(), source.getHeight(), p);

        return targetBitmap;
    }

    /**
     * Draws text on the canvas. Tries to make the text as big as possible so it fills the canvas but not more.
     * <p/>
     * This method starts on size 1 and goes up. Maybe a binary search or something could be better, but it works for simple
     * stuff.
     *
     * @param canvas    The canvas to draw the text on.
     * @param text      The text.
     * @param textColor The color for the text.
     * @param typeface  The typeface for the text.
     */
    public static void placeTextFillingCanvas(Canvas canvas, String text, int textColor, Typeface typeface) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int text_size = 1;

        TextPaint paint = new TextPaint();
        paint.setColor(textColor);
        paint.setTypeface(null);
        paint.setSubpixelText(true);
        paint.setAntiAlias(true);
        paint.setTypeface(typeface);

        StaticLayout sl1 = null;
        StaticLayout sl2;

        while (true) {
            paint.setTextSize(text_size);
            sl2 = new StaticLayout(text, paint, canvasWidth, Layout.Alignment.ALIGN_CENTER, 1, 0, false);
            text_size += 1;
            if (sl1 == null || sl2.getHeight() <= canvasHeight && sl2.getWidth() <= canvasWidth) {
                sl1 = sl2;
            } else {
                break;
            }
        }

        canvas.save();
        canvas.translate(0, (float) (1.0 * (canvasHeight - sl1.getHeight()) / 2));
        sl1.draw(canvas);
        canvas.restore();
    }

    /**
     * Creates a bitmap with a circle of a certain size and color.
     *
     * @param iconSizePx The size of the bitmap/diameter of the circle, in pixels.
     * @param color      The color of the bitmap.
     */
    public static Bitmap getCircleBitmap(int iconSizePx, int color) {
        Bitmap bitmap = Bitmap.createBitmap(iconSizePx, iconSizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawCircle(canvas, color, iconSizePx);
        return bitmap;
    }

    /**
     * Wraps {@link #getCircleBitmap(int, int)} in a {@link android.graphics.drawable.BitmapDrawable}.
     *
     * @param res        A resources.
     * @param iconSizeDp The size of the bitmap/diameter of the circle, in DP.
     * @param color      The color of the bitmap.
     */
    public static BitmapDrawable getCircleBitmapDrawable(Resources res, int iconSizeDp, int color) {
        return new BitmapDrawable(res, getCircleBitmap(dp2px(iconSizeDp), color));
    }

    /**
     * Draws a circle of a certain size and color in the top left of the given canvas.
     *
     * @param canvas     The canvas to draw the circle on.
     * @param color      The color for the circle.
     * @param iconSizePx The diameter of the circle, in pixels.
     */
    public static void drawCircle(Canvas canvas, int color, int iconSizePx) {// DRAW COLORED CIRCLE
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        canvas.drawCircle(iconSizePx / 2f, iconSizePx / 2f, iconSizePx / 2f, p);
    }

    /**
     * Draws a bitmap on top of another, in the middle. Modifies the bottom bitmap.
     *
     * @param bottom The bitmap that will be bellow.
     * @param top    The bitmap that will be on top.
     */
    public static Bitmap drawOnMiddle(Bitmap bottom, Bitmap top) {
        if (bottom == null) throw new InvalidParameterException("Bottom is null!");
        if (top == null) throw new InvalidParameterException("Top is null!");

        if (!bottom.isMutable()) {
            bottom = bottom.copy(Bitmap.Config.ARGB_8888, true);
            assert bottom != null;
        }
        final Canvas canvas = new Canvas(bottom);
        canvas.drawBitmap(top, (bottom.getWidth() - top.getWidth()) / 2f, (bottom.getHeight() - top.getHeight()) / 2f, null);
        return bottom;
    }


    /**
     * Asynchronously obtains a bitmap from disk and places it on an ImageView with a quick fade-in effect.
     * <p/>
     * You must use an {@link android.support.v4.util.LruCache} that implements {@link android.support.v4.util.LruCache#create(Object)}
     * so it will obtain
     * the bitmap.
     *
     * @param file        A reference to the file that will be passed to the LruCache.
     * @param view        The view that will receive the bitmap.
     * @param cache       The LruCache implementation (see {@link com.carlosefonseca.common.utils.ImageUtils.SizedImageCache} for
     *                    an
     *                    example).
     * @param placeholder A placeholder that will be set in case there is no bitmap to place.
     */
    @Nullable
    public static AsyncTask<Void, Void, Bitmap> setImageAsyncFadeIn(final String file,
                                                                    final ImageView view,
                                                                    final LruCache<String, Bitmap> cache,
                                                                    final int placeholder) {
        return setImageAsyncFadeIn(file, view, cache, placeholder, false);
    }

    /**
     * Asynchronously obtains a bitmap from disk and places it on an ImageView with a quick fade-in effect.
     * <p/>
     * You must use an {@link LruCache} that implements {@link android.support.v4.util.LruCache#create(Object)} so it will obtain
     * the bitmap.
     *
     * @param file         A reference to the file that will be passed to the LruCache.
     * @param view         The view that will receive the bitmap.
     * @param cache        The LruCache implementation (see {@link com.carlosefonseca.common.utils.ImageUtils.SizedImageCache}
     *                     for
     *                     an
     *                     example).
     * @param placeholder  A placeholder that will be set in case there is no bitmap to place.
     */
    @Nullable
    public static AsyncTask<Void, Void, Bitmap> setImageAsyncFadeIn(final String file,
                                                                    final View view,
                                                                    final LruCache<String, Bitmap> cache,
                                                                    final int placeholder,
                                                                    final boolean toBackground) {
        if (file != null && file.equals(view.getTag())) {
            return null;
        }
        view.setVisibility(View.INVISIBLE);
        return new AsyncTask<Void, Void, Bitmap>() {
            @Nullable
            @Override
            protected Bitmap doInBackground(Void... params) {
                return file != null ? cache.get(file) : null;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp != null) {
                    if (toBackground) {
                        ResourceUtils.setBackground(view, new BitmapDrawable(view.getResources(), bmp));
                    } else {
                        ((ImageView) view).setImageBitmap(bmp);
                    }
                    view.setTag(file);
                } else {
                    if (toBackground) {
                        view.setBackgroundResource(placeholder);
                    } else {
                        ((ImageView) view).setImageResource(placeholder);
                    }
                }

                if (SDK_INT < ICE_CREAM_SANDWICH) {
                    view.setVisibility(View.VISIBLE);
                    return;
                }

                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(100);
                Animation.AnimationListener listener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                };
                alphaAnimation.setAnimationListener(listener);
                view.startAnimation(alphaAnimation);
            }
        }.execute();
    }


    /**
     * Image cache that loads resized photos from disk. Specify the desired size of the images in the constructor and perform
     * {@link #get(Object)} with a filename or a
     */
    public static class SizedImageCache extends LruCache<String, Bitmap> {
        protected final int width;
        protected final int height;

        public SizedImageCache(int maxSize, int widthDP, int heightDP) {
            super(maxSize);
            this.width = widthDP;
            this.height = heightDP;
        }

        public SizedImageCache(int maxSize, int imageSizeDP) {
            this(maxSize, imageSizeDP, imageSizeDP);
        }

        @Nullable
        @Override
        protected Bitmap create(String key) {
            return getCachedPhoto(key, width, height);
        }
    }

    /**
     * Note: In this example, one eighth of the application memory is allocated for our cache. On a normal/hdpi device this is a
     * minimum of around 4MB (32/8). A full screen GridView filled with images on a device with 800x480 resolution would use
     * around 1.5MB (800*480*4 bytes), so this would cache a minimum of around 2.5 pages of images in memory.
     */

    public static class BitmapCache extends LruCache<String, Bitmap> {

        public BitmapCache() {
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            // Use 1/6th of the available memory for this memory cache.
            super((int) (Runtime.getRuntime().maxMemory() / 1024 / 6));
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return bitmap.getAllocationByteCount();
            }

            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    }


    /**
     * Redraws the bitmap in a new size. Scales the bitmap keeping the aspect ratio.
     */
    public static Bitmap resizeBitmapCanvas(Bitmap bitmap, int desiredWidth, int desiredHeight) {
        if (bitmap.getWidth() == desiredWidth && bitmap.getHeight() == desiredHeight) return bitmap;

        float r = 1f * bitmap.getWidth() / bitmap.getHeight();

        int newBitmapWidth;
        int newBitmapHeight = (int) (desiredWidth * 1f / r);

        if (newBitmapHeight > desiredHeight) {
            newBitmapHeight = desiredHeight;
            newBitmapWidth = (int) (desiredHeight * 1f * r);
        } else {
            newBitmapWidth = desiredWidth;
        }

        Bitmap newBitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(/*Paint.FILTER_BITMAP_FLAG*/);
        paint.setAntiAlias(true);


        int hMargin = (desiredWidth - newBitmapWidth) / 2;
        int vMargin = (desiredHeight - newBitmapHeight) / 2;

        Rect dst = new Rect(hMargin, vMargin, hMargin + newBitmapWidth, vMargin + bitmap.getHeight());

        canvas.drawBitmap(bitmap, null, dst, paint);
        return newBitmap;
    }

    static boolean isImage(String path) {
        return path != null && Pattern.compile(".+\\.(jpe?g|png|gif)", Pattern.CASE_INSENSITIVE).matcher(path).matches();
    }
}


