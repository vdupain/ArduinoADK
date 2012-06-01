package com.company.android.arduinoadk.remotecontrol;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * This service is only used to contain the RemoteControlServer running in the
 * background. To use it, you need to bind to this service and get it from the
 * binder.
 */
public class RemoteControlService extends Service {

	private static final String TAG = RemoteControlService.class.getSimpleName();

	// Binder given to clients
	private final IBinder binder = new RemoteControlBinder();

	private RemoteControlManager remoteControlManager;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class RemoteControlBinder extends Binder {
		public RemoteControlService getService() {
			return RemoteControlService.this;
		}

		public RemoteControlManager getRemoteControlManager() {
			return RemoteControlService.this.remoteControlManager;
		}
	}

	@Override
	public void onCreate() {
		// The service is being created
		Log.d(TAG, "onCreate");
		if (remoteControlManager == null) {
			remoteControlManager = new RemoteControlManager(this.getApplicationContext());
			remoteControlManager.onCreate();
		}
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(TAG, "onDestroy");
		remoteControlManager.stopServer();
		remoteControlManager.stopClient();
		remoteControlManager = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "onStartCommand startId " + startId + ": intent " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		Log.d(TAG, "onBind");
		return this.binder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return true;
	}
}
