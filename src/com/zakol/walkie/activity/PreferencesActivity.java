package com.zakol.walkie.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity
{
	// Uruchomienie widoku ustawie�
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	// Za�adowanie widoku z szablonu
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
    }
}