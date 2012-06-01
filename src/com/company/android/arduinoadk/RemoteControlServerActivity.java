package com.company.android.arduinoadk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlService.RemoteControlBinder;
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

	private RemoteControlManager remoteControlManager;
	private UsbAccessoryManager usbAccessoryManager;

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
		startService(new Intent(this, RemoteControlService.class));
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		Intent intent = new Intent(this, UsbAccessoryService.class);
		// Create a new Messenger for the communication back
		// From the Service to the Activity
		intent.putExtra("MESSENGER", new Messenger(handler));
		boolean success = bindService(intent, usbServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + UsbAccessoryService.class.getSimpleName());
		}
		intent = new Intent(this, RemoteControlService.class);
		// Create a new Messenger for the communication back
		// From the Service to the Activity
		intent.putExtra("MESSENGER", new Messenger(handler));
		success = bindService(intent, remoteControlServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + RemoteControlService.class.getSimpleName());
		}
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		if (isBoundToRemoteControlManager()) {
			unbindService(remoteControlServiceConnection);
			this.remoteControlManager = null;
		}
		if (isBoundToUsbAccessoryManager()) {
			unbindService(usbServiceConnection);
			this.usbAccessoryManager = null;
		}
	}

	private void stopServices() {
		stopService(new Intent(this, RemoteControlService.class));
		stopService(new Intent(this, UsbAccessoryService.class));
	}

	private boolean isBoundToRemoteControlManager() {
		return this.remoteControlManager != null;
	}

	private boolean isBoundToUsbAccessoryManager() {
		return this.usbAccessoryManager != null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				remoteControlManager.startServer();
			} else {
				remoteControlManager.stopServer();
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
		} else if (binder instanceof RemoteControlBinder) {
			RemoteControlBinder b = (RemoteControlBinder) binder;
			remoteControlManager = b.getRemoteControlManager();
			this.getArduinoADKApplication().setRemoteControlManager(remoteControlManager);
			if (usbAccessoryManager != null)
				remoteControlManager.setUsbAccessoryManager(this.usbAccessoryManager);
			if (getArduinoADKApplication().getSettings().isRCServerAutoStart()) {
				remoteControlManager.startServer();
			}
			switchRCServer.setChecked(remoteControlManager.isServerStarted());
			switchRCServer.setOnCheckedChangeListener(RemoteControlServerActivity.this);
			remoteControlManager.setHandler(this.handler);
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

	public RemoteControlManager getRemoteControlManager() {
		return remoteControlManager;
	}

}