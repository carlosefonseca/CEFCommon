package com.carlosefonseca.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TwitterUtils {

    private static final java.lang.String TAG = CodeUtils.getTag(TwitterUtils.class);
    public static final String TWITTER_USER_NAME_URL = "https?://twitter\\.com/([^/]+)/?$";
    public static final Pattern TWITTER_USER_NAME_REGEX = Pattern.compile(TWITTER_USER_NAME_URL);


    private TwitterUtils() {}


    public static void openProfile(Context context, String userName) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + userName));
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (infos.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + userName));
        }
        context.startActivity(intent);
    }

    public static boolean openProfileFromURL(Context context, String url) {
        final Matcher matcher = TWITTER_USER_NAME_REGEX.matcher(url);
        if (matcher.matches()) {
            final String userName = matcher.group(1);
            openProfile(context, userName);
            return true;
        }
        return false;
    }
}
