package com.company.android.arduinoadk;

import android.widget.TextView;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private TextView xCoor; // declare X axis object
	private TextView yCoor; // declare Y axis object
	private ConsoleView console;

	public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		xCoor = (TextView) findViewById(R.id.xcoor); // create X axis object
		yCoor = (TextView) findViewById(R.id.ycoor); // create Y axis object
		console = (ConsoleView) findViewById(R.id.consoleClient);
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {

	}

	public void setPosition(float x, float y) {
		xCoor.setText("X: " + x);
		yCoor.setText("Y: " + y);
	}

	public void logConsole(String message) {
		console.log(message);
	}

}
