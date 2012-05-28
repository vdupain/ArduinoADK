package com.company.android.arduinoadk;

import android.os.IBinder;

/**
 * Used together with {@link ArduinoADKServiceConnection}.
 */
public interface ServiceConnected {

	/**
	 * Called when the connection to the service is established.
	 * 
	 * @param binder
	 *            The binder from the service.
	 */
	public void onConnected(IBinder binder);

	/**
	 * Called when the connection with the service disconnects unexpectedly.
	 * 
	 */
	public void onDisconnected();
}
