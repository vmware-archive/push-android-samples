package io.pivotal.android.push.sample.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.util.Preferences;

public class PreferencesFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private Map<String, Preference> preferenceMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        addPreferences(getPreferenceNames());
        preferenceChangeListener = getPreferenceChangeListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        showCurrentPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    protected String[] getPreferenceNames() {
        return Preferences.PREFERENCE_NAMES;
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

    public void resetPreferencesToDefault() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.edit().clear().commit();
        PreferenceManager.setDefaultValues(this.getActivity(), getPreferencesXmlResourceId(), true);
        showCurrentPreferences();
    }

    protected int getPreferencesXmlResourceId() {
        return R.xml.preferences;
    }
}
