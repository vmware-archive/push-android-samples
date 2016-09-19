/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.service;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.fcm.FcmMessagingService;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.util.Preferences;
import io.pivotal.android.push.util.Logger;

public class PushService extends FcmMessagingService {

    public static final String GEOFENCE_ENTER_BROADCAST = "io.pivotal.android.push.sample.GEOFENCE_ENTER";
    public static final String GEOFENCE_EXIT_BROADCAST = "io.pivotal.android.push.sample.GEOFENCE_EXIT";

    private static final int MESSAGE_NOTIFICATION_ID = 1;
    private static final int HEARTBEAT_NOTIFICATION_ID = 2;

    public static final String NOTIFICATION_ACTION = "io.pivotal.android.push.sample.MainActivityNotification";

    private static final HashMap<String, String> emptyPayload = new HashMap<>();

    @Override
    public void onMessageNotificationReceived(RemoteMessage.Notification notification, Map<String, String> data) {
        final String message;
        if (!notification.getBody().isEmpty()) {
            message = getString(R.string.received_message, notification.getBody());
        } else {
            message = getString(R.string.received_message_no_extras);
        }
        Logger.i(message);
        ServiceHelper.sendNotification(this, ServiceHelper.getTag(data), message, mapToBundle(data), MESSAGE_NOTIFICATION_ID);
    }

    @Override
    public void onReceiveHeartbeat(Map<String, String> heartbeat) {
        final int heartbeatCount = Preferences.incrementHeartbeatCount(this);
        Logger.i(getString(R.string.received_heartbeat_message, heartbeatCount));
        ServiceHelper.sendNotification(this, ServiceHelper.getTag(heartbeat), getString(R.string.received_heartbeat_message, heartbeatCount), mapToBundle(heartbeat), HEARTBEAT_NOTIFICATION_ID);
    }

    @Override
    public void onReceiveDeletedMessages() {
        Logger.i(getString(R.string.received_message_deleted));
        ServiceHelper.sendNotification(this, ServiceHelper.getTag(emptyPayload), getString(R.string.deleted_message_server), new Bundle(), MESSAGE_NOTIFICATION_ID);
    }

    @Override
    public void onReceiveMessageSendError(String messageId, Exception exception) {
        Logger.i(getString(R.string.received_message_send_error));
        ServiceHelper.sendNotification(this, ServiceHelper.getTag(emptyPayload), getString(R.string.send_error, messageId, exception.toString()), new Bundle(), MESSAGE_NOTIFICATION_ID);
    }

    private Bundle mapToBundle(Map<String, String> map) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return  bundle;
    }

}
