package com.carlosefonseca.common.utils;

import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import bolts.Task;
import com.carlosefonseca.common.CFApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the download of stuff from the tubes.
 * It's an extension of AsyncTask, so it will download asynchronously.
 * You can download to a file, which will be downloaded to the {@link android.os.Environment#getExternalStorageDirectory()}
 * folder,
 * or to a string.
 * The path to the file or the string will be returned to your handler.
 * You can pass in a ProgressDialog to be updated during the download.
 * You can pass in a shortened URL, it will expand it. Only tested with http://tiny.cc URLs.
 */
public final class AppLinks {

    private static final String TAG = CodeUtils.getTag(AppLinks.class);

    private AppLinks() {}

    public static Task<String> downloadString(final String url) {
        return Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return download(url);
            }
        });
    }

    public static String download(String urlStr) throws IOException {
        URL possiblyShortenedURL = new URL(urlStr);
        URL url;
        URLConnection connection;

        final String defaultUserAgent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultUserAgent = new WebView(CFApp.getContext()).getSettings().getUserAgentString();
        } else {
            defaultUserAgent = WebSettings.getDefaultUserAgent(CFApp.getContext());
        }

        HttpURLConnection ucon = (HttpURLConnection) possiblyShortenedURL.openConnection();
        ucon.setRequestProperty("User-Agent", defaultUserAgent);

        if (ucon.getResponseCode() != 200) {
            // yep, shortened
            ucon.setInstanceFollowRedirects(false);
            url = new URL(ucon.getHeaderField("Location"));
            connection = url.openConnection();
            ucon.setRequestProperty("User-Agent", defaultUserAgent);

        } else {
            // real url
            connection = ucon;
            url = possiblyShortenedURL;
        }
        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String fullText = "";
        String inputLine;

        final Pattern endHeadPattern = Pattern.compile(".*</\\s*head\\s*>.*", Pattern.CASE_INSENSITIVE);
        final Pattern alPattern = Pattern.compile(
                "<\\s*meta\\s+property\\s*=\\s*[\"']al:android:([^\"']+)[\"']\\s+content\\s*=\\s*[\"']([^\"']+)[\"']\\s*>",
                Pattern.CASE_INSENSITIVE);
        while ((inputLine = in.readLine()) != null) {
            Log.v(inputLine);
            fullText += inputLine + "\n";
            if (endHeadPattern.matcher(inputLine).matches()) break;
        }

        in.close();

        final Matcher matcher = alPattern.matcher(fullText);
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                Log.i(matcher.group(i));
            }
        }
        return fullText;
    }
}