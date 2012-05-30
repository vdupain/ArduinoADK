package com.company.android.arduinoadk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.arduino.ArduinoMessage;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;
import com.company.android.arduinoadk.usb.UsbAccessoryService;
import com.company.android.arduinoadk.usb.UsbAccessoryService.UsbAccessoryBinder;

public class ArduinoActivity extends BaseActivity implements ServiceConnected {
	private static final String TAG = ArduinoActivity.class.getSimpleName();

	private ArduinoController arduinoController;

	/** Defines callbacks for service binding, passed to bindService() */
	private ArduinoADKServiceConnection usbServiceConnection = new ArduinoADKServiceConnection(
			this);

	private UsbAccessoryManager usbAccessoryManager;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case ARDUINO:
				logArduinoConsole("" + msg.obj);
				break;
			case TELEMETRY:
				handleTelemetryMessage((ArduinoMessage) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arduino_main);
		initControllers();
		createServices();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		doBindServices();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		arduinoController.usbAccessoryDetached();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		doUnbindServices();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	private void createServices() {
		Log.d(TAG, "createServices");
		startService(new Intent(this, UsbAccessoryService.class));
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		boolean success = bindService(new Intent(this,
				UsbAccessoryService.class), usbServiceConnection,
				Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to UsbAccessoryService");
		}
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		if (isBoundToUsbAccessoryManager()) {
			unbindService(usbServiceConnection);
			this.usbAccessoryManager = null;
		}
	}

	private void stopServices() {
		stopService(new Intent(this, UsbAccessoryService.class));
	}

	private boolean isBoundToUsbAccessoryManager() {
		return this.usbAccessoryManager != null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (usbAccessoryManager != null) {
			return usbAccessoryManager;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	public void logArduinoConsole(String message) {
		arduinoController.logConsole(message);
	}

	@Override
	void initControllers() {
		arduinoController = new ArduinoController(this);
		arduinoController.usbAccessoryAttached();
	}

	private void handleTelemetryMessage(ArduinoMessage message) {
		arduinoController.setRadarPosition(message.getDegree(),
				message.getDistance());
	}

	@Override
	public void onConnected(IBinder binder) {
		Log.d(TAG, "onConnected");
		if (binder == null) {
			Log.e(TAG, "Failed to get binder");
			return;
		}

		// switch between the different services
		if (binder instanceof UsbAccessoryBinder) {
			usbAccessoryManager = ((UsbAccessoryBinder) binder)
					.getUsbAccessoryManager();
			if (usbAccessoryManager.isOpened()) {
				ArduinoManager arduinoHandlerThread = new ArduinoManager(
						usbAccessoryManager, handler);
				Thread thread = new Thread(null, arduinoHandlerThread,
						"arduinoHandlerThread");
				thread.start();
			}
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

	@Override
	void onQuit() {
	}
}