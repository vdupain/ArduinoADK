package com.company.android.arduinoadk.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Settings {

	private final SharedPreferences preferences;

	public Settings(Context context) {
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isRCServerAutoStart() {
		return this.preferences.getBoolean("auto_start_rcserver", false);
	}

	public void setRCServerAutoStart(boolean flag) {
		Editor editor = preferences.edit();
		editor.putBoolean("auto_start_rcserver", flag);
		editor.commit();
	}

	public int getRCServerTCPPort() {
		return Integer.valueOf(this.preferences.getString("tcp_port", "12345"));
	}

	public String getRCServer() {
		return this.preferences.getString("rc_server", "");
	}

	public boolean isPreventGoingToSleep() {
		return this.preferences.getBoolean("prevent_going_to_sleep", true);
	}
}
