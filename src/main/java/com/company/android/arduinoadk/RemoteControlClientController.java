package com.company.android.arduinoadk;


import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.company.android.arduinoadk.joystick.JoystickMovedListener;
import com.company.android.arduinoadk.joystick.JoystickView;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private ConsoleView console;
    private JoystickView joystick;
    private WebView webView;


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

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.getSettings().setJavaScriptEnabled(true);
        String server = this.activity.getArduinoADKApplication().getSettings().getRCServer();
        //Uri url = Uri.parse("http://" + server+ ":8080" + "/");
        //webView.loadUrl(url.toString());
        webView.loadUrl("file:///android_asset/index.html");
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
