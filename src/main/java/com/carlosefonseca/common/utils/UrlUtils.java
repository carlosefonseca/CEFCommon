package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import bolts.Continuation;
import bolts.Task;
import com.carlosefonseca.apache.commons.lang3.StringUtils;
import com.carlosefonseca.common.CFApp;
import com.carlosefonseca.common.widgets.LoadingDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.carlosefonseca.apache.commons.lang3.StringUtils.defaultString;

public final class UrlUtils {

    private static final java.lang.String TAG = CodeUtils.getTag(UrlUtils.class);

    private UrlUtils() {}


    public static final Pattern phoneMatcher= Pattern.compile("[+(]?(\\d[()]?[- .]?[()]?){8,}\\d");
    // Previously: (fails with years like 1990-2000
    // public static final Pattern phoneMatcher= Pattern.compile("([\\d(+](?:[\\d()./+-]+ ?){6,}[\\d)])");
    public static final Pattern httpMatcher= Pattern.compile("\\b(https?://[^\\s]+)");
    public static final Pattern wwwMatcher= Pattern.compile("\\b(www\\.[^\\s]+)");
    public static final Pattern emailMatcher = Pattern.compile("([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4})");


    public static String urlForEmail(String email) {return "mailto:" + email.trim();}

    public static String urlForTel(String number) {return "tel:" + number.trim().replaceAll("\\s*", "");}

    @Nullable
    public static String simplifyUrlForDisplay(@Nullable String url) {
        return url == null ? null : url.trim().replaceFirst("https?://(www.)?", "").replace("?fref=ts", "");
    }

    /**
     * Takes any string but only returns those that start with HTTP. Returns null otherwise.
     */
    @Nullable public static String filterHttp(@Nullable String url) {
        return url != null && url.startsWith("http") ? url : null;
    }

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
                "^(?:https?://)?(?:www\\.)?facebook\\.com/(?:([^/?]*)|pages(?:.*)/(\\d+))/?(?:\\?.*)?$");


        public static String getFacebookNameFromURL(String url) {
            Matcher matcher = PATTERN.matcher(url);
            if (matcher.find()) {
                return defaultString(matcher.group(1), matcher.group(2));
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

                    final Continuation<String, Void> continuation = new Continuation<String, Void>() {
                        @Override
                        public Void then(Task<String> task) throws Exception {
                            if (TaskUtils.hasErrors(TAG, "failed", task)) {
                                runnable.run(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.facebook.com/" + text)));
                            } else {
                                try {
                                    String fbid = new JSONObject(task.getResult()).getString("id");
                                    CFApp.getUserPreferences(FB_PREFS).edit().putString(text, fbid).apply();
                                    //Tries to make intent with FB's URI
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + fbid));
                                    runnable.run(intent);
                                } catch (Exception e) {
                                    Log.w(TAG, "" + e.getMessage());
                                    runnable.run(new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("https://www.facebook.com/" + text)));
                                }
                            }
                            return null;
                        }
                    };
                    DownloadURLTask.downloadString("https://graph.facebook.com/" + text).continueWith(continuation);

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

        public static String getUserUrl(String username) {
            if (username == null) return null;
            return "https://twitter.com/" + (username.startsWith("@") ? username.substring(1) : username);
        }
    }

    public static final class Instagram {
        private Instagram() {}

        private static final String TAG = CodeUtils.getTag(Twitter.class);
        public static final String IG_USER_NAME_URL = "https?://instagram\\.com/([^/]+)/?$";
        public static final Pattern IG_USER_NAME_REGEX = Pattern.compile(IG_USER_NAME_URL);


        public static void openProfile(Context context, String userName) {
            if (userName.startsWith("@")) userName = userName.substring(1);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/" + userName));
            intent.setPackage("com.instagram.android");
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (infos.isEmpty()) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getUserUrl(userName)));
            }
            context.startActivity(intent);
        }

        public static boolean tryOpenProfileFromURL(Context context, @NotNull String url) {
            if (url == null) return false;
            final Matcher matcher = IG_USER_NAME_REGEX.matcher(url);
            if (matcher.matches()) {
                final String userName = matcher.group(1);
                openProfile(context, userName);
                return true;
            }
            return false;
        }

        public static String getUserUrl(String username) {
            if (username == null) return null;
            return "http://instagram.com/" + (username.startsWith("@") ? username.substring(1) : username);
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
         * Only works with standard "geo:" urls like the ones generated by this class.
         * Starts by checking if the system can open a geo: url, otherwise it'll fallback to a Google Maps url.
         *
         * @return True if the url was opened; False if the url doesn't start with "geo:".
         */
        public static boolean tryOpenUrl(Context context, @NotNull String url) {
            if (url == null) return false;
            return url.startsWith("geo:") && tryStartIntentForUrl(context, getIntent(context, url));
        }

        /**
         * Tries to open a previously generated coordinates URL.
         * Only works with standard "geo:" urls like the ones generated by this class.
         * Starts by checking if the system can open a geo: url, otherwise it'll fallback to a Google Maps url.
         *
         * @return True if the url was opened; False if the url doesn't start with "geo:".
         */
        public static boolean tryOpenCoordinates(Context context, String name, Location location) {
            return tryStartIntentForUrl(context, getIntent(context, urlForCoordinates(name, location)));
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
     * Checks for apps that handle the URL in the following order: Facebook, Twitter, Instagram, Maps, anything else.
     * @return True if the URL was handled and something opened; False if the URL could not be handled.
     */
    public static boolean tryAll(Context context, @Nullable String url) {
        return url != null && (Facebook.tryOpenUrl(context, url) || Twitter.tryOpenProfileFromURL(context, url) ||
                               Instagram.tryOpenProfileFromURL(context, url) || Coordinates.tryOpenUrl(context, url) ||
                               UrlUtils.tryStartIntentForUrl(context, url));
    }

    private static Pattern unfurlPattern;

    protected static Pattern getUnfurlPattern() {
        if (unfurlPattern == null) {
            unfurlPattern = Pattern.compile("(https?://)?(www.)?(goo\\.gl|bit\\.ly|tiny\\.cc)/.*");
        }
        return unfurlPattern;
    }


    public static boolean canUnfurl(String url) {
        return !StringUtils.isEmpty(url) && getUnfurlPattern().matcher(url).matches();
    }

    public static String tryUnfurl(String url) {
        return canUnfurl(url) ? unfurl(url) : url;
    }

    public static String unfurl(String urlStr) {
        try {
            URL possiblyShortenedURL = new URL(urlStr);
            Log.v("Unfurling " + possiblyShortenedURL.toString());
            HttpURLConnection con = (HttpURLConnection) possiblyShortenedURL.openConnection();
            con.setInstanceFollowRedirects(false);
            final int responseCode = con.getResponseCode();
            Log.v("responseCode:" + responseCode);
            if (responseCode == 301) {
                // yep, shortened
                final String location = con.getHeaderField("Location");
                CFApp.getUserPreferences().edit().putString("URL-" + urlStr, location).apply();
                con.disconnect();
                return location;
            } else {
                Log.v(con.getHeaderField("Location"));
                con.disconnect();
                return urlStr;
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
        return urlStr;
    }


    public static String unfurl(String urlStr, boolean allowCache) {
        return allowCache
               ? defaultString(CFApp.getUserPreferences().getString("URL-" + urlStr, null), unfurl(urlStr))
               : unfurl(urlStr);
    }

    public static Task<String> unfurlTask(final String urlStr, boolean allowCache) {
        if (allowCache) {
            final String string = CFApp.getUserPreferences().getString("URL-" + urlStr, null);
            if (string != null) {
                return Task.forResult(string);
            }
        }
        return Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return unfurl(urlStr);
            }
        });
    }

    public static void clickableLinksOnTextView(TextView textView, @Nullable String text) {
        if (text == null) {
            textView.setText(null);
        } else {
            text = linkifyText(text);
            textView.setText(Html.fromHtml(text));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static String linkifyText(String text) {
        text = emailMatcher.matcher(text).replaceAll("<a href='mailto:$1'>$1</a>");
        text = httpMatcher.matcher(text).replaceAll("<a href='$1'>$1</a>");
        text = wwwMatcher.matcher(text).replaceAll("<a href='http://$1'>$1</a>");
        text = phoneMatcher.matcher(text).replaceAll("<a href='tel://$1'>$1</a>");
        text = text.replaceAll("\\n", "<br/>");
        return text;
    }

    public static Intent sendEmail(@Nullable String to, @Nullable String subject, @Nullable String content) {
        Intent emailIntent;
        if (to != null) {
            emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", to, null));
        } else {
            emailIntent = new Intent(Intent.ACTION_SENDTO);
        }
        if (StringUtils.isNotBlank(subject)) emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (StringUtils.isNotBlank(content)) emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        return Intent.createChooser(emailIntent, "Send email...");
    }
}
