/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.demo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.pivotal.android.push.fcm.FcmMessagingService;
import io.pivotal.android.push.util.Logger;

public class PushService extends FcmMessagingService {

    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_KEY = "io.pivotal.android.push.demo.PUSH_RECEIVED";
    public static final String MESSAGE_KEY = "message";

    private static final int NOTIFICATION_LIGHTS_COLOUR = 0xff008981;
    private static final int NOTIFICATION_LIGHTS_ON_MS = 500;
    private static final int NOTIFICATION_LIGHTS_OFF_MS = 1000;

    @Override
    public void onMessageNotificationReceived(RemoteMessage.Notification notification, Map<String, String> data) {
        String message;
        if (!notification.getBody().isEmpty()) {
            message = "Received: \"" + notification.getBody() + "\".";
        } else {
            message = "Received message with no extras.";
        }
        Logger.i(message);
        showNotificationInApp(message);
        showNotificationOnStatusBar(message);
    }

    @Override
    public void onReceiveDeletedMessages() {
        Logger.i("Received deleted message.");
        showNotificationOnStatusBar("Deleted messages on server");
    }

    @Override
    public void onReceiveMessageSendError(String messageId, Exception exception) {
        Logger.i("Received send error message.");
        showNotificationOnStatusBar("Send error: [" + messageId + "]: "+ exception.toString());
    }

    private void showNotificationInApp(String message) {
        Intent intent = new Intent(ACTION_KEY);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    private void showNotificationOnStatusBar(String msg) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setLights(NOTIFICATION_LIGHTS_COLOUR, NOTIFICATION_LIGHTS_ON_MS, NOTIFICATION_LIGHTS_OFF_MS)
            .setSmallIcon(R.drawable.ic_pivotal_logo_2)
            .setContentTitle("Push Demo")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
            .setContentIntent(contentIntent)
            .setContentText(msg);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}
