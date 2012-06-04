package com.company.android.arduinoadk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.company.android.arduinoadk.remotecontrol.PositionMessage;
import com.company.android.arduinoadk.remotecontrol.RemoteControlClientService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlClientService.RemoteControlClientBinder;

public class RemoteControlClientActivity extends BaseActivity implements ServiceConnected, OnCheckedChangeListener {
	private static final String TAG = RemoteControlClientActivity.class.getSimpleName();

	private Switch switchRCClient;
	private RemoteControlClientController controller;

	private ArduinoADKServiceConnection remoteControlServiceConnection = new ArduinoADKServiceConnection(this);
	private RemoteControlClientService remoteControlClientService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcclient_main);
		RemoteControlClientFragment fragment = (RemoteControlClientFragment) getFragmentManager().findFragmentById(R.id.remoteControlClientFragment);
		switchRCClient = (Switch) fragment.getView().findViewById(R.id.switchRCClient);
		switchRCClient.setOnCheckedChangeListener(this);
		initController();
	}

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case RCCLIENT_POSITION:
				handlePositionMessage((PositionMessage) msg.obj);
				break;
			case SERVER_CONNECTION_FAILURE:
				handleServerConnectionFailure((Exception) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		doBindServices();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void handlePositionMessage(PositionMessage positionMessage) {
		controller.setPosition(positionMessage.getX(), positionMessage.getY());
		controller.logConsole(positionMessage.getX() + " - " + positionMessage.getY());
	}

	private void handleServerConnectionFailure(Exception ex) {
		switchRCClient.setChecked(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Server connection failure").setMessage(ex.getMessage()).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		doUnbindServices();
	}

	@Override
	void initController() {
		controller = new RemoteControlClientController(this);
	}

	@Override
	void onQuit() {
		switchRCClient.setChecked(false);
		doUnbindServices();
		stopServices();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCClient:
			if (buttonView.isChecked()) {
				Intent intent = new Intent(this, RemoteControlClientService.class);
				startService(intent);
				boolean success = bindService(intent, remoteControlServiceConnection, Context.BIND_AUTO_CREATE);
				if (!success) {
					Log.e(TAG, "Failed to bind to " + RemoteControlClientService.class.getSimpleName());
				}
			} else {
				unbindService(remoteControlServiceConnection);
				stopService(new Intent(this, RemoteControlClientService.class));
				this.remoteControlClientService = null;
			}
			break;
		}
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		boolean success = bindService(new Intent(this, RemoteControlClientService.class), remoteControlServiceConnection, 0);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + RemoteControlClientService.class.getSimpleName());
		}
	}

	private void stopServices() {
		stopService(new Intent(this, RemoteControlClientService.class));
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		if (isBoundToRemoteControlClientService()) {
			unbindService(remoteControlServiceConnection);
			this.remoteControlClientService = null;
		}
	}

	private boolean isBoundToRemoteControlClientService() {
		return this.remoteControlClientService != null;
	}

	@Override
	public void onConnected(IBinder binder) {
		Log.d(TAG, "onConnected");
		if (binder == null) {
			Log.e(TAG, "Failed to get binder");
			return;
		}

		// switch between the different services
		if (binder instanceof RemoteControlClientBinder) {
			remoteControlClientService = ((RemoteControlClientBinder) binder).getService();
			remoteControlClientService.setHandler(this.handler);
			switchRCClient.setChecked(remoteControlClientService.isRunning());
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

}
