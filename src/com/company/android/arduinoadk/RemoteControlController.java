package com.company.android.arduinoadk;

import android.text.Html;
import android.widget.TextView;

public class RemoteControlController extends
		AbstractController<RemoteControlServerActivity> {
	private static final String TAG = RemoteControlController.class
			.getSimpleName();

	private TextView console;
	private TextView ip;

	public RemoteControlController(RemoteControlServerActivity activity) {
		super(activity);
		console = (TextView) findViewById(R.id.console);
		ip = (TextView) findViewById(R.id.ip);
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {
	}

	public void displayIP() {
		//ip.setText(this.activity.getRemoteControlManager().getIpInfo());
	}

	public void logConsole(String message) {
		String t = console.getText().toString();
		if (t.split("\n").length >= 20) {
			console.setText(t.substring(t.indexOf("\n") + 1, t.length()));
		}
		console.append(Html.fromHtml(message + "<br />"));
	}

}
