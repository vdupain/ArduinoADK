package com.company.android.arduinoadk;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RCClientActivity extends BaseActivity {
	private StickView stickView;
	private SensorManager sensorManager;

	private TextView xCoor; // declare X axis object
	private TextView yCoor; // declare Y axis object
	private TextView zCoor; // declare Z axis object

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rcclient_main);

		xCoor = (TextView) findViewById(R.id.xcoor); // create X axis object
		yCoor = (TextView) findViewById(R.id.ycoor); // create Y axis object
		zCoor = (TextView) findViewById(R.id.zcoor); // create Z axis object

		// Get an instance of the SensorManager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// instantiate our stick view and set it as the activity's content
		stickView = new StickView(this);
	}

	@Override
	void initControllers() {
	}

	@Override
	public void onResume() {
		super.onResume();
		stickView.startStick();
	}

	@Override
	public void onPause() {
		super.onPause();
		stickView.stopStick();
	}

	class StickView extends View implements SensorEventListener {
		private Sensor accelerometer;

		public StickView(Context context) {
			super(context);
			accelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}

		public void startStick() {
			/*
			 * It is not necessary to get accelerometer events at a very high
			 * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
			 * automatic low-pass filter, which "extracts" the gravity component
			 * of the acceleration. As an added benefit, we use less power and
			 * CPU resources.
			 */
			sensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
		}

		public void stopStick() {
			sensorManager.unregisterListener(this);
		}

		public void onSensorChanged(SensorEvent event) {
			// check sensor type
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				// assign directions
				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];

				xCoor.setText("X: " + x);
				yCoor.setText("Y: " + y);
				zCoor.setText("Z: " + z);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	}

	@Override
	void onQuit() {
	}
}
