package com.company.android.arduinoadk;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import com.company.android.arduinoadk.settings.Settings;

public class ArduinoADK extends Application implements ActivityLifecycleCallbacks {
	private static final String TAG = ArduinoADK.class.getSimpleName();

	private Settings settings;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		settings = new Settings(this);
		registerActivityLifecycleCallbacks(this);
	}

	public Settings getSettings() {
		return settings;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.d(TAG, "onActivityStarted:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.d(TAG, "onActivityResumed:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.d(TAG, "onActivityPaused:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivityStopped(Activity activity) {
		Log.d(TAG, "onActivityStopped:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		Log.d(TAG, "onActivitySaveInstanceState:" + activity.getClass().getSimpleName());
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		Log.d(TAG, "onActivityDestroyed:" + activity.getClass().getSimpleName());
	}
}
