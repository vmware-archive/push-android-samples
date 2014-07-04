package io.pivotal.android.push.sample.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.activity.MainActivity;
import io.pivotal.android.push.service.GcmService;
import io.pivotal.android.push.util.Logger;

public class PushService extends GcmService {

    public static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_LIGHTS_COLOUR = 0xff008981;
    private static final int NOTIFICATION_LIGHTS_ON_MS = 500;
    private static final int NOTIFICATION_LIGHTS_OFF_MS = 1000;

    @Override
    public void onReceiveMessage(Bundle payload) {
        String message;
        if (payload.containsKey("message")) {
            message = "Received: \"" + payload.getString("message") + "\".";
        } else {
            message = "Received message with no extras.";
        }
        Logger.i(message);
        sendNotification(message);
    }

    @Override
    public void onReceiveMessageDeleted(Bundle payload) {
        Logger.i("Received message with type 'MESSAGE_TYPE_DELETED'.");
        sendNotification("Deleted messages on server: " + payload.toString());
    }

    @Override
    public void onReceiveMessageSendError(Bundle payload) {
        Logger.i("Received message with type 'MESSAGE_TYPE_SEND_ERROR'.");
        sendNotification("Send error: " + payload.toString());
    }

    private void sendNotification(String msg) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setLights(NOTIFICATION_LIGHTS_COLOUR, NOTIFICATION_LIGHTS_ON_MS, NOTIFICATION_LIGHTS_OFF_MS)
            .setSmallIcon(R.drawable.ic_pivotal_logo)
            .setContentTitle("Push Sample")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
            .setContentIntent(contentIntent)
            .setContentText(msg);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
