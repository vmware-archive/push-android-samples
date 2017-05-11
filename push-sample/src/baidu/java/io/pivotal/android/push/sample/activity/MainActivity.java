/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import io.pivotal.android.push.Push;
import io.pivotal.android.push.PushServiceInfo;
import io.pivotal.android.push.prefs.Pivotal.SslCertValidationMode;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.SubscribeToTagsListener;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.dialog.ClearRegistrationDialogFragment;
import io.pivotal.android.push.sample.dialog.SelectTagsDialogFragment;
import io.pivotal.android.push.sample.helper.MessageSender;
import io.pivotal.android.push.sample.util.Preferences;
import java.util.Set;

public class MainActivity extends LoggingActivity {

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

        push.setBaiduAPIKey(Preferences.getBaiduApiKey(getApplicationContext()));
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
        menuInflater.inflate(R.menu.main, menu);
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

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void register() {
        updateLogRowColour();
        addLogMessage(R.string.starting_registration);

        unregister(new UnregistrationComplete() {
            @Override
            public void onComplete() {
                queueLogMessage("Unregistration complete. Continuing with Registration");
                startRegistration();
            }
        });
    }

    private void startRegistration() {
        final Set<String> subscribedTags = Preferences.getSubscribedTags(this);
        final String deviceAlias = Preferences.getDeviceAlias(this);

        queueLogMessage("subscribedTags:" + subscribedTags + " deviceAlias:" + deviceAlias);

        final PushServiceInfo pushServiceInfo = PushServiceInfo.Builder()
            .setServiceUrl(Preferences.getPcfPushServerUrl(this))
            .setPlatformUuid(Preferences.getPcfPushPlatformUuid(this))
            .setPlatformSecret(Preferences.getPcfPushPlatformSecret(this))
            .setSSLCertValidationMode(SslCertValidationMode.TRUST_ALL)
            .build();

        push.setPushServiceInfo(pushServiceInfo);

        push.startRegistration(deviceAlias, subscribedTags, new RegistrationListener() {
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

    private void unregister(final UnregistrationComplete handler) {
        updateLogRowColour();
        addLogMessage(R.string.starting_unregistration);
        try {
            push.startUnregistration(new UnregistrationListener() {
                @Override
                public void onUnregistrationComplete() {
                    queueLogMessage(R.string.unregistration_successful);

                    if (handler != null) {
                        handler.onComplete();
                    }
                }

                @Override
                public void onUnregistrationFailed(String reason) {
                    queueLogMessage(getString(R.string.unregistration_failed) + reason);

                    if (handler != null) {
                        handler.onComplete();
                    }
                }
            });
        } catch (Exception e) {
            if (handler != null) {
                handler.onComplete();
            }
        }
    }

    private void unregister() {
        unregister(null);
    }


     private void clearRegistration() {
        final ClearRegistrationDialogFragment.Listener listener = new ClearRegistrationDialogFragment.Listener() {

            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
                    final SharedPreferences.Editor editor = getSharedPreferences("PivotalCFMSPush", Context.MODE_PRIVATE).edit();
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_PCF_PUSH) {
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

    private interface UnregistrationComplete {
        void onComplete();
    }
}
