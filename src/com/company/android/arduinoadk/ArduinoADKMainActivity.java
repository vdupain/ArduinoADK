package com.company.android.arduinoadk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ArduinoADKMainActivity extends Activity {
	static final String TAG = "ArduinoADKMainActivity";

	public static Intent createIntent(Activity activity) {
		return new Intent(activity, ArduinoADKActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = createIntent(this);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Unable to start ArduinoADK activity", e);
		}
		finish();
	}
}
