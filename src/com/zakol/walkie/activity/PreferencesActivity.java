package com.zakol.walkie.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity
{
	// Uruchomienie widoku ustawień
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	// Załadowanie widoku z szablonu
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
    }
}