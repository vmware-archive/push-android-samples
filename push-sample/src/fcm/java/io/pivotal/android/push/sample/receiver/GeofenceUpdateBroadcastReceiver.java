package io.pivotal.android.push.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import io.pivotal.android.push.Push;
import io.pivotal.android.push.version.GeofenceStatus;

public class GeofenceUpdateBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final GeofenceStatus status = Push.getInstance(context).getGeofenceStatus();
        if (status != null) {
            if (status.isError()) {
                Toast.makeText(context, status.getErrorReason(), Toast.LENGTH_LONG).show();
            }
            Toast.makeText(context, "Number of currently monitoring geofences: " + status.getNumberCurrentlyMonitoringGeofences(), Toast.LENGTH_LONG).show();
        }
    }
}
