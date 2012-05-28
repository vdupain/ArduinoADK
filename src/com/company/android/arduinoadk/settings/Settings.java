package com.company.android.arduinoadk.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

	private final SharedPreferences preferences;

	public Settings(Context context) {
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isRCServerAutoStart() {
		return this.preferences.getBoolean("auto_start_rcserver", false);
	}

	public int getRCserverTCPPort() {
		return Integer.valueOf(this.preferences.getString("tcp_port", "12345"));
	}
}
