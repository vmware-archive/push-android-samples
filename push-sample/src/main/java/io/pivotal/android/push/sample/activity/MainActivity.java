/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.SubscribeToTagsListener;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.dialog.ClearRegistrationDialogFragment;
import io.pivotal.android.push.sample.dialog.SelectTagsDialogFragment;
import io.pivotal.android.push.sample.helper.MessageSender;
import io.pivotal.android.push.sample.service.PushService;
import io.pivotal.android.push.sample.util.Preferences;

public class MainActivity extends LoggingActivity {

    private static final String GCM_SENDER_ID = "gcm_sender_id";
    private static final String GCM_DEVICE_REGISTRATION_ID = "gcm_device_registration_id";
    private static final String APP_VERSION = "app_version";
    private static final String VARIANT_UUID = "variant_uuid";
    private static final String VARIANT_SECRET = "variant_secret";
    private static final String DEVICE_ALIAS = "device_alias";
    private static final String BACKEND_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    private static final String BASE_SERVER_URL = "base_server_url";
    private static final String GEOFENCE_UPDATE = "geofence_update";

    private static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 9;
    private static final int GEOFENCES_ACTIVITY_PERMISSION_REQUEST_CODE = 13;

    private Push push;
    private MessageSender sender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (logItems.isEmpty()) {
            addLogMessage(R.string.registration_instructions);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLogRowColour();
        clearNotifications();
        setup();

        final Intent i = getIntent();
        if (i.getAction().equals(PushService.NOTIFICATION_ACTION)) {
            Push.getInstance(this).logOpenedNotification(i.getExtras());
        }
    }

    protected Class<? extends PreferencesActivity> getPreferencesActivity() {
        return PreferencesActivity.class;
    }

    private void setup() {
        push = Push.getInstance(this);
        sender = new MessageSender(this, this);
    }

    private void clearNotifications() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(io.pivotal.android.push.sample.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_register:
                register();
                break;

            case R.id.action_subscribe_to_tags:
                subscribeToTags();
                break;

            case R.id.action_unregister:
                unregister();
                break;

            case R.id.action_clear_registration:
                clearRegistration();
                break;

            case R.id.action_send_message:
                sender.sendMessage();
                break;

            case R.id.action_geofence:
                requestPermissionForGeofencesActivity();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void register() {
        updateLogRowColour();
        addLogMessage(R.string.starting_registration);
        requestPermissionToAccessLocation();
    }

    private void requestPermissionToAccessLocation() {

        // Accessing the device location is only required if using geofences.

        try {
            final boolean areGeofencesEnabled = Preferences.getAreGeofencesEnabled(this);

            if (areGeofencesEnabled) {

                final Dialog dialog = new AlertDialog.Builder(this).
                        setMessage(R.string.geofence_permission).
                        setPositiveButton(R.string.ok, null).
                        create();

                if (push.requestPermissions(this, ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE, dialog)) {
                    // Permission is already granted.  Otherwise, requestPermissions will display
                    // the provided dialog box which will call onRequestPermissionsResults below (API >= 23)
                    startRegistrationWithGeofencesEnabled(true);
                }

            } else {
                startRegistrationWithGeofencesEnabled(false);
            }

        } catch (Exception e) {
            queueLogMessage(e.getLocalizedMessage());
        }
    }

    private void startRegistrationWithGeofencesEnabled(boolean areGeofencesEnabled) {
        final Set<String> subscribedTags = Preferences.getSubscribedTags(this);
        final String deviceAlias = Preferences.getDeviceAlias(this);

        addLogMessage("subscribedTags:" + subscribedTags + " deviceAlias:" + deviceAlias + " areGeofencesEnabled:" + areGeofencesEnabled);

        push.startRegistration(deviceAlias, subscribedTags, areGeofencesEnabled, new RegistrationListener() {
            @Override
            public void onRegistrationComplete() {
                queueLogMessage(getString(R.string.registration_successful) + " " + push.getDeviceUuid());
            }

            @Override
            public void onRegistrationFailed(String reason) {
                queueLogMessage(getString(R.string.registration_failed) + reason);
            }
        });
    }

    @Override
    // Only called on API level >= 23
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRegistrationWithGeofencesEnabled(true);
            } else {
                startRegistrationWithGeofencesEnabled(false);
            }

        } else if (requestCode == GEOFENCES_ACTIVITY_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGeofencesActivity();
            } else {
                Toast.makeText(this, R.string.maps_requires_location_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void subscribeToTags() {

        final SelectTagsDialogFragment.Listener listener = new SelectTagsDialogFragment.Listener() {

            @Override
            public void onClickResult(int result, Set<String> selectedTags) {
                if (result == SelectTagsDialogFragment.OK) {
                    updateLogRowColour();
                    addLogMessage(R.string.starting_subscribe_to_tags);

                    Preferences.setSubscribedTags(MainActivity.this, selectedTags);

                    push.subscribeToTags(selectedTags, new SubscribeToTagsListener() {
                        @Override
                        public void onSubscribeToTagsComplete() {
                            queueLogMessage(R.string.subscribe_to_tags_successful);
                        }

                        @Override
                        public void onSubscribeToTagsFailed(String reason) {
                            queueLogMessage(getString(R.string.subscribe_to_tags_failed) + reason);
                        }
                    });
                }
            }
        };

        final SelectTagsDialogFragment dialog = new SelectTagsDialogFragment();
        dialog.setPositiveButtonLabelResourceId(R.string.subscribe);
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "SelectTagsDialogFragment");
    }

    private void unregister() {
        updateLogRowColour();
        addLogMessage(R.string.starting_unregistration);

        push.startUnregistration(new UnregistrationListener() {
            @Override
            public void onUnregistrationComplete() {
                queueLogMessage(R.string.unregistration_successful);
            }

            @Override
            public void onUnregistrationFailed(String reason) {
                queueLogMessage(getString(R.string.unregistration_failed) + reason);
            }
        });
    }

    // This method cheats since it knows the names of the parameters saved by the SDK.
    private void clearRegistration() {
        final ClearRegistrationDialogFragment.Listener listener = new ClearRegistrationDialogFragment.Listener() {

            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
                    final SharedPreferences.Editor editor = getSharedPreferences(PushPreferencesProviderImpl.TAG_NAME, Context.MODE_PRIVATE).edit();
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_GCM || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage(R.string.clearing_gcm_device_registration);
                        editor.remove(GCM_SENDER_ID);
                        editor.remove(GCM_DEVICE_REGISTRATION_ID);
                        editor.remove(APP_VERSION);
                    }
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_PCF_PUSH || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage(R.string.clear_pcf_push_device_registration);
                        editor.remove(VARIANT_UUID);
                        editor.remove(VARIANT_SECRET);
                        editor.remove(DEVICE_ALIAS);
                        editor.remove(BACKEND_DEVICE_REGISTRATION_ID);
                        editor.remove(BASE_SERVER_URL);
                        editor.remove(GEOFENCE_UPDATE);
                    }
                    editor.commit();
                }
            }
        };
        final ClearRegistrationDialogFragment dialog = new ClearRegistrationDialogFragment();
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }

    private void requestPermissionForGeofencesActivity() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= 23) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    new AlertDialog.Builder(this)
                            .setMessage(R.string.geofence_permission)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            GEOFENCES_ACTIVITY_PERMISSION_REQUEST_CODE);
                                }
                            })
                            .create()
                            .show();

                } else {

                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, GEOFENCES_ACTIVITY_PERMISSION_REQUEST_CODE);
                }
            } else {
                Toast.makeText(this, R.string.maps_requires_location_permission, Toast.LENGTH_LONG).show();
            }

        } else {
            // Permission already granted
            startGeofencesActivity();
        }
    }

    private void startGeofencesActivity() {
        final Intent intent = new Intent(this, GeofenceActivity.class);
        startActivity(intent);
    }
}
