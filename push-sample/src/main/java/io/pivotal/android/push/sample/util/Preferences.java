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
    public static final String GCM_BROWSER_API_KEY = "test_gcm_browser_api_key";
    public static final String PCF_PUSH_APP_UUID = "test_pcf_app_uuid";
    public static final String PCF_PUSH_API_KEY = "test_pcf_api_key";
    public static final String SUBSCRIBED_TAGS = "test_subscribed_tags";

    public static final String[] PREFERENCE_NAMES = {
            DEVICE_ALIAS,
            GCM_BROWSER_API_KEY,
            PCF_PUSH_APP_UUID,
            PCF_PUSH_API_KEY
    };

    public static String getDeviceAlias(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ALIAS, null);
    }

    public static String getGcmBrowserApiKey(Context context) {
        return getSharedPreferences(context).getString(GCM_BROWSER_API_KEY, null);
    }

    public static String getPCFPushAppUuid(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_APP_UUID, null);
    }

    public static String getPCFPushApiKey(Context context) {
        return getSharedPreferences(context).getString(PCF_PUSH_API_KEY, null);
    }

    public static Set<String> getSubscribedTags(Context context) {
        return getSharedPreferences(context).getStringSet(SUBSCRIBED_TAGS, new HashSet<String>());
    }

    public static void setSubscribedTags(Context context, Set<String> tags) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet(SUBSCRIBED_TAGS, tags);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
