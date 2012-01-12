package com.zakol.walkie.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity
{
	// Uruchomienie widoku ustawieñ
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	// Za³adowanie widoku z szablonu
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
    }
}