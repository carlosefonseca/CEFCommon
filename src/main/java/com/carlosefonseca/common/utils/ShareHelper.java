package com.carlosefonseca.common.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.carlosefonseca.common.CFActivity;

import java.io.File;

public final class ShareHelper {

    private static final java.lang.String TAG = CodeUtils.getTag(ShareHelper.class);
    private static final int SHARE_REQUEST_ID = 6921;

    private ShareHelper() {}

    public static void shareTextImage(CFActivity activity, @Nullable String fbAppId, String text, @Nullable final File path) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (fbAppId != null) sendIntent.putExtra("com.facebook.platform.extra.APPLICATION_ID", fbAppId);
        if (path != null) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(path));
            sendIntent.setType("image/*");
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            sendIntent.setType("text/plain");
        }
        activity.startActivityForResult(Intent.createChooser(sendIntent, "Share"), SHARE_REQUEST_ID);
    }
}
