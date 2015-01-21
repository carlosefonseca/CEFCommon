package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.carlosefonseca.common.CFApp;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
     *
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
     *
     * @param context A context.
     * @param src     The path to the file relative to the assets folder.
     * @param dst     The full path the destination. Parent dirs will be created if needed.
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

    public static List<String> extFileList(@Nullable String subfolder, final Pattern pattern) {
        File dir = CFApp.getContext().getExternalFilesDir(null);
        if (dir == null) {
            Log.e(TAG, new RuntimeException("getExternalFilesDir failed"));
            return Collections.emptyList();
        }
        if (subfolder != null) dir = new File(dir, subfolder);

        FilenameFilter filter;
        String[] files;
        if (pattern != null) {
            filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return pattern.matcher(filename).matches();
                }
            };
            files = dir.list(filter);
        } else {
            files = dir.list();
        }

        return Arrays.asList(files);
    }

    /**
     * Reads from a stream and writes on another.
     *
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
            Log.w(TAG + ".clearExternalCache", e.getMessage());
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

    @Nullable
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

    /**
     * @return String from the file or null if doesn't exist.
     */
    @Nullable
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

    public static boolean StrToFile(String text, File file) {
        try {
            OutputStream os;
            os = new FileOutputStream(file);
            os.write(text.getBytes());
            return true;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file " + file, e);
        }
        return false;
    }

    @Nullable
    public static String StrFromFileInAssets(String file) {
        try {
            InputStream is;
            is = CFApp.getContext().getAssets().open(file);
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found in assets: " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error opening the file in assets: " + file, e);
        }
        return null;
    }

    @Nullable
    public static String StrFromFileInAssets(Context context, String file) {
        try {
            InputStream is;
            is = context.getAssets().open(file);
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found in assets: " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error opening the file in assets: " + file, e);
        }
        return null;
    }

    public static void writeObject(Serializable serializable, File file) {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(serializable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    public static <T> T readObject(File file) {
        ObjectInputStream stream = null;
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            stream = new ObjectInputStream(input);
            //noinspection unchecked
            return (T) stream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);

        } finally {
            try {
                if (stream != null) {
                    stream.close();
                } else if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }
}
