package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.carlosefonseca.common.CFApp;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;

/**
 * Util methods for handling Files.
 */
public final class FileUtils {
    public static final String ASSETS_PREFIX = "@assets/";
    private static final String TAG = getTag(FileUtils.class);

    private FileUtils() {}

    /**
     * Copies a file from one place to another.
     * @param src The full path to the original file.
     * @param dst The full path the destination. Parent dirs will be created if needed.
     */
    public static void copy(File src, File dst) throws IOException {
        if (!src.exists()) return;

        //noinspection ResultOfMethodCallIgnored
        dst.getParentFile().mkdirs();

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        copyStreams(in, out);
        in.close();
        out.close();
    }

    /**
     * Copies a file from the assets folder to the file system.
     * @param context A context.
     * @param src The path to the file relative to the assets folder.
     * @param dst The full path the destination. Parent dirs will be created if needed.
     * @return True if the copy was successful, false otherwise.
     */
    public static boolean copyFromAssets(Context context, String src, File dst) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open(src);
            //noinspection ResultOfMethodCallIgnored
            dst.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(dst);
            copyStreams(in, out);
            in.close();
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Failed to copy asset file: " + src + " - " + e.getMessage());
        }
        return false;
    }

    public static List<String> assetsFileList(Resources resources, @Nullable String subfolder) {
        try {
            return Arrays.asList(resources.getAssets().list(StringUtils.defaultString(subfolder)));
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Reads from a stream and writes on another.
     * @throws IOException
     */
    private static void copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public static void clearExternalCache(Context context) {
        try {
            File dir = context.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            Log.w(TAG+".clearExternalCache", e.getMessage());
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) return false;
                }
            }
            // The directory is now empty so delete it
            return dir.delete();
        }
        return true;
    }

    public static String StrFromFile(String file) {
        try {
            InputStream is;
            if (file.startsWith(ASSETS_PREFIX)) {
                file = file.replace(ASSETS_PREFIX, "");
                is = CFApp.getContext().getAssets().open(file);
            } else {
                is = new FileInputStream(file);
            }
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error opening the file " + file, e);
        }
        return null;
    }

    public static String StrFromFile(File file) {
        try {
            InputStream is;
            is = new FileInputStream(file);
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found " + file);
        }
        return null;
    }

    public static void StrToFile(String text, File file) {
        try {
            OutputStream os;
            os = new FileOutputStream(file);
            os.write(text.getBytes());
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file " + file, e);
        }
    }

    public static String StrFromFileInAssets(String file) {
        try {
            InputStream is;
            is = CFApp.getContext().getAssets().open(file);
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error opening the file " + file, e);
        }
        return null;
    }
}
