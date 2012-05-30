package com.company.android.arduinoadk;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class RemoteControlClientController extends
		AbstractController<RemoteControlClientActivity> implements
		SensorEventListener {
	private TextView xCoor; // declare X axis object
	private TextView yCoor; // declare Y axis object
	private TextView zCoor; // declare Z axis object

	public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		xCoor = (TextView) findViewById(R.id.xcoor); // create X axis object
		yCoor = (TextView) findViewById(R.id.ycoor); // create Y axis object
		zCoor = (TextView) findViewById(R.id.zcoor); // create Z axis object
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {

	}

	public void setPosition(float x, float y, float z) {
		xCoor.setText("X: " + x);
		yCoor.setText("Y: " + y);
		zCoor.setText("Z: " + z);
		//FIXME remapper x et y à des valeurs 0 à 180 et envoyer au client RC
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// check sensor type
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// assign directions
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			setPosition(x, y, z);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
