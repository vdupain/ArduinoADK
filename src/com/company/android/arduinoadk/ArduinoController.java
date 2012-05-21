package com.company.android.arduinoadk;

public class ArduinoController extends AbstractController {
	private RadarView radarView;

	public ArduinoController(ArduinoADKActivity activity) {
		super(activity);
		radarView = (RadarView) findViewById(R.id.radarView);
	}

	@Override
	protected void onUsbAccesssoryAttached() {
	}

	@Override
	protected void onUsbAccesssoryDetached() {

	}

	public void setRadarPosition(int degree, int distance) {
		radarView.setPosition(degree, distance);
	}

}
