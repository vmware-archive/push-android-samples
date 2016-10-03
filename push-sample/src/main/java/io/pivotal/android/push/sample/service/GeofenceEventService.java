package io.pivotal.android.push.sample.service;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.service.GeofenceService;
import io.pivotal.android.push.util.Logger;

import static io.pivotal.android.push.sample.service.ServiceHelper.getTag;
import static io.pivotal.android.push.sample.service.ServiceHelper.sendNotification;


public class GeofenceEventService extends GeofenceService {

    private static final int MESSAGE_NOTIFICATION_ID = 1;
    public static final String GEOFENCE_ENTER_BROADCAST = "io.pivotal.android.push.sample.GEOFENCE_ENTER";
    public static final String GEOFENCE_EXIT_BROADCAST = "io.pivotal.android.push.sample.GEOFENCE_EXIT";

    @Override
    public void onGeofenceEnter(final Bundle payload) {
        HashMap<String, String> payloadMap = bundleToMap(payload);
        Logger.i(getString(R.string.received_geofence_enter));
        sendNotification(this, getTag(payloadMap), getString(R.string.geofence_entered, payload.getString("message")), payload, MESSAGE_NOTIFICATION_ID);

        final Intent intent = new Intent(GEOFENCE_ENTER_BROADCAST);
        intent.replaceExtras(payload);
        sendBroadcast(intent);
    }

    @Override
    public void onGeofenceExit(final Bundle payload) {
        HashMap<String, String> payloadMap = bundleToMap(payload);
        Logger.i(getString(R.string.received_geofence_exit));
        sendNotification(this, getTag(payloadMap), getString(R.string.geofence_exited, payload.getString("message")), payload, MESSAGE_NOTIFICATION_ID);

        final Intent intent = new Intent(GEOFENCE_EXIT_BROADCAST);
        intent.replaceExtras(payload);
        sendBroadcast(intent);
    }

    private HashMap<String, String> bundleToMap(final Bundle bundle) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : bundle.keySet()) {
            map.put(key, bundle.getString(key));
        }

        return map;
    }
}
