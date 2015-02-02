/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.dialog.ClearRegistrationDialogFragment;
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
        notificationManager.cancel(PushService.NOTIFICATION_ID);
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
            case io.pivotal.android.push.sample.R.id.action_register:
                register();
                break;

            case io.pivotal.android.push.sample.R.id.action_unregister:
                unregister();
                break;

            case io.pivotal.android.push.sample.R.id.action_clear_registration:
                clearRegistration();
                break;

            case io.pivotal.android.push.sample.R.id.action_send_message:
                sender.sendMessage();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void register() {
        updateLogRowColour();
        addLogMessage(R.string.starting_registration);

        try {
            // TODO - find a way to let the user supply tags
            push.startRegistration(Preferences.getDeviceAlias(this), null, new RegistrationListener() {

                @Override
                public void onRegistrationComplete() {
                    queueLogMessage(R.string.registration_successful);
                }

                @Override
                public void onRegistrationFailed(String reason) {
                    queueLogMessage(getString(R.string.registration_failed) + reason);
                }
            });
        } catch (Exception e) {
            queueLogMessage(e.getLocalizedMessage());
        }
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
                    }
                    editor.commit();
                }
            }
        };
        final ClearRegistrationDialogFragment dialog = new ClearRegistrationDialogFragment();
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }
}
