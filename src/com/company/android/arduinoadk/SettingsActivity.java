package com.company.android.arduinoadk;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_headers, false);
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	/**
	 * This fragment shows the general preferences.
	 */
	public static class GlobalSettingsPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager.setDefaultValues(getActivity(), R.xml.global_preferences, false);
			addPreferencesFromResource(R.xml.global_preferences);
		}
	}

	/**
	 * This fragment shows the RC preferences.
	 */
	public static class RemoteControlSettingsPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager.setDefaultValues(getActivity(), R.xml.remotecontrol_preferences, false);
			addPreferencesFromResource(R.xml.remotecontrol_preferences);
		}
	}

	/**
	 * This fragment shows the Arduino preferences.
	 */
	public static class ArduinoSettingsPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager.setDefaultValues(getActivity(), R.xml.arduino_preferences, false);
			addPreferencesFromResource(R.xml.arduino_preferences);
		}
	}
}
