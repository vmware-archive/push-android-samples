/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.helper;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.adapter.MessageLogger;
import io.pivotal.android.push.sample.dialog.SelectTagsDialogFragment;
import io.pivotal.android.push.sample.dialog.SendMessageDialogFragment;
import io.pivotal.android.push.sample.model.GcmMessageRequest;
import io.pivotal.android.push.sample.model.PCFPushMessageCustom;
import io.pivotal.android.push.sample.model.PCFPushMessageRequest;
import io.pivotal.android.push.sample.util.Preferences;
import io.pivotal.android.push.sample.util.StringUtil;
import io.pivotal.android.push.util.Logger;

public class MessageSender {

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String GCM_SEND_MESSAGE_URL = "https://android.googleapis.com/gcm/send";
    private static final String PCF_PUSH_SEND_MESSAGE_URL = "v1/push";

    private final FragmentActivity context;
    private final MessageLogger logger;

    public MessageSender(final FragmentActivity context, final MessageLogger logger) {
        this.logger = logger;
        this.context = context;
    }

    public void sendMessage() {
        final SendMessageDialogFragment.Listener listener = new SendMessageDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                switch(result) {
                    case SendMessageDialogFragment.VIA_GCM:
                        sendMessageViaGcm();
                        break;
                    case SendMessageDialogFragment.VIA_PCF_PUSH:
                        sendMessageViaPCFPush(null, null);
                        break;
                    case SendMessageDialogFragment.VIA_PCF_PUSH_TAGS:
                        selectTags();
                        break;
                    case SendMessageDialogFragment.VIA_PCF_PUSH_CUSTOM_USER_ID:
                        selectCustomUserId();
                        break;
                    case SendMessageDialogFragment.HEARTBEAT:
                        sendHeartbeat();
                    default:
                }
            }
        };
        final SendMessageDialogFragment dialog = new SendMessageDialogFragment();
        dialog.setListener(listener);
        dialog.show(context.getSupportFragmentManager(), "SendMessageDialogFragment");
    }

    private void selectTags() {
        final SelectTagsDialogFragment.Listener listener = new SelectTagsDialogFragment.Listener() {

            @Override
            public void onClickResult(int result, Set<String> selectedTags) {
                if (result == SelectTagsDialogFragment.OK) {
                    sendMessageViaPCFPush(selectedTags, null);
                }
            }
        };

        final SelectTagsDialogFragment dialog = new SelectTagsDialogFragment();
        dialog.setTitleResourceId(R.string.send_to_tags_title);
        dialog.setPositiveButtonLabelResourceId(R.string.send);
        dialog.setListener(listener);
        dialog.show(context.getSupportFragmentManager(), "SelectTagsDialogFragment");
    }

    private void selectCustomUserId() {
        final LayoutInflater inflater = context.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_custom_user_id, null);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final EditText editText = (EditText)dialogView.findViewById(R.id.input);
        editText.setText(Preferences.getCustomUserId(context));
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(R.string.push_to_custom_user_id);
        dialogBuilder.setPositiveButton(R.string.push, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String customUserId = editText.getText().toString().trim();
                if (customUserId.isEmpty()) {
                    Toast.makeText(context, R.string.custom_user_id_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                Logger.fd(context.getString(R.string.push_to_custom_user_id_log), customUserId);

                final String[] tokens = customUserId.split(",");
                final Set<String> customUserIds = new HashSet<>();
                for (final String s: tokens) {
                    customUserIds.add(s.trim());
                }

                sendMessageViaPCFPush(null, customUserIds);
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        dialogBuilder.create().show();
    }

    private void sendMessageViaPCFPush(Set<String> tags, Set<String> customUserIds) {
        logger.updateLogRowColour();
        final String data = getPCFPushMessageRequestString(tags, customUserIds);
        if (data == null) {
            logger.addLogMessage(R.string.need_to_be_registered_error);
            return;
        }
        logger.addLogMessage(R.string.pcf_sending_message);
        logger.addLogMessage(context.getString(R.string.message_body_data) + data);

        postMessageToPCF(data);
    }

    public void sendHeartbeat() {
        logger.updateLogRowColour();
        final String data = getPCFPushHeartbeatRequestString();

        if (data == null) {
            logger.addLogMessage(R.string.need_to_be_registered_error);
            return;
        }
        logger.addLogMessage(R.string.pcf_sending_heartbeat_message);
        logger.addLogMessage(context.getString(R.string.message_body_data) + data);

        postMessageToPCF(data);
    }

    private void postMessageToPCF(final String data) {

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                OutputStream outputStream = null;

                try {
                    final URL url = new URL(Pivotal.getServiceUrl(context) + "/" + PCF_PUSH_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.setDoOutput(true);
                    urlConnection.addRequestProperty(AUTHORIZATION, getBasicAuthorizationValue());
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.queueLogMessage(context.getString(R.string.pcf_message_request_accepted) + statusCode);
                    } else {
                        logger.queueLogMessage(context.getString(R.string.pcf_server_send_message_error) + statusCode);
                    }

                    urlConnection.disconnect();

                } catch (IOException e) {
                    logger.queueLogMessage(context.getString(R.string.pcf_parse_error) + e.getLocalizedMessage());

                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
                return null;
            }
        };

        asyncTask.execute((Void) null);
    }

    private String getBasicAuthorizationValue() {
        final String appUuid = Preferences.getPCFPushAppUuid(context);
        final String apiKey = Preferences.getPCFPushApiKey(context);
        final String stringToEncode = appUuid + ":" + apiKey;
        return "Basic  " + Base64.encodeToString(stringToEncode.getBytes(), Base64.NO_WRAP);
    }

    private void writeConnectionOutput(String requestBodyData, OutputStream outputStream) throws IOException {
        final byte[] bytes = requestBodyData.getBytes();
        for (byte b : bytes) {
            outputStream.write(b);
        }
        outputStream.close();
    }

    private String getPCFPushMessageRequestString(Set<String> tags, Set<String> customUserIds) {

        final PCFPushMessageRequest messageRequest;

        if (tags != null) {
            messageRequest = targetByTag(tags);

        } else if (customUserIds != null) {
           messageRequest = targetByCustomUserIds(customUserIds);

        } else {
            messageRequest = targetByDeviceUuid();
        }

        if (messageRequest == null) {
            return null;
        }

        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private PCFPushMessageRequest targetByTag(Set<String> tags) {
        final String[] tagsArray = tags.toArray(new String[tags.size()]);
        final String messageBody = context.getString(R.string.pcf_message_by_tags, StringUtil.join(tags, ", "), logger.getLogTimestamp());
        return new PCFPushMessageRequest(messageBody, null, tagsArray, null, null);
    }

    private PCFPushMessageRequest targetByCustomUserIds(Set<String> customUserIds) {
        final String[] customUserIdsArray = customUserIds.toArray(new String[customUserIds.size()]);
        final String messageBody = context.getString(R.string.pcf_message_by_custom_user_ids, StringUtil.join(customUserIds, ","), logger.getLogTimestamp());
        return new PCFPushMessageRequest(messageBody, null, null, customUserIdsArray, null);
    }

    private PCFPushMessageRequest targetByDeviceUuid() {
        final String deviceUuid = Push.getInstance(context).getDeviceUuid();
        if (deviceUuid == null) {
            return null;
        }

        final String[] devices = new String[] {deviceUuid};
        final String messageBody = context.getString(R.string.pcf_message_by_device_uuid, logger.getLogTimestamp());
        return new PCFPushMessageRequest(messageBody, devices, null, null, null);
    }

    private String getPCFPushHeartbeatRequestString() {
        final String device_uuid = Push.getInstance(context).getDeviceUuid();
        if (device_uuid == null) {
            return null;
        }
        final String messageBody = context.getString(R.string.pcf_hearbeat_message, logger.getLogTimestamp());
        final Map<String, String> androidExtras = new HashMap<>();
        androidExtras.put("pcf.push.heartbeat.sentAt", Long.toString(System.currentTimeMillis()));
        final PCFPushMessageCustom custom = new PCFPushMessageCustom(androidExtras);
        final PCFPushMessageRequest messageRequest = new PCFPushMessageRequest(messageBody, null, new String[] {"pcf.push.heartbeat"}, null, custom);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private void sendMessageViaGcm() {
        logger.updateLogRowColour();
        final String data = getGcmMessageRequestString();
        if (data == null) {
            logger.addLogMessage(R.string.need_to_be_registered_error);
            return;
        }
        logger.addLogMessage(R.string.gcm_sending_message);
        logger.addLogMessage(context.getString(R.string.message_body_data) + data);

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                OutputStream outputStream = null;
                try {
                    final URL url = new URL(GCM_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.addRequestProperty(AUTHORIZATION, "key=" + Preferences.getGcmBrowserApiKey(context));
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.queueLogMessage(context.getString(R.string.gcm_message_request_accepted) + statusCode);
                    } else {
                        logger.queueLogMessage(context.getString(R.string.gcm_message_send_message_error) + statusCode);
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {
                    logger.queueLogMessage(context.getString(R.string.gcm_parse_error) + e.getLocalizedMessage());
                }

                finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
                return null;
            }
        };
        asyncTask.execute((Void) null);
    }

    private HttpURLConnection getUrlConnection(URL url) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod(POST);
        urlConnection.setConnectTimeout(60000);
        urlConnection.setReadTimeout(60000);
        urlConnection.addRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        return urlConnection;
    }

    private String getGcmMessageRequestString() {
        final String regId = Push.getInstance(context).getDeviceUuid();
        if (regId == null) {
            return null;
        }
        final String[] devices = new String[]{regId};
        final String message = context.getString(R.string.gcm_message) + logger.getLogTimestamp();
        final GcmMessageRequest messageRequest = new GcmMessageRequest(devices, message);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }
}