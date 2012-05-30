package com.company.android.arduinoadk;

import android.text.Html;
import android.widget.TextView;

public class ArduinoController extends AbstractController {
	private RadarView radarView;
	private TextView console;

	public ArduinoController(ArduinoActivity activity) {
		super(activity);
		console = (TextView) findViewById(R.id.arduinoConsole);
		radarView = (RadarView) findViewById(R.id.radarView);
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {

	}

	public void setRadarPosition(int degree, int distance) {
		radarView.setPosition(degree, distance);
	}

	public void logConsole(String message) {
		String t = console.getText().toString();
		if (t.split("\n").length >= 6) {
			console.setText(t.substring(t.indexOf("\n") + 1, t.length()));
		}
		console.append(Html.fromHtml(message + "<br />"));
	}

}
