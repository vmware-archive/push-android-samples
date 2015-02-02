/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.util.Set;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.registration.RegistrationListener;

public class MainActivity extends ActionBarActivity {

    // Set to your own defined alias for this device.  May not be null.  May be empty.
    private static final String DEVICE_ALIAS = "push-device-alias";

    // Set to the list of tags you'd like to subscribe to.  May be empty or null.
    private static final Set<String> TAGS = null;

    private BroadcastReceiver messageBroadcastReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        queueLogMessage("DEVICE_ALIAS: " + DEVICE_ALIAS);
        queueLogMessage("TAGS: " + TAGS);
        queueLogMessage("Registering for notifications...");

        registerForPushNotifications();

        messageBroadcastReceiver = getBroadcastReceiver();
        IntentFilter intentFilter = getIntentFilter();
        registerReceiver(messageBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageBroadcastReceiver);
        messageBroadcastReceiver = null;
    }

    private BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String message = intent.getStringExtra(PushService.MESSAGE_KEY);
                    queueLogMessage(message);
                }
            }
        };
    }

    private IntentFilter getIntentFilter() {
        return new IntentFilter(PushService.ACTION_KEY);
    }

    private void registerForPushNotifications() {

        Push.getInstance(this).startRegistration(DEVICE_ALIAS, TAGS, new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                queueLogMessage("Registration successful.");
            }

            @Override
            public void onRegistrationFailed(String reason) {
                queueLogMessage("Registration failed. Reason is '" + reason + "'.");
            }
        });
    }

    private void queueLogMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView label = (TextView) findViewById(R.id.label);
                label.setText(label.getText() + "\n" + message + "\n");
            }
        });
    }
}
