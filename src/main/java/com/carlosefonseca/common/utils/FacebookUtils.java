package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.carlosefonseca.common.CFApp;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FacebookUtils {
    private static final java.lang.String TAG = CodeUtils.getTag(FacebookUtils.class);
    public static final String FB_PREFS = "fbids";
    public static final String FB_PACKAGE_NAME = "com.facebook.katana";

    private FacebookUtils() {}

    public static String getFacebookNameFromURL(String url) {
        Matcher matcher = Pattern.compile("(https://)?(www\\.)?facebook\\.com/(.*)").matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }

    public static void openFacebookPage(final Context context, final String text) throws NetworkingUtils.NotConnectedException {
        getOpenFacebookIntent(context, text, new RunnableWith<Intent>() {
            @Override
            public void run(Intent intent) {
                context.startActivity(intent);
            }
        });
    }

    public static void getOpenFacebookIntent(Context context, final String text, final RunnableWith<Intent> runnable)
            throws NetworkingUtils.NotConnectedException {
        try {
            //Checks if FB is even installed.
            context.getPackageManager().getPackageInfo(FB_PACKAGE_NAME, 0);

            // Check ID cache
            String fbid = CFApp.getUserPreferences(FB_PREFS).getString(text, null);

            // Fetch ID
            if (fbid == null) {
                if (!NetworkingUtils.hasInternet()) {
                    throw new NetworkingUtils.NotConnectedException();
                }

                AsyncHttpClient client = new AsyncHttpClient();
                client.get("https://graph.facebook.com/" + text, new AsyncHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, error.getMessage());
                        runnable.run(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + text)));
                    }

                    @Override
                    public void onSuccess(String response) {
                        try {
                            String fbid = new JSONObject(response).getString("id");
                            CFApp.getUserPreferences(FB_PREFS).edit().putString(text, fbid).commit();
                            //Tries to make intent with FB's URI
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + fbid));
                            runnable.run(intent);
                        } catch (JSONException e) {
                            Log.w(TAG, "" + e.getMessage());
                            runnable.run(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + text)));
                        }
                    }
                });

            } else {
                // has ID
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + fbid));
                runnable.run(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            // opens a url to the desired page
            runnable.run(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + text)));
        }
    }
}