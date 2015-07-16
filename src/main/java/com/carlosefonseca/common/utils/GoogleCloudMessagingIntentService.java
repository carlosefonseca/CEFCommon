package com.carlosefonseca.common.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.gcm.GcmListenerService;

public abstract class GoogleCloudMessagingIntentService extends GcmListenerService {
    private static final String TAG = CodeUtils.getTag(GoogleCloudMessagingIntentService.class);
    public static final int NOTIFICATION_ID = 1;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        sendNotification(message);
    }
    // [END receive_message]

    protected abstract void sendNotification(String msg);

    /**
     * Displays a basic notification.
     *
     * @param msg            The message on the notification.
     * @param activityToOpen The activity that will open when the user touches the notification.
     * @param appName        The title of the notification (usually the app name).
     * @param iconRes        The iconRes to display (e.g. R.drawable.app_icon).
     */
    protected void sendBasicNotification(String msg, Class activityToOpen, String appName, int iconRes) {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, activityToOpen), 0);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle().bigText(msg);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(iconRes)
                                                                                  .setContentTitle(appName)
                                                                                  .setStyle(style)
                                                                                  .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
