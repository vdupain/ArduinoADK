package com.company.android.arduinoadk.remotecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.company.android.arduinoadk.ArduinoADKActivity;
import com.company.android.arduinoadk.WhatAbout;

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

	private ArduinoADKActivity arduinoADKActivity;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case SERVER_LOG:
				arduinoADKActivity.logConsole("" + msg.obj);
				break;
			case SERVER_START:
				arduinoADKActivity.logConsole("RC Server started...");
				arduinoADKActivity.rcServerController.displayIP();
				break;
			case SERVER_STOP:
				arduinoADKActivity.logConsole("RC Server stopped...");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class RemoteControlBinder extends Binder {
		public RemoteControlService getService() {
			return RemoteControlService.this;
		}

		public RemoteControlManager getRCServerManager() {
			return RemoteControlService.this.remoteControlManager;
		}
	}

	@Override
	public void onCreate() {
		// The service is being created
		Log.d(TAG, "onCreate");
		if (remoteControlManager == null) {
			remoteControlManager = new RemoteControlManager(this.getApplicationContext(), handler);
		}
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(TAG, "onDestroy");
		remoteControlManager.stop();
		remoteControlManager = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "onStartCommand startId " + startId + ": intent " + intent);
		Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
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
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
		// return true;
	}

	public void setActivity(ArduinoADKActivity arduinoADKActivity) {
		this.arduinoADKActivity = arduinoADKActivity;
	}

}
