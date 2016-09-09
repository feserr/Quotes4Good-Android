package com.gangergames.quotes4good;

import android.app.Activity;
import android.os.Bundle;

/**
 * Copyright by Elias Serrano [2016].
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}