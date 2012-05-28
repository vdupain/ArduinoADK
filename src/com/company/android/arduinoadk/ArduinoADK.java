package com.company.android.arduinoadk;

import android.app.Application;

import com.company.android.arduinoadk.settings.Settings;

public class ArduinoADK extends Application {
	private Settings settings;

	@Override
	public void onCreate() {
		super.onCreate();
		settings = new Settings(this);

	}

	public Settings getSettings() {
		return settings;
	}
}
