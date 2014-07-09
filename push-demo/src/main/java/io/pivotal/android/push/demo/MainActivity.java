/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.registration.RegistrationListener;

public class MainActivity extends ActionBarActivity {

    // Set to your "Project Number" on your Google Cloud project
    private static final String GCM_SENDER_ID = "961895792376";

    // Set to your "Variant UUID", as provided by the Pivotal Mobile Services Suite console
    private static final String VARIANT_UUID = "8e00641c-b264-4ae9-98db-dd6f2ca858b2";

    // Set to your "Variant Secret" as provided by the Pivotal Mobile Services Suite console
    private static final String VARIANT_SECRET = "0de0f14a-ab2c-4f47-aa44-a6cb06531c6c";

    // Set to your instance of the Pivotal Mobile Services Suite server providing your push services.
    private static final String PUSH_BASE_SERVER_URL = "http://ec2-54-87-125-154.compute-1.amazonaws.com";

    // Set to your own defined alias for this device.  May not be null.  May be empty.
    private static final String DEVICE_ALIAS = "push-demo-alias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queueLogMessage("Any push notifications you receive will appear on your device status bar.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        queueLogMessage("GCM_SENDER_ID: " + GCM_SENDER_ID);
        queueLogMessage("VARIANT_UUID: " + VARIANT_UUID);
        queueLogMessage("VARIANT_SECRET: " + VARIANT_SECRET);
        queueLogMessage("DEVICE_ALIAS: " + DEVICE_ALIAS);
        queueLogMessage("PUSH_BASE_SERVER_URL: " + PUSH_BASE_SERVER_URL);

        queueLogMessage("Registering for notifications...");

        registerForPushNotifications();
    }

    private void registerForPushNotifications() {
        final RegistrationParameters parameters = new RegistrationParameters(
            GCM_SENDER_ID, VARIANT_UUID, VARIANT_SECRET, DEVICE_ALIAS, PUSH_BASE_SERVER_URL
        );

        Push.getInstance(this).startRegistration(parameters, new RegistrationListener() {

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
