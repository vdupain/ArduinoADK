package com.company.android.arduinoadk;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

public class RemoteControlService extends IntentService {


	public RemoteControlService() {
		super("RemoteControlService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		RemoteControlServer server = new RemoteControlServer();
		server.run();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "RC Service starting", Toast.LENGTH_SHORT).show();
		return super.onStartCommand(intent, flags, startId);
	}

}
