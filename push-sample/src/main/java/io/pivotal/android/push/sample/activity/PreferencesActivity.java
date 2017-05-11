/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.util.Preferences;

public class PreferencesActivity extends PreferenceActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private Map<String, Preference> preferenceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // NOTE - many of the method calls in this class show up as deprecated.  However, I still want my
        // app to run on old Android versions, so I'm going to leave them in here.
        addPreferencesFromResource(getPreferencesXmlResourceId());
        addPreferences(getPreferenceNames());
        preferenceChangeListener = getPreferenceChangeListener();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        final Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_preferences_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.inflateMenu(R.menu.preferences);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_reset_preferences) {
                    resetPreferencesToDefault();
                    return true;
                }
                return false;
            }
        });
    }

    protected String[] getPreferenceNames() {
        return Preferences.PREFERENCE_NAMES;
    }

    protected int getPreferencesXmlResourceId() {
        return R.xml.preferences;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        // Show the Up button in the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addPreferences(String[] preferenceNames) {
        preferenceMap = new HashMap<>();
        for (final String preferenceName: preferenceNames) {
            addPreference(preferenceName);
        }
    }

    private void addPreference(String preferenceName) {
        final Preference item = getPreferenceScreen().findPreference(preferenceName);
        preferenceMap.put(preferenceName, item);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                showCurrentPreferences();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrentPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void showCurrentPreferences() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        for (final String preferenceName : preferenceMap.keySet()) {
            final Preference item = preferenceMap.get(preferenceName);
            if (item instanceof EditTextPreference) {
                setupEditTextPreferenceField((EditTextPreference) item, prefs.getString(preferenceName, null));
            } else if (item instanceof CheckBoxPreference) {
                setupCheckBoxPreference((CheckBoxPreference) item, prefs.getBoolean(preferenceName, false));
            }
        }
    }

    protected void setupEditTextPreferenceField(EditTextPreference preference, String value) {
        preference.setText(value);
        preference.setSummary(value);
    }

    private void setupCheckBoxPreference(CheckBoxPreference preference, boolean value) {
        preference.setChecked(value);
    }

    @SuppressLint("CommitPrefEdits")
    private void resetPreferencesToDefault() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.edit().clear().commit();
        PreferenceManager.setDefaultValues(this, getPreferencesXmlResourceId(), true);
        showCurrentPreferences();
    }
}
