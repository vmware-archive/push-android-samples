/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.helper;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.adapter.MessageLogger;
import io.pivotal.android.push.sample.dialog.SelectTagsDialogFragment;
import io.pivotal.android.push.sample.dialog.SendMessageDialogFragment;
import io.pivotal.android.push.sample.model.FcmDownstreamMessageRequest;
import io.pivotal.android.push.sample.model.PCFPushMessageCustom;
import io.pivotal.android.push.sample.model.PCFPushMessageRequest;
import io.pivotal.android.push.sample.util.Preferences;
import io.pivotal.android.push.util.DebugUtil;

public class MessageSender {

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String DEVICE_UUID = "device_uuid";
    private static final String FCM_SEND_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String PCF_PUSH_SEND_MESSAGE_URL = "v1/push";

    private final FragmentActivity context;
    private final MessageLogger logger;

    public MessageSender(final FragmentActivity context, final MessageLogger logger) {
        this.logger = logger;
        this.context = context;
    }

    public void sendMessage() {
        if (!DebugUtil.getInstance(context).isDebuggable()) {
            Toast.makeText(context, R.string.release_build_error, Toast.LENGTH_LONG).show();
            return;
        }
        final File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG).show();
            return;
        }
        final SendMessageDialogFragment.Listener listener = new SendMessageDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                switch(result) {
                    case SendMessageDialogFragment.VIA_FCM:
                        sendMessageViaFcm();
                        break;
                    case SendMessageDialogFragment.VIA_PCF_PUSH:
                        sendMessageViaPCFPush(null);
                        break;
                    case SendMessageDialogFragment.VIA_PCF_PUSH_TAGS:
                        sendMessageViaPCFPushAndTags();
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

    private void sendMessageViaPCFPushAndTags() {
        final SelectTagsDialogFragment.Listener listener = new SelectTagsDialogFragment.Listener() {

            @Override
            public void onClickResult(int result, Set<String> selectedTags) {
                if (result == SelectTagsDialogFragment.OK) {
                    sendMessageViaPCFPush(selectedTags);
                }
            }
        };

        final SelectTagsDialogFragment dialog = new SelectTagsDialogFragment();
        dialog.setPositiveButtonLabelResourceId(R.string.send);
        dialog.setListener(listener);
        dialog.show(context.getSupportFragmentManager(), "SelectTagsDialogFragment");
    }

    private void sendMessageViaPCFPush(Set<String> tags) {
        logger.updateLogRowColour();
        final String data = getPCFPushMessageRequestString(tags);
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
                    final URL url = new URL(Preferences.getPcfPushServerUrl(context) + "/" + PCF_PUSH_SEND_MESSAGE_URL);
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

    private String getPCFPushMessageRequestString(Set<String> tags) {
        final String device_uuid = readIdFromFile(DEVICE_UUID);
        if (device_uuid == null) {
            return null;
        }
        final String[] devices = new String[]{device_uuid};
        final String messageBody = context.getString(R.string.pcf_message) + logger.getLogTimestamp();
        final PCFPushMessageRequest messageRequest;
        if (tags != null) {
            final String[] tagsArray = tags.toArray(new String[tags.size()]);
            messageRequest = new PCFPushMessageRequest(messageBody, null, tagsArray, null);
        } else {
            messageRequest = new PCFPushMessageRequest(messageBody, devices, null, null);
        }
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private String getPCFPushHeartbeatRequestString() {
        final String device_uuid = readIdFromFile(DEVICE_UUID);
        if (device_uuid == null) {
            return null;
        }
        final String messageBody = context.getString(R.string.pcf_message) + logger.getLogTimestamp();
        final Map<String, String> androidExtras = new HashMap<>();
        androidExtras.put("pcf.push.heartbeat.sentAt", Long.toString(System.currentTimeMillis()));
        final PCFPushMessageCustom custom = new PCFPushMessageCustom(androidExtras);
        final PCFPushMessageRequest messageRequest = new PCFPushMessageRequest(messageBody, null, new String[] {"pcf.push.heartbeat"}, custom);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private void sendMessageViaFcm() {
        logger.updateLogRowColour();
        final String data = getFcmMessageRequestString();
        if (data == null) {
            logger.addLogMessage(R.string.need_to_be_registered_error);
            return;
        }
        logger.addLogMessage(R.string.fcm_sending_message);
        logger.addLogMessage(context.getString(R.string.message_body_data) + data);

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                OutputStream outputStream = null;
                try {
                    final URL url = new URL(FCM_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.addRequestProperty(AUTHORIZATION, "key=" + Preferences.getFcmBrowserApiKey(context));
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.queueLogMessage(context.getString(R.string.fcm_message_request_accepted) + statusCode);
                    } else {
                        logger.queueLogMessage(context.getString(R.string.fcm_message_send_message_error) + statusCode);
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {
                    logger.queueLogMessage(context.getString(R.string.fcm_parse_error) + e.getLocalizedMessage());
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

    private String getFcmMessageRequestString() {
        final String regId = FirebaseInstanceId.getInstance().getToken();
        if (regId == null) {
            return null;
        }
        final String[] devices = new String[]{regId};
        final String message = context.getString(R.string.fcm_message) + logger.getLogTimestamp();
        final FcmDownstreamMessageRequest messageRequest = new FcmDownstreamMessageRequest(devices, message);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private String readIdFromFile(String idType) {
        final File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            logger.addLogMessage(R.string.external_files_dir_error);
            return null;
        }
        final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
        final File regIdFile = new File(dir, idType + ".txt");
        if (!regIdFile.exists() || !regIdFile.canRead()) {
            logger.addLogMessage(context.getString(R.string.read_file_error, idType, regIdFile.getAbsoluteFile()));
            return null;
        }
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(regIdFile);
            br = new BufferedReader(fr);
            return br.readLine();

        } catch (Exception e) {
            logger.addLogMessage(context.getString(R.string.error_reading_file, idType, e.getLocalizedMessage()));
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
        }
    }

}