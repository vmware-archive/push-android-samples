/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Set;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.PushPlatformInfo;
import io.pivotal.android.push.registration.RegistrationListener;

public class MainActivity extends AppCompatActivity {

    // Set to your own defined alias for this device.  May not be null.  May be empty.
    private static final String DEVICE_ALIAS = "push-device-alias";

    // Set to the list of tags you'd like to subscribe to.  May be empty or null.
    private static final Set<String> TAGS = null;

    // Set areGeofencesEnabled. Default is true.
    private static final boolean ARE_GEOFENCES_ENABLED = true;

    // Request code when requesting permission to use geofences.
    private static final int REQUEST_PERMISSION_FOR_GEOFENCES_RESPONSE_CODE = 27;

    private BroadcastReceiver messageBroadcastReceiver = null;
    private boolean isRegistering;
    private PushPlatformInfo platformInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        requestPushPlatformInformation();
    }

    private void requestPushPlatformInformation() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.title_push_platform_dlg));
        final View view = alertDialog.getLayoutInflater().inflate(R.layout.platform_info, null);
        alertDialog.setView(view);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String serverUrl = ((EditText)view.findViewById(R.id.server_url)).getText().toString();
                final String platformUuid = ((EditText)view.findViewById(R.id.platform_uuid)).getText().toString();
                final String platformSecret = ((EditText)view.findViewById(R.id.platform_secret)).getText().toString();
                platformInfo = new PushPlatformInfo(serverUrl, platformUuid, platformSecret);

                requestPermissionForGeofences();
            }
        });
        alertDialog.show();
    }

    private void requestPermissionForGeofences() {

        if (ARE_GEOFENCES_ENABLED) {

            // If you want to use geofences and are targetting Android Marshmallow or greater, then you must specifically
            // ask the user for permission to read the device location.  The following Dialog class is used to explain
            // to the user why your app is requesting permission to read the device location.

            final Dialog dialog = new AlertDialog.Builder(this)
                    .setMessage("The Push Demo App needs permission to read the device location in order to send you notifications when you enter certain locations.")
                    .setPositiveButton("OK", null)
                    .create();

            final boolean werePermissionsAlreadyGranted = Push.getInstance(this).requestPermissions(this, REQUEST_PERMISSION_FOR_GEOFENCES_RESPONSE_CODE, dialog);

            if (werePermissionsAlreadyGranted) {

                // If Push.requestPermissions returns true then ACCESS_FINE_LOCATION permission has already been granted
                // and we can immediately begin push registration.

                startPushRegistrationWithGeofencesEnabled(true);
            }

        } else {
            startPushRegistrationWithGeofencesEnabled(false);
        }
    }

    private void startPushRegistrationWithGeofencesEnabled(boolean areGeofencesEnabled) {

        printMessage("Device Alias: " + DEVICE_ALIAS);
        printMessage("Tags: " + TAGS);
        printMessage("Geofences enabled: " + areGeofencesEnabled);
        printMessage("Registering for notifications...");

        Push push = Push.getInstance(this);

        push.setPlatformInfo(platformInfo);
        push.startRegistration(DEVICE_ALIAS, TAGS, areGeofencesEnabled, new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                printMessage("Registration successful.");
                printMessage("FCM TokenId:" + FirebaseInstanceId.getInstance().getToken());
                Log.i("push-demo", "FCM TokenId:" + FirebaseInstanceId.getInstance().getToken());
            }

            @Override
            public void onRegistrationFailed(String reason) {
                printMessage("Registration failed. Reason is '" + reason + "'.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // This callback is invoked by Android after the user decides to allow or deny permission for ACCESS_FINE_LOCATION.
        // If Push.requestPermissions returns false then you need to wait for this callback before attempting
        // to register for pushes.

        if (requestCode == REQUEST_PERMISSION_FOR_GEOFENCES_RESPONSE_CODE && permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPushRegistrationWithGeofencesEnabled(true);
            } else {
                startPushRegistrationWithGeofencesEnabled(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        messageBroadcastReceiver = getBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(PushService.ACTION_KEY);
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
                    printMessage(message);
                }
            }
        };
    }

    private void printMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView label = (TextView) findViewById(R.id.label);
                label.setText(label.getText() + "\n" + message + "\n");
            }
        });
    }
}
