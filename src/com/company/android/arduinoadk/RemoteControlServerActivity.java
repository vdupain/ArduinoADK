package com.company.android.arduinoadk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService.RemoteControlServerBinder;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;
import com.company.android.arduinoadk.usb.UsbAccessoryService;
import com.company.android.arduinoadk.usb.UsbAccessoryService.UsbAccessoryBinder;

public class RemoteControlServerActivity extends BaseActivity implements ServiceConnected, OnCheckedChangeListener {
	private static final String TAG = RemoteControlServerActivity.class.getSimpleName();

	private RemoteControlController controller;
	private Switch switchRCServer;

	/** Defines callbacks for service binding, passed to bindService() */
	private ArduinoADKServiceConnection usbServiceConnection = new ArduinoADKServiceConnection(this);
	private ArduinoADKServiceConnection remoteControlServiceConnection = new ArduinoADKServiceConnection(this);

	private UsbAccessoryManager usbAccessoryManager;
	private RemoteControlServerService remoteControlServerService;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case SERVER_LOG:
				logConsole("" + msg.obj);
				break;
			case SERVER_START:
				handleServerStart();
				break;
			case SERVER_STOP:
				handleServerStop();
				break;
			default:
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcserver_main);
		RemoteControlServerFragment fragment = (RemoteControlServerFragment) getFragmentManager().findFragmentById(R.id.remoteControlServerFragment);
		switchRCServer = (Switch) fragment.getView().findViewById(R.id.switchRCServer);
		switchRCServer.setOnCheckedChangeListener(this);
		initController();
		createServices();
	}

	private void handleServerStart() {
		logConsole("RC Server started...");
		controller.displayIP();
	}

	private void handleServerStop() {
		logConsole("RC Server stopped...");
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindServices();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		controller.usbAccessoryDetached();

	}

	@Override
	protected void onStop() {
		super.onStop();
		doUnbindServices();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void createServices() {
		Log.d(TAG, "createServices");
		startService(new Intent(this, UsbAccessoryService.class));
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		boolean success = bindService(new Intent(this, UsbAccessoryService.class), usbServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + UsbAccessoryService.class.getSimpleName());
		}
		success = bindService(new Intent(this, RemoteControlServerService.class), remoteControlServiceConnection, 0);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + RemoteControlServerService.class.getSimpleName());
		}
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		if (isBoundToRemoteControlServerService()) {
			unbindService(remoteControlServiceConnection);
			this.remoteControlServerService = null;
		}
		if (isBoundToUsbAccessoryManager()) {
			unbindService(usbServiceConnection);
			this.usbAccessoryManager = null;
		}
	}

	private void stopServices() {
		stopService(new Intent(this, RemoteControlServerService.class));
		stopService(new Intent(this, UsbAccessoryService.class));
	}

	private boolean isBoundToRemoteControlServerService() {
		return this.remoteControlServerService != null;
	}

	private boolean isBoundToUsbAccessoryManager() {
		return this.usbAccessoryManager != null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				startService(new Intent(this, RemoteControlServerService.class));
				boolean success = bindService(new Intent(this, RemoteControlServerService.class), remoteControlServiceConnection, Context.BIND_AUTO_CREATE);
				if (!success) {
					Log.e(TAG, "Failed to bind to " + RemoteControlServerService.class.getSimpleName());
				}
			} else {
				unbindService(remoteControlServiceConnection);
				stopService(new Intent(this, RemoteControlServerService.class));
				this.remoteControlServerService = null;
			}
			break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (usbAccessoryManager != null) {
			return usbAccessoryManager;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	@Override
	void onQuit() {
		switchRCServer.setChecked(false);
		doUnbindServices();
		stopServices();
	}

	public void logConsole(String message) {
		controller.logConsole(message);
	}

	@Override
	void initController() {
		controller = new RemoteControlController(this);
		controller.usbAccessoryAttached();
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
			usbAccessoryManager = ((UsbAccessoryBinder) binder).getUsbAccessoryManager();
			if (usbAccessoryManager.isOpened()) {
				ArduinoManager arduinoHandlerThread = new ArduinoManager(usbAccessoryManager);
				arduinoHandlerThread.setHandler(handler);
				Thread thread = new Thread(null, arduinoHandlerThread, "arduinoHandlerThread");
				thread.start();
			}
		} else if (binder instanceof RemoteControlServerBinder) {
			remoteControlServerService = ((RemoteControlServerBinder) binder).getService();
			remoteControlServerService.setHandler(this.handler);
			switchRCServer.setChecked(remoteControlServerService.isRunning());
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

}