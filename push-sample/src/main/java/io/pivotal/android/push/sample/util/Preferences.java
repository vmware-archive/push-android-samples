/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class Preferences {

    public static final String DEVICE_ALIAS = "test_device_alias";
    public static final String FCM_BROWSER_API_KEY = "test_fcm_browser_api_key";
    public static final String BAIDU_API_KEY = "test_baidu_api_key";
    public static final String PCF_PUSH_APP_UUID = "test_pcf_app_uuid";
    public static final String PCF_PUSH_SERVER_URL = "test_pcf_server_url";
    public static final String PCF_PUSH_PLATFORM_UUID = "test_pcf_platform_uuid";
    public static final String PCF_PUSH_PLATFORM_SECRET = "test_pcf_platform_secret";
    public static final String PCF_PUSH_API_KEY = "test_pcf_api_key";
    public static final String SUBSCRIBED_TAGS = "test_subscribed_tags";
    public static final String ARE_GEOFENCES_ENABLED = "test_are_geofences_enabled";
    public static final String HEARTBEAT_COUNT = "test_heartbeat_count";

    public static final String[] PREFERENCE_NAMES = {
            DEVICE_ALIAS,
            FCM_BROWSER_API_KEY,
            BAIDU_API_KEY,
            PCF_PUSH_APP_UUID,
            PCF_PUSH_SERVER_URL,
            PCF_PUSH_PLATFORM_UUID,
            PCF_PUSH_PLATFORM_SECRET,
            PCF_PUSH_API_KEY,
            ARE_GEOFENCES_ENABLED,
            HEARTBEAT_COUNT
    };

    public static String getDeviceAlias(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ALIAS, null);
    }

    public static String getFcmBrowserApiKey(Context context) {
        return getSharedPreferences(context).getString(FCM_BROWSER_API_KEY, null);
    }

    public static String getPCFPushAppUuid(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_APP_UUID, null);
    }

    public static String getPcfPushServerUrl(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_SERVER_URL, null);
    }

    public static String getPcfPushPlatformUuid(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_PLATFORM_UUID, null);
    }

    public static String getPcfPushPlatformSecret(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_PLATFORM_SECRET, null);
    }

    public static String getBaiduApiKey(Context context) {
        return getSharedPreferences(context).getString(BAIDU_API_KEY, null);
    }

    public static String getPCFPushApiKey(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_API_KEY, null);
    }

    public static boolean getAreGeofencesEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(ARE_GEOFENCES_ENABLED, false);
    }

    public static Set<String> getSubscribedTags(Context context) {
        return getSharedPreferences(context).getStringSet(SUBSCRIBED_TAGS, new HashSet<String>());
    }

    public static void setSubscribedTags(Context context, Set<String> tags) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet(SUBSCRIBED_TAGS, tags);
        editor.apply();
    }

    public static int incrementHeartbeatCount(Context context) {
        final int heartbeatCount = getSharedPreferences(context).getInt(HEARTBEAT_COUNT, 0) + 1;
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(HEARTBEAT_COUNT, heartbeatCount);
        editor.apply();
        return heartbeatCount;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
