package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import com.carlosefonseca.common.CFApp;
import com.carlosefonseca.common.widgets.LoadingDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UrlUtils {

    private UrlUtils() {}

    public static String urlForEmail(String email) {return "mailto:" + email.trim();}

    public static String urlForTel(String number) {return "tel:" + number.trim().replaceAll("\\s*", "");}

    public static final class Facebook {
        private Facebook() {}

        private static final String TAG = CodeUtils.getTag(Facebook.class);
        public static final String FB_PREFS = "fbids";
        public static final String FB_PACKAGE_NAME = "com.facebook.katana";

        /**
         * Supports the following URL's:
         * <pre>{@code
         * https://www.facebook.com/xtourmaker
         *  http://www.facebook.com/xtourmaker
         *         www.facebook.com/xtourmaker
         *             facebook.com/xtourmaker
         * https://www.facebook.com/pages/Beware/255500287862344
         *  http://www.facebook.com/pages/Beware/255500287862344
         *         www.facebook.com/pages/Beware/255500287862344
         *             facebook.com/pages/Beware/255500287862344
         * + optional ending slash
         * + optional query string
         * }</pre>
         */
        public static final Pattern PATTERN = Pattern.compile(
                "^(?:https?://)?(?:www\\.)?facebook\\.com/(?:([^/]*)|pages(?:.*)/(\\d+))/?(?:\\?.*)?$");


        public static String getFacebookNameFromURL(String url) {
            Matcher matcher = PATTERN.matcher(url);
            if (matcher.find()) {
                return StringUtils.defaultString(matcher.group(1), matcher.group(2));
            }
            return null;
        }

        public static void openFacebookPage(final Context context, final String text)
                throws NetworkingUtils.NotConnectedException {
            getOpenFacebookIntent(context, text, new RunnableWith<Intent>() {
                @Override
                public void run(Intent intent) {
                    context.startActivity(intent);
                }
            });
        }

        public static void getOpenFacebookIntent(Context context,
                                                 final String text,
                                                 final RunnableWith<Intent> runnable)
                throws NetworkingUtils.NotConnectedException {
            try {
                //Checks if FB is even installed.
                context.getPackageManager().getPackageInfo(FB_PACKAGE_NAME, 0);

                // Check ID cache
                String fbid = StringUtils.isNumeric(text)
                              ? text
                              : CFApp.getUserPreferences(FB_PREFS).getString(text, null);

                // Fetch ID
                if (fbid == null) {
                    if (!NetworkingUtils.hasInternet()) {
                        throw new NetworkingUtils.NotConnectedException();
                    }

                    // CHANGE TO TASK AND VOLLEY
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get("https://graph.facebook.com/" + text, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers,
                                              String responseString,
                                              Throwable throwable) {
                            Log.w(TAG, throwable.getMessage());
                            runnable.run(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + text)));
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String response) {
                            try {
                                String fbid = new JSONObject(response).getString("id");
                                CFApp.getUserPreferences(FB_PREFS).edit().putString(text, fbid).apply();
                                //Tries to make intent with FB's URI
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + fbid));
                                runnable.run(intent);
                            } catch (JSONException e) {
                                Log.w(TAG, "" + e.getMessage());
                                runnable.run(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.facebook.com/" + text)));
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

        /* EXAMPLE USAGE */
        public static boolean tryOpenUrl(Context context, @NotNull String url) {
            if (url == null) return false;
            String facebookName = Facebook.getFacebookNameFromURL(url);
            if (facebookName != null) {
                try {
                    Facebook.openFacebookPage(context, facebookName);
                } catch (NetworkingUtils.NotConnectedException e) {
                    LoadingDialog.ErrorDialog(context, "NO INTERNET");
                }
                return true;    // No further action needed
            } else {
                return false;   // You should do something else with your URL
            }
        }

    }

    public static final class Twitter {
        private Twitter() {}

        private static final String TAG = CodeUtils.getTag(Twitter.class);
        public static final String TWITTER_USER_NAME_URL = "https?://twitter\\.com/([^/]+)/?$";
        public static final Pattern TWITTER_USER_NAME_REGEX = Pattern.compile(TWITTER_USER_NAME_URL);


        public static void openProfile(Context context, String userName) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + userName));
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (infos.isEmpty()) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + userName));
            }
            context.startActivity(intent);
        }

        public static boolean tryOpenProfileFromURL(Context context, @NotNull String url) {
            if (url == null) return false;
            final Matcher matcher = TWITTER_USER_NAME_REGEX.matcher(url);
            if (matcher.matches()) {
                final String userName = matcher.group(1);
                openProfile(context, userName);
                return true;
            }
            return false;
        }
    }


    public static final class Coordinates {

        private static Boolean geoOk;

        private Coordinates() {}

        public static String urlForCoordinates(@Nullable String name, @NotNull String coordinates) {
            name = StringUtils.stripToNull(name);
            return "geo:0,0?q=" + coordinates + (name != null ? " (" + name + ")" : "");
        }

//      "http://maps.google.com/maps?q=" + StringUtils.normalizeSpace(getName()).replaceAll(StringUtils.SPACE, "%20");

        @Nullable
        public static String urlForAddress(@Nullable String address) {
            if (address == null) return null;
            return "geo:0,0?q=" + StringUtils.normalizeSpace(address);
        }

        public static String urlForCoordinates(String name, double lat, double lng) {
            return urlForCoordinates(name, lat + "," + lng);
        }

        public static String urlForCoordinates(String name, Location location) {
            return urlForCoordinates(name, location.getLatitude(), location.getLongitude());
        }

        public static boolean canHandleDirections(Context context) {
            if (geoOk == null) geoOk = canHandleUrl(context, "geo:0,0");
            return geoOk;
        }

        public static String getIntent(Context context, String url) {
            if (canHandleDirections(context)) {
                return url;
            } else {
                return url.replace("geo:0,0?q=", "http://maps.google.com/maps?q=");
            }
        }

        /**
         * Tries to open a previously generated coordinates URL.
         * Only works with standard "geo:" urls like the
         * ones generated by this class. Starts by checking if the system can open a geo: url,
         * otherwise it'll fallback to a Google Maps url.
         *
         * @return True if the url was opened; False if the url doesn't start with "geo:".
         */
        public static boolean tryOpenUrl(Context context, @NotNull String url) {
            if (url == null) return false;
            return url.startsWith("geo:") && tryStartIntentForUrl(context, getIntent(context, url));
        }
    }

    public static boolean tryStartIntentForUrl(Context context, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (canHandleIntent(context, intent)) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    public static boolean canHandleUrl(Context context, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }

    public static boolean canHandleIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }

    /**
     * Checks for apps that handle the URL in the following order: Facebook, Twitter, Maps, anything else.
     * @return True if the URL was handled and something opened; False if the URL could not be handled.
     */
    public static boolean tryAll(Context context, String url) {
        if (url == null) throw new RuntimeException("URL is null!");
        return Facebook.tryOpenUrl(context, url) || Twitter.tryOpenProfileFromURL(context, url) ||
               Coordinates.tryOpenUrl(context, url) || UrlUtils.tryStartIntentForUrl(context, url);
    }
}
