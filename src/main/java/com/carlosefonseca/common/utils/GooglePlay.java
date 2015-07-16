package com.carlosefonseca.common.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.carlosefonseca.common.CFApp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public final class GooglePlay {

    private static final String TAG = CodeUtils.getTag(GooglePlay.class);

    private GooglePlay() {}

    public static boolean isAvailable(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        return (ConnectionResult.SUCCESS == resultCode);
    }

    public static void displayGooglePlayInstallerDialog(FragmentActivity context) {
        checkGooglePlay(context);
    }

    public static void displayGooglePlayInstallerDialog(Activity context) {
        checkGooglePlay(context);
    }


    private static boolean checkGooglePlay(FragmentActivity context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS != resultCode) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, context, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(context.getSupportFragmentManager(), "BusCore");
            }
        }
        return ConnectionResult.SUCCESS == resultCode;
    }

    private static boolean checkGooglePlay(Activity context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS != resultCode) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, context, 0);
            if (dialog != null) {
                dialog.show();
            }
        }
        return ConnectionResult.SUCCESS == resultCode;
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    private static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    /*
     *       ____ _                 _    __  __                           _
     *      / ___| | ___  _   _  __| |  |  \/  | ___  ___ ___  __ _  __ _(_)_ __   __ _
     *     | |   | |/ _ \| | | |/ _` |  | |\/| |/ _ \/ __/ __|/ _` |/ _` | | '_ \ / _` |
     *     | |___| | (_) | |_| | (_| |  | |  | |  __/\__ \__ \ (_| | (_| | | | | | (_| |
     *      \____|_|\___/ \__,_|\__,_|  |_|  |_|\___||___/___/\__,_|\__, |_|_| |_|\__, |
     *                                                              |___/         |___/
     */

    /**
     * Allows register for simple push notifications.
     *
     * Example usage to register the ID of this device:
     * <pre>
     * {@code
GooglePlay.CloudMessaging.registerCloudMessaging(this,
                                                       Config.GCM_PROJECT_NUMBER,
                                                       new GooglePlay.CloudMessaging.CloudMessagingIdListener() {
                                                           @Override
                                                           public void onCloudMessagingIdListener(String id) {
                                                               ClientForYourServer.sendPushNotificationId(id);
                                                           }
                                                       });
     * }
     * </pre>
     */
    public static final class CloudMessaging {
        public static String TAG = CodeUtils.getTag(CloudMessaging.class);

        public static final String PROPERTY_REG_ID = "registration_id";
        private static final String PROPERTY_APP_VERSION = "appVersion";

        private CloudMessaging() {}

        public interface CloudMessagingIdListener {
            void onCloudMessagingIdListener(String id);
        }

        public static void registerCloudMessaging(Activity context, String senderId, CloudMessagingIdListener listener) {
            if (!checkGooglePlay(context)) {
                Log.w(TAG, "Google Play Services failed. Not registering GCM");
                return;
            }
            String id = getRegistrationId();

            //noinspection SizeReplaceableByIsEmpty
            if (StringUtils.isEmpty(id)) {
                registerInBackground(listener, senderId);
            } else {
                boolean consoleLogging = Log.isConsoleLogging();
                Log.setConsoleLogging(true);
                Log.i(TAG, "Device registered, registration ID=" + id);
                Log.setConsoleLogging(consoleLogging);
                listener.onCloudMessagingIdListener(id);
            }
        }


        /**
         * Gets the current registration ID for application on GCM service.
         * <p/>
         * If result is empty, the app needs to register.
         *
         * @return registration ID, or empty string if there is no existing
         * registration ID.
         */
        private static String getRegistrationId() {
            final SharedPreferences prefs = getGCMPreferences();
            String registrationId = prefs.getString(PROPERTY_REG_ID, "");
            if (StringUtils.isEmpty(registrationId)) {
                Log.i(TAG, "Registration not found.");
                return "";
            }
            // Check if app was updated; if so, it must clear the registration ID
            // since the existing regID is not guaranteed to work with the new
            // app version.
            int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
            int currentVersion = CodeUtils.getAppVersionCode();
            if (registeredVersion != currentVersion) {
                Log.i(TAG, "App version changed.");
                return "";
            }
            return registrationId;
        }

        /**
         * Stores the registration ID and app versionCode in the application's
         * {@code SharedPreferences}.
         *
         * @param regId registration ID
         */
        private static void storeRegistrationId(String regId) {
            final SharedPreferences prefs = getGCMPreferences();
            int appVersion = CodeUtils.getAppVersionCode();
            Log.i(TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.apply();
        }

        private static SharedPreferences getGCMPreferences() {
            return CFApp.getUserPreferences("GCM");
        }

        /**
         * Registers the application with GCM servers asynchronously.
         * <p/>
         * Stores the registration ID and app versionCode in the application's
         * shared preferences.
         */
        private static void registerInBackground(final CloudMessagingIdListener listener, final String senderId) {
            new AsyncTask<Void, Void, Void>() {
                @Nullable
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        InstanceID instanceID = InstanceID.getInstance(CFApp.getContext());
                        String id = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                        // Old way
                        // GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(CFApp.getContext());
                        // String id = gcm.register(senderId);

                        boolean consoleLogging = Log.isConsoleLogging();
                        Log.setConsoleLogging(true);
                        Log.i(TAG, "Device registered, registration ID=" + id);
                        Log.setConsoleLogging(consoleLogging);

                        // You should send the registration ID to your server over HTTP,
                        // so it can use GCM/HTTP or CCS to send messages to your app.
                        // The request to your server should be authenticated if your app
                        // is using accounts.
                        if (listener != null) listener.onCloudMessagingIdListener(id);

                        // Persist the regID - no need to register again.
                        storeRegistrationId(id);
                    } catch (IOException ex) {
                        Log.w(TAG, "Error :" + ex.getMessage());
                        // If there is an error, don't just keep trying to register.
                        // Require the user to click a button again, or perform
                        // exponential back-off.
                    }
                    return null;
                }
            }.execute();
        }
    }
}

