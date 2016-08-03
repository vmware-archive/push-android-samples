/*
 * Copyright (C) 2014 - 2016 Pivotal Software, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
