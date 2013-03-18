package com.company.android.arduinoadk;


import android.widget.TextView;
import com.company.android.arduinoadk.joystick.DualJoystickView;
import com.company.android.arduinoadk.joystick.JoystickMovedListener;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private ConsoleView console;
    DualJoystickView joystick;
    TextView txtX1, txtY1;
    TextView txtX2, txtY2;
    IPWebCamWebView ipWebCamWebView;

    private JoystickMovedListener left = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            txtX1.setText(Integer.toString(pan));
            txtY1.setText(Integer.toString(tilt));
            activity.getRemoteControlClientService().setPosition(pan, tilt);
        }

        @Override
        public void OnReleased() {
            txtX1.setText("released");
            txtY1.setText("released");
        }

        public void OnReturnedToCenter() {
            txtX1.setText("stopped");
            txtY1.setText("stopped");
        };
    };

    private  JoystickMovedListener right = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            txtX2.setText(Integer.toString(pan));
            txtY2.setText(Integer.toString(tilt));
            activity.getRemoteControlClientService().setPosition(pan, tilt);
        }

        @Override
        public void OnReleased() {
            txtX2.setText("released");
            txtY2.setText("released");
        }

        public void OnReturnedToCenter() {
            txtX2.setText("stopped");
            txtY2.setText("stopped");
        };
    };

    public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		console = (ConsoleView) findViewById(R.id.consoleClient);
        txtX1 = (TextView)findViewById(R.id.TextViewX1);
        txtY1 = (TextView)findViewById(R.id.TextViewY1);
        txtX2 = (TextView)findViewById(R.id.TextViewX2);
        txtY2 = (TextView)findViewById(R.id.TextViewY2);
        joystick = (DualJoystickView) findViewById(R.id.dualjoystickView);
        joystick.setOnJoystickMovedListener(left, right);
        ipWebCamWebView = (IPWebCamWebView) findViewById(R.id.ipWebcamWebview);
        String server = this.activity.getArduinoADKApplication().getSettings().getRCServer();
        ipWebCamWebView.loadUrl("http://" + server+ ":8080");
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
