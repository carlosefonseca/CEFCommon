package com.carlosefonseca.common.utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import com.carlosefonseca.apache.commons.collections4.CollectionUtils;
import com.carlosefonseca.common.CFApp;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class FileDownloader {
    private static final String TAG = CodeUtils.getTag(FileDownloader.class);

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE_TIME = 1L;
    private static final int MAX_QUEUE_SIZE = 2048;
    private static ThreadPoolExecutor sThreadPoolExecutor;

    public static AtomicInteger sDownloadCount = new AtomicInteger();

    private static boolean cancelAll;

    private FileDownloader() {}

    public static void cancelAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor().shutdownNow();
        }
        cancelAll = true;
        sNotifier.canceled();
        sDownloadCount.set(0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public static String syncDownload(Download download) {
        String uri = download.url.replace(" ", "%20");
        File path = download.file;
//            Log.v(TAG, uri);
        download.tries++;

        if (Thread.currentThread().isInterrupted()) return path.getName();

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
            urlConnection.setConnectTimeout(Downloader.TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(Downloader.TIMEOUT_MILLIS);

            if (Thread.currentThread().isInterrupted()) return path.getName();

            InputStream input = new BufferedInputStream(urlConnection.getInputStream());
            OutputStream output = new FileOutputStream(tempPath);
            try {
                if (copy(input, output) == -1) {
                    Log.i(TAG, String.format("(%d remain) Download of %s INTERRUPTED", sDownloadCount.get() - 1, uri));
                    return path.getName();
                }
            } catch (IOException e) {
                // Network error. May retry
                Log.i(TAG, String.format("(%d remain) Download of %s failed (will retry): %s", sDownloadCount.get() - 1, uri, e.getMessage()));
                download(download);
                return path.getName();
            } finally {
                output.flush();
                output.close();
                input.close();
            }

            if (!tempPath.renameTo(path)) {
                Log.w(TAG,
                      "RENAME FAILED %s (%d) -> %s (%d)",
                      tempPath.getAbsolutePath(),
                      tempPath.length(),
                      path.getAbsolutePath(),
                      path.length());
                tempPath.delete();
                path.delete();
                download(download);
                return path.getName();
            }
            Log.v(TAG,
                  String.format("(%d remain) Downloaded %s%s",
                                sDownloadCount.get() - 1,
                                path.getName(),
                                download.tries > 1 ? " " + download.tries + " tries" : ""));
            return null; // SUCCESS!
        } catch (SocketException e) {
            // Network error. May retry
            Log.i(TAG,
                  String.format("(%d remain) Failed on  %s (will retry) %s: %s ",
                                sDownloadCount.get() - 1,
                                path.getName(),
                                uri,
                                e.getMessage()));
            download(download);
            return path.getName();
        } catch (FileNotFoundException e) {
            // URL is wrong - do not retry
            Log.i(TAG, "(%d remain) Failed on  %s - %s - %s", sDownloadCount.get() - 1, path.getName(), uri, e.getMessage());
            return path.getName();
        } catch (Exception e) {
            Log.i(TAG, "(%d remain) Failed on  %s - %s", sDownloadCount.get() - 1, path.getName(), uri, e);
            return path.getName();
        }
    }

    /**
     * Copy, modified to return -1 if the thread is interrupted.
     * @see com.carlosefonseca.common.utils.IOUtils#copyLarge(java.io.InputStream, java.io.OutputStream)
     */
    protected static long copy(InputStream input, OutputStream output) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 1024 * 4;
        final int EOF = -1;

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count1 = 0;
        int n;
        Thread thread = Thread.currentThread();
        while (EOF != (n = input.read(buffer))) {
            if (thread.isInterrupted()) return -1;
            output.write(buffer, 0, n);
            count1 += n;
        }
        return count1;
    }

    public static class Download {
        public final String url;
        public final File file;
        int tries;

        public Download(String url, File file) {
            if (!url.startsWith("http")) Log.w(TAG, new RuntimeException("" + url + " is not a URL."));
            this.url = url;
            this.file = file;
        }

        public Download(String url) {
            this(url, new File(CFApp.getContext().getExternalCacheDir(), NetworkingUtils.getLastSegmentOfURL(url)));
        }

        boolean canRetry() {
            return tries < 5;
        }

        @Override
        public int hashCode() {
            return CodeUtils.hashCode(68, file);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Download && file.equals(((Download) o).file);
        }
    }

    public static void downloadFiles(final Collection<Download> toDownload) {
        if (CollectionUtils.isEmpty(toDownload)) return;

        if (sDownloadCount.getAndAdd(toDownload.size()) == 0) {
            cancelAll = false;
            getNotifier().start(sDownloadCount.get());
        }

        downloadList(toDownload);
    }

    public static void downloadUrls(final Collection<String> toDownload) {
        if (CollectionUtils.isEmpty(toDownload)) return;

        if (sDownloadCount.getAndAdd(toDownload.size()) == 0) {
            cancelAll = false;
            getNotifier().start(sDownloadCount.get());
        }

        downloadStringList(toDownload);
    }

    protected static void downloadOne(final String url) {
        downloadOne(new Download(url));
    }

    //region DOWNLOAD CODE
    protected static void downloadStringList(final Collection<String> toDownload) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                for (String url : toDownload)
                    new Downloader().executeOnExecutor(sThreadPoolExecutor, new Download(url));
            } else {
                CodeUtils.runOnUIThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run() {
                        for (String url : toDownload)
                            new Downloader().executeOnExecutor(sThreadPoolExecutor, new Download(url));
                    }
                });
            }
        } else {
            for (String url : toDownload) {
                if (!cancelAll) new Downloader().execute(new Download(url));
            }
        }
    }

    protected static void downloadList(final Collection<Download> toDownload) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                for (Download url : toDownload)
                    new Downloader().executeOnExecutor(sThreadPoolExecutor, url);
            } else {
                CodeUtils.runOnUIThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run() {
                        for (Download url : toDownload)
                            new Downloader().executeOnExecutor(sThreadPoolExecutor, url);
                    }
                });
            }
        } else {
            for (Download url : toDownload) {
                if (!cancelAll) new Downloader().execute(url);
            }
        }
    }

    protected static void downloadOne(final Download download) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                new Downloader().executeOnExecutor(sThreadPoolExecutor, download);
            } else {
                CodeUtils.runOnUIThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run() {
                        new Downloader().executeOnExecutor(sThreadPoolExecutor, download);
                    }
                });
            }
        } else {
            if (!cancelAll) new Downloader().execute(download);
        }
    }
    //endregion

    static void download(Download toDownload) {
        if (!toDownload.canRetry()) {
            Log.w(TAG, "Download of '" + toDownload.url + "' reached the limit of retries.");
            return;
        }
        sDownloadCount.addAndGet(1);
        downloadOne(toDownload);
    }

    private static ThreadPoolExecutor getThreadPoolExecutor() {
        if (sThreadPoolExecutor == null || sThreadPoolExecutor.isShutdown()) {
            Log.i("NEW ThreadPoolExecutor");
            sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                                         MAX_POOL_SIZE,
                                                         KEEP_ALIVE_TIME,
                                                         TimeUnit.SECONDS,
                                                         new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE));
        }
        return sThreadPoolExecutor;
    }


    public static class Downloader extends AsyncTask<Download, Integer, String> {

        public static final int TIMEOUT_MILLIS = 15 * 1000;
        private static final String TAG = CodeUtils.getTag(Downloader.class);

        @Nullable
        @Override
        protected final String doInBackground(Download... params) {
            final Download download = params[0];
            return syncDownload(download);
        }

        @Override
        protected void onPostExecute(String failedFile) {
            if (!cancelAll) {
                if (failedFile != null) {
                    sNotifier.fileFailed(failedFile);
//                Log.d(TAG, "Download of " + failedFile + " failed");
                }
                final int i = sDownloadCount.decrementAndGet();
                sNotifier.queueUpdate(i);
                if (i == 0) {
                    sNotifier.finished();
                }
            }
        }
    }

    ///
    ///  NOTIFIER
    ///
    private static FileDownloaderNotifier sNotifier;

    public static FileDownloaderNotifier getNotifier() {
        if (sNotifier == null) sNotifier = new Notification();
        return sNotifier;
    }

    public static void setNotifier(FileDownloaderNotifier notifier) {
        FileDownloader.sNotifier = notifier;
    }

    public interface FileDownloaderNotifier {
        void start(int downloads);

        void finished();

        void canceled();

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
        public void canceled() {
            Log.i(TAG, "Downloads Canceled!");
        }

        @Override
        public void queueUpdate(int i) {
//            Log.v(TAG, "Queue Update. New count: " + sDownloadCount);
        }

        @Override
        public void fileFailed(String failedFile) {
            Log.w(TAG, "File Failed: " + failedFile);
        }
    }
}
