package io.pivotal.android.push.sample.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.Map;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.activity.MainActivity;

public class ServiceHelper {

    private static final String GEOFENCE_ID = "PCF_GEOFENCE_ID";

    private static final int NOTIFICATION_LIGHTS_COLOUR = 0xff008981;
    private static final int NOTIFICATION_LIGHTS_ON_MS = 500;
    private static final int NOTIFICATION_LIGHTS_OFF_MS = 1000;
    public static final String NOTIFICATION_ACTION = "io.pivotal.android.push.sample.MainActivityNotification";

    public static String getTag(final Map<String, String> payload) {
        final String tag;
        if (payload.containsKey(GEOFENCE_ID)) {
            tag = payload.get(GEOFENCE_ID);
        } else {
            tag = "TAG";
        }
        return tag;
    }

    public static void sendNotification(final Context context, final String tag, final String msg, final Bundle payload, final int notificationId) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(NOTIFICATION_ACTION);
        intent.putExtras(payload);

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setLights(NOTIFICATION_LIGHTS_COLOUR, NOTIFICATION_LIGHTS_ON_MS, NOTIFICATION_LIGHTS_OFF_MS)
                .setSmallIcon(R.drawable.ic_pivotal_logo_2)
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(contentIntent)
                .setContentText(msg);

        notificationManager.notify(tag, notificationId, builder.build());
    }
}
