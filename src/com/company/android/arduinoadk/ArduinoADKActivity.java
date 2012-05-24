package com.company.android.arduinoadk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.company.android.arduinoadk.LocalService.LocalServiceBinder;

public class ArduinoADKActivity extends Activity implements OnCheckedChangeListener {
	private static final String TAG = ArduinoADKActivity.class.getSimpleName();

	private ArduinoController arduinoController;
	private RemoteControlServerController rcServerController;
	private Switch switchRcServer;

	// Local Service
	private boolean boundToLocalService;
	private LocalService localService;

	private PowerManager.WakeLock wakeLock;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case TELEMETRY:
				handleTelemetryMessage((ArduinoMessage) msg.obj);
				break;
			case SERVER_LOG:
				logConsole((String) msg.obj);
				break;
			case SERVER_START:
				break;
			case SERVER_STOP:
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

		setContentView(R.layout.main);
		findViewById(R.id.container1).setVisibility(View.VISIBLE);
		findViewById(R.id.container2).setVisibility(View.GONE);
		switchRcServer = (Switch) findViewById(R.id.switchRCServer);

		initControllers();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.company.android.arduinoadk.wakelock");

		createAndBindLocalService();
	}

	private void createAndBindLocalService() {
		Intent intent = new Intent(this, LocalService.class);
		// Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		startService(intent);
		// Bind from the service
		boolean success = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to local service");
		}
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		doUnbindLocalService();
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindLocalService() {
		// Detach our existing connection
		if (isBoundToLocalService()) {
			unbindService(serviceConnection);
			boundToLocalService = false;
		}
	}

	private boolean isBoundToLocalService() {
		return this.boundToLocalService;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				localService.startRcServer();
			} else {
				localService.stopRcServer();
			}
			break;
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalServiceBinder binder = (LocalServiceBinder) service;
			localService = binder.getService();
			switchRcServer.setChecked(localService.isRcServerStarted());
			switchRcServer.setOnCheckedChangeListener(ArduinoADKActivity.this);
			boundToLocalService = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			boundToLocalService = false;
		}
	};

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		wakeLock.acquire();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		arduinoController.usbAccessoryDetached();
		rcServerController.usbAccessoryDetached();
		wakeLock.release();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
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

	private void quit() {
		// localService.getRcServer().stopServer();
		doUnbindLocalService();
		stopService(new Intent(this, LocalService.class));
		moveTaskToBack(true);
	}

	public void logConsole(String message) {
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