package com.carlosefonseca.common.utils;

import android.os.AsyncTask;
import android.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class FileDownloader {
    private static final String TAG = CodeUtils.getTag(FileDownloader.class);

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 5;
    private static final int KEEP_ALIVE = 10;

    public static AtomicInteger sDownloads = new AtomicInteger();

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE);
    private static ThreadPoolExecutor sThreadPoolExecutor;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @NotNull
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private FileDownloader() {}


    public static void downloadFiles(List<Pair<String, File>> toDownload) {
        if (CollectionUtils.isEmpty(toDownload)) return;

        if (sNotifier == null) sNotifier = new Notification();
        if (sThreadPoolExecutor == null) {
            sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                                         MAXIMUM_POOL_SIZE,
                                                         KEEP_ALIVE,
                                                         TimeUnit.SECONDS,
                                                         sPoolWorkQueue,
                                                         sThreadFactory);
        }

        if (sDownloads.get() == 0 && toDownload.size() != 0) sNotifier.start(sDownloads.get());

        sDownloads.getAndAdd(toDownload.size());

        if (sDownloads.get() > 0) {
            boolean downloadingSomething = false;
            for (Pair<String, File> stringFilePair : toDownload) {
                if (stringFilePair.first.startsWith("http")) {
                    downloadingSomething = true;
                    //noinspection unchecked
                    new Downloader().execute(stringFilePair);
                } else {
                    Log.w(TAG, new RuntimeException("" + stringFilePair.first + " is not a URL"));
                }
            }
            if (!downloadingSomething) sNotifier.finished();
        }
    }


    static class Downloader extends AsyncTask<Pair<String, File>, Integer, String> {

        public static final int TIMEOUT_MILLIS = 15 * 1000;
        private static final String TAG = CodeUtils.getTag(Downloader.class);

        @SafeVarargs
        @Override
        protected final String doInBackground(Pair<String, File>... params) {
            String uri = params[0].first.replace(" ", "%20");
            File path = params[0].second;

            if (path.exists()) {
                return null;
            }

            try {
                File tempPath = new File(path.getAbsolutePath() + ".tmp");
                URL url = new URL(uri);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a typical 0-100% progress bar
//                int fileLength = connection.getContentLength();

                // download the file
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(TIMEOUT_MILLIS);
                urlConnection.setReadTimeout(TIMEOUT_MILLIS);
                InputStream input = new BufferedInputStream(urlConnection.getInputStream());

                OutputStream output = new FileOutputStream(tempPath);

                byte data[] = new byte[1024 * 10];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                if (!tempPath.renameTo(path)) {
                    Log.w(TAG, new RuntimeException("RENAME FAILED " + tempPath.getName() + " -> " + path.getName()));
                    return path.getName();
                }
                Log.v(TAG, String.format("(%d remain) Downloaded %s", sDownloads.get() - 1, path.getName()));
            } catch (SocketException | FileNotFoundException e) {
                Log.i(TAG, String.format("(%d remain) Download of %s failed: %s", sDownloads.get() - 1, uri, e.getMessage()));
                return path.getName();
            } catch (Exception e) {
                Log.i(TAG, String.format("(%d remain) Download of %s failed", sDownloads.get() - 1, uri), e);
                return path.getName();
            }
            return null;
        }

        @Override
        protected void onPostExecute(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") String failedFile) {
            if (failedFile != null) {
                sNotifier.fileFailed(failedFile);
                Log.d(TAG, "Download of " + failedFile + " failed");
            }
            sNotifier.queueUpdate(sDownloads.decrementAndGet());
            if (sDownloads.get() == 0) {
                sNotifier.finished();
            }
        }
    }

    ///
    ///  NOTIFIER
    ///
    private static FileDownloaderNotifier sNotifier;

    public static FileDownloaderNotifier getNotifier() {
        return sNotifier;
    }

    public static void setNotifier(FileDownloaderNotifier notifier) {
        FileDownloader.sNotifier = notifier;
    }

    public interface FileDownloaderNotifier {
        void start(int downloads);

        void finished();

        void queueUpdate(int i);

        void fileFailed(String failedFile);
    }

    public static class Notification implements FileDownloaderNotifier {
        @Override
        public void start(int downloads) {
            Log.i(TAG, "Downloads Starting. Count: " + downloads);
        }

        @Override
        public void finished() {
            Log.i(TAG, "Downloads finished");
        }

        @Override
        public void queueUpdate(int i) {
//            Log.v(TAG, "Queue Update. New count: " + sDownloads);
        }

        @Override
        public void fileFailed(String failedFile) {
            Log.w(TAG, "File Failed: " + failedFile);
        }
    }
}
