package com.carlosefonseca.common.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.carlosefonseca.common.CFApp;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedDeclaration")
public final class AppUpdater {
    private static final String TAG = CodeUtils.getTag(AppUpdater.class);

    @Nullable
    private static String managerUrl = null;
    private static String appName;
    private static String channel;

    private static String existsNewVersion;

    private static LatestVersionJSON latestVersion;
    private static Map<String, LatestVersionJSON> latestVersions;

    private AppUpdater() {}

    static String currentChannelUrl() {
        if (managerUrl == null) throw new RuntimeException("URL must be set!");
        return managerUrl + "/" + appName + "/" + channel;
    }

    public static String urlForLatest() {
        if (managerUrl == null) throw new RuntimeException("URL must be set!");
        return managerUrl + "/" + appName + "/releases/latest";
    }


    public static void checkForUpdates(final CheckForUpdatesDelegate delegate) {
        checkForUpdates(currentChannelUrl(), delegate);
    }

    private static void checkForUpdates(final String url, final CheckForUpdatesDelegate delegate) {
        assert appName != null;
        assert channel != null;
        if (!NetworkingUtils.hasInternet()) return;
        final DownloadURL.DownloadResult aaa = new DownloadURL.DownloadResult() {
            @Override
            public void onString(String string) {
                if (string == null) return;

                try {
                    latestVersion = new Gson().fromJson(string, LatestVersionJSON.class);

                    Log.v(TAG, "Current Version: " + CodeUtils.getAppVersionName() + " Latest Version " + latestVersion.version);

                    existsNewVersion = latestVersion.version;

                    if (CodeUtils.getAppVersionCode() != latestVersion.getVersionCode()) {
                        delegate.newUpdateExists(existsNewVersion);
                    }
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "" + e.getMessage() + " - " + string, e);
                }
            }
        };
        new DownloadURL(null, false, aaa).execute(url);
    }

    public static void getServerVersions(final CheckForUpdatesDelegate delegate) {
        if (!NetworkingUtils.hasInternet()) return;
        DownloadURL.DownloadResult aaa = new DownloadURL.DownloadResult() {
            @Override
            public void onString(String string) {
                if (string == null) return;

                latestVersions = new Gson().fromJson(string, new TypeToken<Map<String, LatestVersionJSON>>() {}.getType());

                String list = "";

                for (Map.Entry<String, LatestVersionJSON> version : latestVersions.entrySet()) {
                    list += StringUtils.capitalize(version.getKey()) + ": " + version.getValue().version + "\n";
                }

                Log.v(TAG, "Server Versions: " + list.replace("\n", " ; "));

                delegate.newUpdateExists(list.trim());
            }
        };
        new DownloadURL(null, false, aaa).execute(urlForLatest());
    }

    public static void updateApp(final Context activity) {
        if (latestVersion == null) {
            if (latestVersions == null) {
                Toast.makeText(activity, "Error :(", Toast.LENGTH_SHORT).show();
                Log.w(TAG, new RuntimeException("No known update checked"));
                return;
            } else if (latestVersions.containsKey(channel)) {
                latestVersion = latestVersions.get(channel);
            } else {
                Toast.makeText(activity, "Error :(", Toast.LENGTH_SHORT).show();
                Log.w(TAG, new RuntimeException("No known update checked."));
                return;
            }
        }

        ProgressDialog mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle("Downloading update…");
        if (StringUtils.isNotBlank(latestVersion.release_notes)) {
            mProgressDialog.setMessage("Release notes:\n\n" + latestVersion.release_notes);
        } else {
            mProgressDialog.setMessage("Please wait…");
        }
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        DownloadURL.DownloadResult aaa = new DownloadURL.DownloadResult() {
            @Override
            public void onFail() {
                Toast.makeText(CFApp.getContext(),
                               "Download failed :( Check your internet connection and try again…",
                               Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Download Failed ");
            }

            @Override
            public void onFile(String path) {
                //noinspection SizeReplaceableByIsEmpty
                if (path != null && path.length() > 0) {
                    if (new File(path).exists()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                        activity.startActivity(intent);
                    } else {
                        Toast.makeText(CFApp.getContext(), "Ups! Something wrong with the update process...", Toast.LENGTH_SHORT)
                             .show();
                        Log.e(TAG, new Exception("Update failed, file doesn't exist!"));
                    }
                }
            }
        };

        final DownloadURL downloadURL = new DownloadURL(mProgressDialog, true, aaa);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                downloadURL.cancel(true);
            }
        });

        downloadURL.execute(latestVersion.url);
    }

    public static void configureButton(final Button button) {
        button.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUpdater.updateApp(button.getContext());
            }
        });
        checkForUpdates(new CheckForUpdatesDelegate() {
            @Override
            public void newUpdateExists(String version) {
                button.setVisibility(View.VISIBLE);
                button.setText("Update to "+version);
            }
        });
    }

    public static void setup(String url, String appname) {
        managerUrl = url;
        AppUpdater.appName = appname;
    }

    public interface CheckForUpdatesDelegate {
        public void newUpdateExists(String version);
    }

    public static String getAppName() {
        return appName;
    }

    public static void setAppName(String appName) {
        AppUpdater.appName = appName;
    }

    public static String getChannel() {
        return channel;
    }

    public static void setChannel(String channel) {
        if (AppUpdater.channel != null && !AppUpdater.channel.equals(channel)) latestVersion = null;
        AppUpdater.channel = channel;
    }

    static class LatestVersionJSON {
        String url;
        String version;
        String release_notes;

        transient int versionCode = -1;
        transient String versionName;
        transient static final Pattern versionSplit = Pattern.compile("(\\S+) \\((\\d+)\\)");

        String getVersionName() {
            splitVersions();
            return versionName;
        }

        int getVersionCode() {
            splitVersions();
            return versionCode;
        }

        private void splitVersions() {
            if (versionCode == -1) {
                Matcher matcher = versionSplit.matcher(version);
                if (matcher.matches()) {
                    versionName = matcher.group(1);
                    versionCode = Integer.parseInt(matcher.group(2));
                }
            }
        }
    }
}
