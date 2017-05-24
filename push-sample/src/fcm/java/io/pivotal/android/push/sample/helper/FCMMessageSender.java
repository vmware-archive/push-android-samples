/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.helper;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.adapter.MessageLogger;
import io.pivotal.android.push.sample.dialog.SendMessageDialogFragment;
import io.pivotal.android.push.sample.model.FcmDownstreamMessageRequest;
import io.pivotal.android.push.sample.util.Preferences;
import io.pivotal.android.push.util.DebugUtil;

public class FCMMessageSender extends MessageSender {

    private static final String FCM_SEND_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

    public FCMMessageSender(FragmentActivity context, MessageLogger logger) {
        super(context, logger);
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
                switch (result) {
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
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return null;
            }
        };
        asyncTask.execute((Void) null);
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
}