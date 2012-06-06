package com.company.android.arduinoadk.test;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.company.android.arduinoadk.ArduinoADK;

public class ArduinoADKApplicationTests extends ApplicationTestCase<ArduinoADK> {

	public ArduinoADKApplicationTests() {
		super(ArduinoADK.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@SmallTest
	public void testPreconditions() {
	}

	@MediumTest
	public void testSimpleCreate() {
		createApplication();
	}

}
