package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import bolts.Task;
import com.carlosefonseca.common.CFApp;
import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static com.carlosefonseca.common.utils.CodeUtils.getTag;

/**
 * Util methods for manipulating images.
 */
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
            //noinspection ConstantConditions
            Log.w(TAG, "" + e.getMessage(), (Object[])null);
        }
    }

    private static HashSet<String> imagesOnAssets;

    private ImageUtils() {}

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
        }
        return displayMetrics;
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            //noinspection deprecation
            options.inPurgeable = true;
            //noinspection deprecation
            options.inInputShareable = true;
        }
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


    public static int sizeBitmap(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

//    @Deprecated
//    @Nullable
//    public static Bitmap getCachedPhoto(String path) {
//        return UIL.loadSync(path);
//    }
//
//    @Nullable
//    @Deprecated
//    public static Bitmap getCachedPhoto(String image, int i, int i1) {
//        return UIL.loadSyncDP(image, i, i1);
//    }
//
//    @Deprecated
//    @Nullable
//    public static Bitmap getCachedPhoto(File file, int i, int i1) {
//        return UIL.loadSync(file, i, i1);
//    }
//
//    @Deprecated
//    @Nullable
//    public static Bitmap getCachedPhoto(File path) {
//        return UIL.loadSync(path);
//    }
//
//    @Nullable
//    @Deprecated
//    public static Bitmap getResizedIcon(String file, int w, int h) {
//        return UIL.getIconDP(file, w, h);
//    }
//
//    @Nullable
//    @Deprecated
//    public static Bitmap getResizedIcon(File file, int w, int h) {
//        return UIL.getIconDP(UIL.getUri(file), w, h);
//    }

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


    /**
     * Configuration.SCREENLAYOUT_SIZE_LARGE, Configuration.SCREENLAYOUT_SIZE_NORMAL...
     */
    public static int getScreenLayout() {
        return screenLayout;
    }

    public static HashSet<String> getImagesOnAssets() {
        if (imagesOnAssets == null) {
            try {
                imagesOnAssets = new HashSet<>(Arrays.asList(CFApp.getContext().getAssets().list("")));
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

    static void writeImageInBackground(final File file, final Bitmap bitmap) {
        Task.callInBackground(new Callable<Object>() {
            @Nullable
            @Override
            public Object call() throws Exception {
                try {
                    final String state = Environment.getExternalStorageState();
                    if (state.equals(Environment.MEDIA_MOUNTED)) {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
                    } else {
                        Log.i("Can't write " + file + " on ExtStorage. " + state);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "WriteImageInBackground Failed for " + file.getAbsolutePath(), e);
                }
                return null;
            }
        });
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
        try {
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
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    //noinspection deprecation
                    options.inPurgeable = true;
                    //noinspection deprecation
                    options.inInputShareable = true;
                }
                bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "" + e.getMessage(), e);
            return null;
        }
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
        Bitmap bitmap = UIL.loadSync(path, side, side);
        bitmap = cropSquare(bitmap);
        bitmap = Bitmap.createScaledBitmap(bitmap, side, side, true);
        return bitmap;
    }

    /**
     * Creates a square, cropped image from the center of the source image.
     *
     * @param bitmap Source bitmap.
     * @return New image.
     */
    public static Bitmap cropSquare(Bitmap bitmap) {
        Rect centerSquare = getCenterSquare(bitmap);
        return Bitmap.createBitmap(bitmap,
                                   centerSquare.left,
                                   centerSquare.top,
                                   centerSquare.width(),
                                   centerSquare.height());
    }


    public static Rect getCenterSquare(Bitmap bitmap) {
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
        return new Rect(x, y, x + side, y + side);
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
     * @deprecated Use @{@link #createRecoloredBitmap(android.content.res.Resources, int, int)}
     */
    @Deprecated
    public static Bitmap createRecoloredBitmap(Context c, int resId, int color) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap source = BitmapFactory.decodeResource(c.getResources(), resId);
        return createRecoloredBitmap(source, color);
    }

    /**
     * Creates a new bitmap from the original resource and paints the visible parts with the given color.
     * The alpha channel is the only part of the original resource that is used for the painting.
     * <p/>
     * Note: I tried saving a painted file on disk and loading that one each time. Got 1ms improvement but more used
     * memory for
     * repeated calls.
     *
     * @param r     Resources.
     * @param resId The original image with alpha channel.
     * @param color The new Color
     * @return Bitmap with new color.
     */
    public static Bitmap createRecoloredBitmap(Resources r, int resId, int color) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap source = BitmapFactory.decodeResource(r, resId);
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
     * @deprecated Use {@link #createRecoloredDrawable(android.content.res.Resources, int, int)}
     */
    @Deprecated
    public static BitmapDrawable createRecoloredDrawable(Context context, int resId, int color) {
        return createRecoloredDrawable(context.getResources(), resId, color);
    }

    /**
     * Wraps {@link #createRecoloredBitmap(android.content.Context, int, int)} in a {@link
     * android.graphics.drawable.BitmapDrawable}.
     *
     * @param resources Resources
     * @param resId     The original image with alpha channel.
     * @param color     The new Color
     * @return Bitmap with new color.
     */
    public static BitmapDrawable createRecoloredDrawable(Resources resources, int resId, int color) {
        return new BitmapDrawable(resources, createRecoloredBitmap(resources, resId, color));
    }

    /**
     * Wraps {@link #createRecoloredBitmap(android.graphics.Bitmap, int)} in a {@link
     * android.graphics.drawable.BitmapDrawable}.
     *
     * @param bitmap The original image with alpha channel.
     * @param color  The new Color
     * @return Bitmap with new color.
     * @deprecated {@link #createRecoloredDrawable(android.content.res.Resources, android.graphics.Bitmap, int)}
     */
    @Deprecated
    public static BitmapDrawable createRecoloredDrawable(Context context, Bitmap bitmap, int color) {
        return createRecoloredDrawable(context.getResources(), bitmap, color);
    }

    /**
     * Wraps {@link #createRecoloredBitmap(android.graphics.Bitmap, int)} in a {@link
     * android.graphics.drawable.BitmapDrawable}.
     *
     * @param bitmap The original image with alpha channel.
     * @param color  The new Color
     * @return Bitmap with new color.
     */
    public static BitmapDrawable createRecoloredDrawable(Resources resources, Bitmap bitmap, int color) {
        Assert.assertNotNull(resources);
        return new BitmapDrawable(resources, createRecoloredBitmap(bitmap, color));
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
            return UIL.loadSync(key, dp2px(width), dp2px(height));
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
            // Use 1/4th of the available memory for this memory cache.
            super(Math.max(0, (int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) / 4)));
            Log.i(TAG, "Cache Size: " + maxSize() / 1024f / 1024f + " MB");
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return sizeBitmap(bitmap);
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


