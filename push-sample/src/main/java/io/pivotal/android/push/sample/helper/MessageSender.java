package io.pivotal.android.push.sample.helper;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.pivotal.android.push.sample.adapter.MessageLogger;
import io.pivotal.android.push.sample.dialog.SendMessageDialogFragment;
import io.pivotal.android.push.sample.model.BackEndMessageRequest;
import io.pivotal.android.push.sample.model.GcmMessageRequest;
import io.pivotal.android.push.sample.util.Preferences;
import io.pivotal.android.push.util.DebugUtil;

public class MessageSender {

    private final FragmentActivity context;
    private final MessageLogger logger;

    public MessageSender(final FragmentActivity context, final MessageLogger logger) {
        this.logger = logger;
        this.context = context;
    }

    private static final String GCM_SEND_MESSAGE_URL = "https://android.googleapis.com/gcm/send";
    private static final String BACK_END_SEND_MESSAGE_URL = "v1/push";

    public void sendMessage() {
        if (!DebugUtil.getInstance(context).isDebuggable()) {
            Toast.makeText(context, "This feature does not work in release builds.", Toast.LENGTH_SHORT).show();
            return;
        }
        final File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Toast.makeText(context, "This feature requires the SD-card to be mounted.", Toast.LENGTH_SHORT).show();
            return;
        }
        final SendMessageDialogFragment.Listener listener = new SendMessageDialogFragment.Listener() {
            @Override
            public void onClickResult(int result) {
                if (result == SendMessageDialogFragment.VIA_GCM) {
                    sendMessageViaGcm();
                } else if (result == SendMessageDialogFragment.VIA_BACK_END) {
                    sendMessageViaBackEnd();
                }
            }
        };
        final SendMessageDialogFragment dialog = new SendMessageDialogFragment();
        dialog.setListener(listener);
        dialog.show(context.getSupportFragmentManager(), "SendMessageDialogFragment");
    }

    private void sendMessageViaBackEnd() {
        logger.updateLogRowColour();
        final String data = getBackEndMessageRequestString();
        if (data == null) {
            logger.addLogMessage("Can not send message. Please register first.");
            return;
        }
        logger.addLogMessage("Sending message via back-end server...");
        logger.addLogMessage("Message body data: \"" + data + "\"");

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                OutputStream outputStream = null;

                try {
                    final URL url = new URL(Preferences.getPushBaseServerUrl(context) + "/" + BACK_END_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.setDoOutput(true);
                    urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue());
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.queueLogMessage("Back-end server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        logger.queueLogMessage("Back-end server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }

                    urlConnection.disconnect();

                } catch (IOException e) {
                    logger.queueLogMessage("ERROR: got exception parsing network response from Back-end server: " + e.getLocalizedMessage());

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

        asyncTask.execute((Void)null);
    }

    private String getBasicAuthorizationValue() {
        final String environmentUuid = Preferences.getBackEndEnvironmentUuid(context);
        final String environmentKey = Preferences.getBackEndEnvironmentKey(context);
        final String stringToEncode = environmentUuid + ":" + environmentKey;
        return "Basic  " + Base64.encodeToString(stringToEncode.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
    }

    private void writeConnectionOutput(String requestBodyData, OutputStream outputStream) throws IOException {
        final byte[] bytes = requestBodyData.getBytes();
        for (byte b : bytes) {
            outputStream.write(b);
        }
        outputStream.close();
    }

    private String getBackEndMessageRequestString() {
        final String device_uuid = readIdFromFile("device_uuid");
        if (device_uuid == null) {
            return null;
        }
        final String[] devices = new String[]{device_uuid};
        final String platforms = "android";
        final String messageTitle = "Sample Message Title";
        final String messageBody = "This message was sent to the back-end at " + logger.getLogTimestamp() + "." ;
        final BackEndMessageRequest messageRequest = new BackEndMessageRequest(messageTitle, messageBody, platforms, devices);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private void sendMessageViaGcm() {
        logger.updateLogRowColour();
        final String data = getGcmMessageRequestString();
        if (data == null) {
            logger.addLogMessage("Can not send message. Please register first.");
            return;
        }
        logger.addLogMessage("Sending message via GCM...");
        logger.addLogMessage("Message body data: \"" + data + "\"");

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                OutputStream outputStream = null;
                try {
                    final URL url = new URL(GCM_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.addRequestProperty("Authorization", "key=" + Preferences.getGcmBrowserApiKey(context));
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.queueLogMessage("GCM server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        logger.queueLogMessage("GCM server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {
                    logger.queueLogMessage("ERROR: got exception posting message to GCM server: " + e.getLocalizedMessage());
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
        urlConnection.setRequestMethod("POST");
        urlConnection.setConnectTimeout(60000);
        urlConnection.setReadTimeout(60000);
        urlConnection.addRequestProperty("Content-Type", "application/json");
        return urlConnection;
    }

    private String getGcmMessageRequestString() {
        final String regId = readIdFromFile("gcm_registration_id");
        if (regId == null) {
            return null;
        }
        final String[] devices = new String[]{regId};
        final String message = "This message was sent to GCM at " + logger.getLogTimestamp() + ".";
        final GcmMessageRequest messageRequest = new GcmMessageRequest(devices, message);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private String readIdFromFile(String idType) {
        final File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            logger.addLogMessage("ERROR: Was not able to get the externalFilesDir");
            return null;
        }
        final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
        final File regIdFile = new File(dir, idType + ".txt");
        if (!regIdFile.exists() || !regIdFile.canRead()) {
            logger.addLogMessage("ERROR: " + idType + " file not found (" + regIdFile.getAbsoluteFile() + "). Have you registered with GCM and the back-end successfully? Are you running a debug build? Is the external cache directory accessible?");
            return null;
        }
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(regIdFile);
            br = new BufferedReader(fr);
            return br.readLine();

        } catch (Exception e) {
            logger.addLogMessage("ERROR reading " + idType + " file:" + e.getLocalizedMessage());
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