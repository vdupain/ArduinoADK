package com.company.android.arduinoadk;


public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private ConsoleView console;

	public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		console = (ConsoleView) findViewById(R.id.consoleClient);
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {

	}

	public void setPosition(float x, float y) {
	}

	public void logConsole(String message) {
		console.log(message);
	}

}
