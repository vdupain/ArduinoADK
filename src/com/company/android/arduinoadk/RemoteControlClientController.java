package com.company.android.arduinoadk;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.Surface;
import android.widget.TextView;

public class RemoteControlClientController extends AbstractController<RemoteControlClientActivity> implements SensorEventListener {
	private TextView xCoor; // declare X axis object
	private TextView yCoor; // declare Y axis object

	public RemoteControlClientController(RemoteControlClientActivity activity) {
		super(activity);
		xCoor = (TextView) findViewById(R.id.xcoor); // create X axis object
		yCoor = (TextView) findViewById(R.id.ycoor); // create Y axis object
	}

	@Override
	protected void onUsbAccessoryAttached() {
	}

	@Override
	protected void onUsbAccessoryDetached() {

	}

	public void setPosition(float x, float y) {
		xCoor.setText("X: " + x);
		yCoor.setText("Y: " + y);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		/*
		 * record the accelerometer data, the event's timestamp as well as the
		 * current time. The latter is needed so we can calculate the "present"
		 * time during rendering. In this application, we need to take into
		 * account how the screen is rotated with respect to the sensors (which
		 * always return data in a coordinate space aligned to with the screen
		 * in its native orientation).
		 */
		float x = 0, y = 0;
		switch (activity.getDisplay().getRotation()) {
		case Surface.ROTATION_0:
			x = event.values[0];
			y = event.values[1];
			break;
		case Surface.ROTATION_90:
			x = -event.values[1];
			y = event.values[0];
			break;
		case Surface.ROTATION_180:
			x = -event.values[0];
			y = -event.values[1];
			break;
		case Surface.ROTATION_270:
			x = event.values[1];
			y = -event.values[0];
			break;
		}
		setPosition(x, y);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
