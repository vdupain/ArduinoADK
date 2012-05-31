package com.company.android.arduinoadk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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

	public RemoteControlController rcServerController;
	private Switch switchRcServer;

	/** Defines callbacks for service binding, passed to bindService() */
	private ArduinoADKServiceConnection usbServiceConnection = new ArduinoADKServiceConnection(this);
	private ArduinoADKServiceConnection remoteControlServiceConnection = new ArduinoADKServiceConnection(this);

	private RemoteControlManager remoteControlManager;
	private UsbAccessoryManager usbAccessoryManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcserver_main);
		RemoteControlServerFragment fragment = (RemoteControlServerFragment) getFragmentManager().findFragmentById(R.id.remoteControlServerFragment);
		switchRcServer = (Switch) fragment.getView().findViewById(R.id.switchRCServer);
		initControllers();
		createServices();
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
		rcServerController.usbAccessoryDetached();
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
		boolean success = bindService(new Intent(this, UsbAccessoryService.class), usbServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + UsbAccessoryService.class.getSimpleName());
		}
		success = bindService(new Intent(this, RemoteControlService.class), remoteControlServiceConnection, Context.BIND_AUTO_CREATE);
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
		rcServerController.logConsole(message);
	}

	@Override
	void initControllers() {
		rcServerController = new RemoteControlController(this);
		rcServerController.usbAccessoryAttached();
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
				ArduinoManager arduinoHandlerThread = new ArduinoManager(usbAccessoryManager, null);
				Thread thread = new Thread(null, arduinoHandlerThread, "arduinoHandlerThread");
				thread.start();
			}
		} else if (binder instanceof RemoteControlBinder) {
			RemoteControlBinder b = (RemoteControlBinder) binder;
			remoteControlManager = b.getRemoteControlManager();
			this.getArduinoADKApplication().setRemoteControlManager(remoteControlManager);
			if (usbAccessoryManager != null)
				remoteControlManager.setUsbAccessoryManager(this.usbAccessoryManager);
			if (((ArduinoADK) getApplicationContext()).getSettings().isRCServerAutoStart()) {
				remoteControlManager.startServer();
			}
			switchRcServer.setChecked(remoteControlManager.isServerStarted());
			switchRcServer.setOnCheckedChangeListener(RemoteControlServerActivity.this);
			b.getService().setActivity(this);
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