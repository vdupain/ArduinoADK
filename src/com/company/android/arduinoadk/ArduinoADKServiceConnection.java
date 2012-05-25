package com.company.android.arduinoadk;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Used to bind the service.
 * 
 */
public class ArduinoADKServiceConnection implements ServiceConnection {

	private IBinder binder;
	private final ServiceConnected serviceConnected;

	public ArduinoADKServiceConnection(ServiceConnected serviceConnected) {
		this.serviceConnected = serviceConnected;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		binder = service;
		serviceConnected.onConnected(binder);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		binder = null;
	}

	/**
	 * Should be used only after 'onConnected' was called.
	 */
	public IBinder getBinder() {
		return binder;
	}

}
