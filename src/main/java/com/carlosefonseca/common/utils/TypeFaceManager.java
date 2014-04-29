package com.carlosefonseca.common.utils;

import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.TextView;
import com.carlosefonseca.common.CFApp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;

/**
 * Helper to set typefaces on views.
 * The general use case is to have a TypeFaces enum with the font names matching the font files on assets/fonts/.
 * Then use {@link #setTypeFace(Enum, android.widget.TextView...)}.
 * <p/>
 * Loaded typefaces are cached and .ttf and .otf are supported.
 */
@SuppressWarnings("UnusedDeclaration")
public final class TypeFaceManager {
    private static final String TAG = getTag(TypeFaceManager.class);
    public static final String FONTS_FOLDER = "fonts/";
    public static SparseArray<Typeface> typeFaceCache = new SparseArray<Typeface>();
    private static List<String> filelist;
    private static List<String> extensions = Arrays.asList(".ttf", ".otf");

    private TypeFaceManager() {}

    /**
     * Applies the specified typeface to a list of TextViews.
     *
     * @param typeface  The typeface to apply, from a TypeFace enum.
     * @param textViews The TextViews.
     * @param <T>       TypeFaces Enum.
     */
    public static <T extends Enum> void setTypeFace(T typeface, TextView... textViews) {
        if (typeface == null) return;
        Typeface typeFace = getTypeFace(typeface);
        for (TextView textView : textViews) {
            if (textView != null) textView.setTypeface(typeFace);
        }
    }

    /**
     * Applies the specified typeface to a set of views in the specified container. Use this if you need typefaces on views that
     * you don't access on your code, since this will perform findViewById, which has its costs.
     *
     * @param typeface The typeface to apply, from a TypeFace enum.
     * @param wrapper  The view group that holds all the target subviews.
     * @param views    The resource IDs for the target views.
     * @param <T>      TypeFaces Enum.
     */
    public static <T extends Enum> void setTypeFace(T typeface, ViewGroup wrapper, int... views) {
        if (typeface == null) return;
        Typeface typeFace = getTypeFace(typeface);
        TextView textView;
        for (int view : views) {
            textView = (TextView) wrapper.findViewById(view);
            if (textView != null) textView.setTypeface(typeFace);
        }
    }

    /**
     * Obtains a TypeFace from the enum constant. If it's not already in the cache, it will load the font from the defined fonts
     * folder.
     *
     * @param typeface The typeface to obtain, from a TypeFace enum.
     * @param <T>      TypeFaces Enum.
     */
    public static <T extends Enum> Typeface getTypeFace(T typeface) {
        if (typeface == null) return Typeface.DEFAULT;
        Typeface typeface1;
        if (typeFaceCache.indexOfKey(typeface.ordinal()) < 0) {
            try {
                String name = typeface.toString().toLowerCase();
                String ext = "";
                for (String extension : extensions) {
                    if (getFileList().contains(name + extension)) {
                        ext = extension;
                        break;
                    }
                }

                //noinspection SizeReplaceableByIsEmpty
                if (ext.length() == 0) {
                    Log.w(TAG, "Can't find " + FONTS_FOLDER + name + extensions);
                    typeface1 = Typeface.DEFAULT;
                } else {
                    typeface1 = Typeface.createFromAsset(CFApp.getContext().getAssets(), FONTS_FOLDER + name + ext);
                }
                typeFaceCache.put(typeface.ordinal(), typeface1);
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage(), e);
                typeface1 = Typeface.DEFAULT;
            }
            return typeface1;
        }
        return typeFaceCache.get(typeface.ordinal());
    }

    /**
     * Obtains the list of files in assets/fonts
     *
     * @return List of filenames.
     */
    private static List<String> getFileList() {
        if (filelist == null) {
            try {
                filelist = Arrays.asList(CFApp.getContext().getAssets().list(FONTS_FOLDER.replace("/", "")));
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
        return filelist;
    }
}