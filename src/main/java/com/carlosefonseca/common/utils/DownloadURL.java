package com.carlosefonseca.common.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class handles the download of stuff from the tubes.
 * It's an extension of AsyncTask, so it will download asynchronously.
 * You can download to a file, which will be downloaded to the {@link android.os.Environment#getExternalStorageDirectory()} folder,
 * or to a string.
 * The path to the file or the string will be returned to your handler.
 * You can pass in a ProgressDialog to be updated during the download.
 * You can pass in a shortened URL, it will expand it. Only tested with http://tiny.cc URLs.
 */
public class DownloadURL extends AsyncTask<String, Integer, String> {

    private static final String TAG = CodeUtils.getTag(DownloadURL.class);

    public static class DownloadResult {
        public void onFile(String path) {}

        public void onString(String string){}

        public void onFail() {}
    }

    ProgressDialog progressDialog;
    boolean isFile;
    private final DownloadResult handler;


    public DownloadURL(ProgressDialog pd, boolean isFile, DownloadResult handler) {
        progressDialog = pd;
        this.isFile = isFile;
        this.handler = handler;
    }

    @Nullable
    @Override
    protected String doInBackground(String... sUrl) {
        try {
            URL possiblyShortenedURL = new URL(sUrl[0]);
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

            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            Log.v(TAG, "File length: " + fileLength);

            if (fileLength == -1) {
                return null;
            }

            if (progressDialog != null)
                progressDialog.setMax(fileLength / 1024);


            if (isFile) {
                // download the file
                InputStream input = new BufferedInputStream(url.openStream());

                long total = 0;
                int count;
                byte data[] = new byte[1024];
                File file = new File(Environment.getExternalStorageDirectory().getPath(), new File(url.getPath()).getName());
                OutputStream output = new FileOutputStream(file);

                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) break;
                    total += count;
                    // publishing the progress....
                    publishProgress((int) total / 1024);// (int) (total * 100 / fileLength));
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


        } catch (Exception e) {
            Log.w(TAG, "Download of " + sUrl[0] + " failed - " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog != null)
            progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (progressDialog != null)
            progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        if (s == null)
            handler.onFail();
        if (isFile)
            handler.onFile(s);
        else
            handler.onString(s);
    }
}