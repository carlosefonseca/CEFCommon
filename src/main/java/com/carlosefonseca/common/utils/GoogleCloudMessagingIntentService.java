package com.carlosefonseca.common.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public abstract class GoogleCloudMessagingIntentService extends IntentService {
    private static final String TAG = CodeUtils.getTag(GoogleCloudMessagingIntentService.class);
    public static final int NOTIFICATION_ID = 1;

    public GoogleCloudMessagingIntentService(String gcmIntentService) {
        super(gcmIntentService);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification("Send error: " + extras.toString());
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Deleted messages on server: " + extras.toString());
                    // If it's a regular GCM message, do some work.
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    // Post notification of received message.
                    sendNotification(extras.getString("alert"));
                    Log.i(TAG, "Received: " + extras.getString("alert"));
                    break;
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

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
