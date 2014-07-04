/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package io.pivotal.android.push.sample.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.dialog.ClearRegistrationDialogFragment;
import io.pivotal.android.push.sample.helper.MessageSender;
import io.pivotal.android.push.sample.service.PushService;
import io.pivotal.android.push.sample.util.Preferences;

public class MainActivity extends LoggingActivity {

    private Push push;
    private MessageSender sender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration.");
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
        addLogMessage("Starting registration...");

        push.startRegistration(getRegistrationParameters(), new RegistrationListener() {

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

    private void unregister() {
        updateLogRowColour();
        addLogMessage("Starting unregistration...");

        push.startUnregistration(getRegistrationParameters(), new UnregistrationListener() {
            @Override
            public void onUnregistrationComplete() {
                queueLogMessage("Unregistration successful.");
            }

            @Override
            public void onUnregistrationFailed(String reason) {
                queueLogMessage("Unregistration failed. Reason is '" + reason + "'.");
            }
        });
    }

    private RegistrationParameters getRegistrationParameters() {
        final String gcmSenderId = Preferences.getGcmSenderId(this);
        final String variantUuid = Preferences.getVariantUuid(this);
        final String variantSecret = Preferences.getVariantSecret(this);
        final String deviceAlias = Preferences.getDeviceAlias(this);
        final String baseServerUrl = Preferences.getPushBaseServerUrl(this);
        addLogMessage("GCM Sender ID: '" + gcmSenderId + "'\nVariant UUID: '" + variantUuid + "\nVariant Secret: '" + variantSecret + "'\nDevice Alias: '" + deviceAlias + "'\nBase Server URL: '" + baseServerUrl + "'.");
        return new RegistrationParameters(gcmSenderId, variantUuid, variantSecret, deviceAlias, baseServerUrl);
    }

    private void clearRegistration() {
        final ClearRegistrationDialogFragment.Listener listener = new ClearRegistrationDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
                    final SharedPreferences.Editor editor = getSharedPreferences(PushPreferencesProviderImpl.TAG_NAME, Context.MODE_PRIVATE).edit();
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_GCM || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage("Clearing device registration from GCM");
                        editor.remove("gcm_sender_id");
                        editor.remove("gcm_device_registration_id");
                        editor.remove("app_version");
                    }
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BACK_END || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage("Clearing device registration from the back-end");
                        editor.remove("variant_uuid");
                        editor.remove("variant_secret");
                        editor.remove("device_alias");
                        editor.remove("backend_device_registration_id");
                        editor.remove("base_server_url");
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
