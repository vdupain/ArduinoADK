package com.company.android.arduinoadk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.company.android.arduinoadk.remotecontrol.PositionMessage;

public class RemoteControlClientActivity extends BaseActivity implements OnCheckedChangeListener {
	private Switch switchRCClient;
	private RemoteControlClientController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcclient_main);
		RemoteControlClientFragment fragment = (RemoteControlClientFragment) getFragmentManager().findFragmentById(R.id.remoteControlClientFragment);
		switchRCClient = (Switch) fragment.getView().findViewById(R.id.switchRCClient);
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
	public void onResume() {
		super.onResume();
		if (getArduinoADKApplication().getRemoteControlManager() != null) {
			switchRCClient.setChecked(getArduinoADKApplication().getRemoteControlManager().isClientStarted());
		}
		switchRCClient.setOnCheckedChangeListener(this);
		this.getArduinoADKApplication().getRemoteControlManager().setHandler(handler);
		this.getArduinoADKApplication().getRemoteControlManager().getRemoteControlClient().setHandler(handler);
	}

	private void handlePositionMessage(PositionMessage positionMessage) {
		controller.setPosition(positionMessage.getX(), positionMessage.getY());
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
	void initController() {
		controller = new RemoteControlClientController(this);
	}

	@Override
	void onQuit() {
	}

	public void setPosition(float x, float y) {
		controller.setPosition(x, y);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCClient:
			if (buttonView.isChecked()) {
				getArduinoADKApplication().getRemoteControlManager().startClient();
			} else {
				getArduinoADKApplication().getRemoteControlManager().stopClient();
			}
			break;
		}
	}

}
