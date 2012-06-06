package com.company.android.arduinoadk.test;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.company.android.arduinoadk.ArduinoADK;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService.RemoteControlServerBinder;

public class RemoteControlServerServiceTests extends
		ServiceTestCase<RemoteControlServerService> {

	public RemoteControlServerServiceTests() {
		super(RemoteControlServerService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	
	@Override
	protected void setupService() {
		ArduinoADK application = new ArduinoADK();
		//application.onCreate();
		this.setApplication(application);
		super.setupService();
	}

	@SmallTest
	public void testPreconditions() {
	}

	@SmallTest
	public void testStartable() {
		Intent startIntent = new Intent();
		startIntent.setClass(getContext(), RemoteControlServerService.class);
		startService(startIntent);
	}

	@MediumTest
	public void testBindable() {
		Intent startIntent = new Intent();
		startIntent.setClass(getContext(), RemoteControlServerService.class);
		IBinder service = bindService(startIntent);
		assertTrue(service instanceof RemoteControlServerBinder);		
	}

}
