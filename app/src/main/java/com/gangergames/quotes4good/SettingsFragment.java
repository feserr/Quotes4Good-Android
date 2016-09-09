package com.gangergames.quotes4good;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

/**
 * Copyright by Elias Serrano [2016].
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    /// Time between notification
    private static String _notificationTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }

        _notificationTime = "60";
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferences(findPreference(key));

        if (key.equals(getString(R.string.pref_time_notification_key)))
            _notificationTime = sharedPreferences.getString(key, "55");
    }

    /**
     * Initialize the summary of the preference
     *
     * @param p Preference item.
     */
    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreferences(p);
        }
    }

    /**
     * Update the summary of the given preference.
     *
     * @param p Preference item.
     */
    private void updatePreferences(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText() + " " +
                    getResources().getString(R.string.second_name));
        }
    }

    /**
     * Ask for the time between notifications of the settings menu.
     *
     * @return Time between notifications
     */
    public static int getNotificationTime() {
        if (_notificationTime == null)
            return 60;
        else
            return Integer.parseInt(_notificationTime);
    }

}