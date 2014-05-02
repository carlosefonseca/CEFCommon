package com.carlosefonseca.common.utils;

import android.os.AsyncTask;
import android.os.Build;
import junit.framework.Assert;
import org.apache.commons.collections4.CollectionUtils;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
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
    private static final int MAX_QUEUE_SIZE = 1024;
    private static ThreadPoolExecutor sThreadPoolExecutor;

    public static AtomicInteger sDownloadCount = new AtomicInteger();

    private FileDownloader() {}

    public static class Download {
        public final String url;
        public final File file;
        int tries;

        public Download(String url, File file) {
            Assert.assertTrue("" + url + " is not a URL", url.startsWith("http"));
            this.url = url;
            this.file = file;
        }

        boolean canRetry() {
            return tries < 5;
        }
    }

    public static void downloadFiles(List<Download> toDownload) {
        if (CollectionUtils.isEmpty(toDownload)) return;

        if (sDownloadCount.getAndAdd(toDownload.size()) == 0) {
            getNotifier().start(sDownloadCount.get());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor();
            for (Download download : toDownload) new Downloader().executeOnExecutor(sThreadPoolExecutor, download);
        } else {
            for (Download download : toDownload) new Downloader().execute(download);
        }
    }

    static void download(Download toDownload) {
        if (!toDownload.canRetry()) {
            Log.w(TAG, "Download of '" + toDownload.url + "' reached the limit of retries.");
            return;
        }
        sDownloadCount.addAndGet(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getThreadPoolExecutor();
            new Downloader().executeOnExecutor(sThreadPoolExecutor, toDownload);
        } else {
            new Downloader().execute(toDownload);
        }
    }

    private static void getThreadPoolExecutor() {
        if (sThreadPoolExecutor == null) {
            sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                                         MAX_POOL_SIZE,
                                                         KEEP_ALIVE_TIME,
                                                         TimeUnit.SECONDS,
                                                         new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE));
        }
    }


    static class Downloader extends AsyncTask<Download, Integer, String> {

        public static final int TIMEOUT_MILLIS = 15 * 1000;
        private static final String TAG = CodeUtils.getTag(Downloader.class);

        @Override
        protected final String doInBackground(Download... params) {
            final Download download = params[0];
            String uri = download.url.replace(" ", "%20");
            File path = download.file;
//            Log.v(TAG, uri);
            download.tries++;

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
                    tempPath.delete();
                    path.delete();
                    download(download);
                    return path.getName();
                }
                Log.v(TAG,
                      String.format("(%d remain) Downloaded %s%s",
                                    sDownloadCount.get() - 1,
                                    path.getName(),
                                    download.tries > 1 ? " " + download.tries + " tries" : "")
                );
            } catch (SocketException e) {
                // Network error. May retry
                Log.i(TAG,
                      String.format("(%d remain) Download of %s failed (will retry): %s",
                                    sDownloadCount.get() - 1,
                                    uri,
                                    e.getMessage())
                );
                download(download);
                return path.getName();
            } catch (FileNotFoundException e) {
                // URL is wrong - do not retry
                Log.i(TAG, String.format("(%d remain) Download of %s failed: %s", sDownloadCount.get() - 1, uri, e.getMessage()));
                return path.getName();
            } catch (Exception e) {
                Log.i(TAG, String.format("(%d remain) Download of %s failed", sDownloadCount.get() - 1, uri), e);
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
            final int i = sDownloadCount.decrementAndGet();
            sNotifier.queueUpdate(i);
            if (i == 0) {
                sNotifier.finished();
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
//            Log.v(TAG, "Queue Update. New count: " + sDownloadCount);
        }

        @Override
        public void fileFailed(String failedFile) {
            Log.w(TAG, "File Failed: " + failedFile);
        }
    }
}