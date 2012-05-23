package com.company.android.arduinoadk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbAccessory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.company.android.arduinoadk.RemoteControlService.LocalBinder;
import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

public class ArduinoADKActivity extends Activity implements OnCheckedChangeListener {
	private static final String TAG = "ArduinoADKActivity";

	protected UsbAccessoryManager usbAccessoryManager;

	private ArduinoController arduinoController;
	private RemoteControlServerController rcServerController;

	private PowerManager.WakeLock wakeLock;

	private RemoteControlService remoteControlService;
	private boolean mIsBound = false;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case TELEMETRY:
				handleTelemetryMessage((ArduinoMessage) msg.obj);
				break;
			case SERVER_LOG:
				logServerConsole((String) msg.obj);
				break;
			case SERVER_START:
				break;
			case SERVER_STOP:
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		usbAccessoryManager = new UsbAccessoryManager(this.getApplicationContext(), handler);
		usbAccessoryManager.setupAccessory((UsbAccessory) this.getLastNonConfigurationInstance());

		setContentView(R.layout.main);
		findViewById(R.id.container1).setVisibility(View.VISIBLE);
		findViewById(R.id.container2).setVisibility(View.GONE);

		Switch switchRcServer = (Switch) findViewById(R.id.switchRCServer);
		switchRcServer.setOnCheckedChangeListener(this);

		initControllers();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.company.android.arduinoadk.wakelock");
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				doBindService();
			} else {
				doUnbindService();
			}
			break;
		}
	}

	private void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(this, RemoteControlService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		// Unbind from the service
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.usbAccessoryManager.getUsbAccessory() != null) {
			return this.usbAccessoryManager.getUsbAccessory();
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		wakeLock.acquire();
		usbAccessoryManager.reOpenAccessory();
		// this.rcServerController.displayIP();
	}

	@Override
	public void onPause() {
		super.onPause();
		usbAccessoryManager.closeUsbAccessory();
		arduinoController.usbAccessoryDetached();
		rcServerController.usbAccessoryDetached();
		wakeLock.release();
	}

	@Override
	public void onDestroy() {
		this.usbAccessoryManager.unregisterReceiver();
		super.onDestroy();
		this.doUnbindService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rcserver:
			findViewById(R.id.container1).setVisibility(View.VISIBLE);
			findViewById(R.id.container2).setVisibility(View.GONE);
			return true;
		case R.id.menu_arduino:
			findViewById(R.id.container1).setVisibility(View.GONE);
			findViewById(R.id.container2).setVisibility(View.VISIBLE);
			return true;
		case R.id.menu_quit:
			quit();
			return true;
		case R.id.menu_test:
			TestUtils.test(handler);
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_abouthelp:
			Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			remoteControlService = binder.getService();
			mIsBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mIsBound = false;
		}
	};

	private void quit() {
		finish();
		System.exit(0);
	}

	public void logServerConsole(String message) {
		rcServerController.logConsole(message);
	}

	private void initControllers() {
		arduinoController = new ArduinoController(this);
		arduinoController.usbAccessoryAttached();

		rcServerController = new RemoteControlServerController(this);
		rcServerController.usbAccessoryAttached();
	}

	private void handleTelemetryMessage(ArduinoMessage message) {
		arduinoController.setRadarPosition(message.getDegree(), message.getDistance());
	}

}