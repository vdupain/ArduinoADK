package com.company.android.arduinoadk;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

public class RemoteControlClientActivity extends BaseActivity {
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private RemoteControlClientController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rcclient_main);
		// Get an instance of the SensorManager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		initControllers();
	}

	@Override
	void initControllers() {
		controller = new RemoteControlClientController(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
		 * It is not necessary to get accelerometer events at a very high rate,
		 * by using a slower rate (SENSOR_DELAY_UI), we get an automatic
		 * low-pass filter, which "extracts" the gravity component of the
		 * acceleration. As an added benefit, we use less power and CPU
		 * resources.
		 */
		sensorManager.registerListener(controller, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(controller);
	}

	@Override
	void onQuit() {
	}
}
