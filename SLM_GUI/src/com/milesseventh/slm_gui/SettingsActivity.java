package com.milesseventh.slm_gui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle _sI){
		super.onCreate(_sI);
		addPreferencesFromResource(R.xml.settings);
	}
	
}
