package com.company.android.arduinoadk;


import android.net.Uri;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import com.company.android.arduinoadk.joystick.DualJoystickView;
import com.company.android.arduinoadk.joystick.JoystickMovedListener;
import com.company.android.arduinoadk.joystick.JoystickView;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> {
	private ConsoleView console;
    private JoystickView joystick;
    private IPWebCamWebView ipWebCamWebView;
    private VideoView videoView;


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
//        ipWebCamWebView = (IPWebCamWebView) findViewById(R.id.ipWebcamWebview);
//        WebSettings webSettings = ipWebCamWebView.getSettings();
//        webSettings.setPluginState(WebSettings.PluginState.ON);
//        webSettings.setJavaScriptEnabled(true);
//        ipWebCamWebView.setWebViewClient(new WebViewClient());
//        String server = this.activity.getArduinoADKApplication().getSettings().getRCServer();
//        ipWebCamWebView.loadUrl("http://" + server+ ":8080" + "/videofeed");
        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this.activity.getBaseContext());
        mediaController.setAnchorView(videoView);
        String server = this.activity.getArduinoADKApplication().getSettings().getRCServer();
        Uri video = Uri.parse("http://" + server+ ":8080" + "/videofeed");
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);

        videoView.start();
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
