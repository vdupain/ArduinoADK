package com.company.android.arduinoadk.test;

import android.test.ActivityInstrumentationTestCase2;

import com.company.android.arduinoadk.ArduinoADKActivity;

public class ArduinoADKActivityTest extends ActivityInstrumentationTestCase2<ArduinoADKActivity> {

	private ArduinoADKActivity mActivity;

	public ArduinoADKActivityTest() {
		super("com.company.android.arduinoadk", ArduinoADKActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);

	    mActivity = getActivity();
	}
	
	
}
