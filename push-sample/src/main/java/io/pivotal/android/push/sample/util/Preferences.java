/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    public static final String GCM_SENDER_ID = "test_gcm_sender_id";
    public static final String VARIANT_UUID = "test_variant_uuid";
    public static final String VARIANT_SECRET = "test_variant_secret";
    public static final String DEVICE_ALIAS = "test_device_alias";
    public static final String GCM_BROWSER_API_KEY = "test_gcm_browser_api_key";
    public static final String PUSH_BASE_SERVER_URL = "test_push_base_server_url";
    public static final String BACK_END_ENVIRONMENT_UUID = "test_back_end_environment_uuid";
    public static final String BACK_END_ENVIRONMENT_KEY = "test_back_end_environment_key";

    public static final String[] PREFERENCE_NAMES = {
            GCM_SENDER_ID,
            VARIANT_UUID,
            VARIANT_SECRET,
            DEVICE_ALIAS,
            GCM_BROWSER_API_KEY,
            PUSH_BASE_SERVER_URL,
            BACK_END_ENVIRONMENT_UUID,
            BACK_END_ENVIRONMENT_KEY
    };

    public static String getGcmSenderId(Context context) {
        return getSharedPreferences(context).getString(GCM_SENDER_ID, null);
    }

    public static String getVariantUuid(Context context) {
        return getSharedPreferences(context).getString(VARIANT_UUID, null);
    }

    public static String getVariantSecret(Context context) {
        return getSharedPreferences(context).getString(VARIANT_SECRET, null);
    }

    public static String getDeviceAlias(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ALIAS, null);
    }

    public static String getGcmBrowserApiKey(Context context) {
        return getSharedPreferences(context).getString(GCM_BROWSER_API_KEY, null);
    }

    public static String getPushBaseServerUrl(Context context) {
        return getSharedPreferences(context).getString(PUSH_BASE_SERVER_URL, null);
    }

    public static String getBackEndEnvironmentUuid(Context context) {
        return getSharedPreferences(context).getString(BACK_END_ENVIRONMENT_UUID, null);
    }

    public static String getBackEndEnvironmentKey(Context context) {
        return getSharedPreferences(context).getString(BACK_END_ENVIRONMENT_KEY, null);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
