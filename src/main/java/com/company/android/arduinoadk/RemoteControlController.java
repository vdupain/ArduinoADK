package com.company.android.arduinoadk;

import android.widget.TextView;

public class RemoteControlController extends AbstractController<RemoteControlServerActivity> {
	private static final String TAG = RemoteControlController.class.getSimpleName();

	private ConsoleView console;
	private TextView ip;

	public RemoteControlController(RemoteControlServerActivity activity) {
		super(activity);
		console = (ConsoleView) findViewById(R.id.console);
		ip = (TextView) findViewById(R.id.ip);
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {
	}

	public void displayIP() {
		if (this.activity.getRemoteControlServerService() != null)
			ip.setText(this.activity.getRemoteControlServerService().getIpInfo());
		else
			ip.setText("");
	}

	public void logConsole(String message) {
		console.log(message);
	}

}
