package com.company.android.arduinoadk;


import com.company.android.arduinoadk.joystick.JoystickMovedListener;
import com.company.android.arduinoadk.joystick.JoystickView;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private ConsoleView console;
    private JoystickView joystick;
    private IPWebCamWebView ipWebCamWebView;


    private JoystickMovedListener listener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            activity.getRemoteControlClientService().setPosition(pan, tilt);
        }

        @Override
        public void OnReleased() {
        }

        public void OnReturnedToCenter() {
        };
    };

    public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		//console = (ConsoleView) findViewById(R.id.consoleClient);

        joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnJoystickMovedListener(listener);

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
		//console.log(message);
	}

}
