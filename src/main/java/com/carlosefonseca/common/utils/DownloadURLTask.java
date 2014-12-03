package com.carlosefonseca.common.utils;

import android.os.Environment;
import bolts.Task;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 * This class handles the download of stuff from the tubes.
 * It's an extension of AsyncTask, so it will download asynchronously.
 * You can download to a file, which will be downloaded to the {@link android.os.Environment#getExternalStorageDirectory()} folder,
 * or to a string.
 * The path to the file or the string will be returned to your handler.
 * You can pass in a ProgressDialog to be updated during the download.
 * You can pass in a shortened URL, it will expand it. Only tested with http://tiny.cc URLs.
 */
public final class DownloadURLTask  {

    private static final String TAG = CodeUtils.getTag(DownloadURLTask.class);

    private DownloadURLTask() {}

    public static Task<String> downloadString(final String url) {
        return Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return download(url, false);
            }
        });
    }

    private static String download(String urlStr, boolean isFile) throws IOException {
        URL possiblyShortenedURL = new URL(urlStr);
        URL url;
        URLConnection connection;

        HttpURLConnection ucon = (HttpURLConnection) possiblyShortenedURL.openConnection();
        if (ucon.getResponseCode() != 200) {
            // yep, shortened
            ucon.setInstanceFollowRedirects(false);
            url = new URL(ucon.getHeaderField("Location"));
            connection = url.openConnection();
        } else {
            // real url
            connection = ucon;
            url = possiblyShortenedURL;
        }
        connection.connect();
        Log.d(TAG, url.toString());

        if (isFile) {
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            Log.v(TAG, "File length: " + fileLength);

            if (fileLength == -1) {
                return null;
            }
        }

        if (isFile) {
            // download the file
            InputStream input = new BufferedInputStream(url.openStream());

            int count;
            byte data[] = new byte[1024];
            File file = new File(Environment.getExternalStorageDirectory().getPath(), new File(url.getPath()).getName());
            OutputStream output = new FileOutputStream(file);

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            return file.getAbsolutePath();
        } else {

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String fullText = "";
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                fullText += inputLine+"\n";

            in.close();
            return fullText.trim();
        }
    }
}