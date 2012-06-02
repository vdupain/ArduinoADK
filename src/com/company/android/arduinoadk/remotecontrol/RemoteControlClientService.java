package com.company.android.arduinoadk.remotecontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.company.android.arduinoadk.ArduinoADK;
import com.company.android.arduinoadk.MathHelper;
import com.company.android.arduinoadk.R;
import com.company.android.arduinoadk.RemoteControlClientActivity;
import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.clientserver.TCPClient;

/**
 * This service is only used to contain the RemoteControlServer running in the
 * background. To use it, you need to bind to this service and get it from the
 * binder.
 */
public class RemoteControlClientService extends Service implements SensorEventListener {

	private static final String TAG = RemoteControlClientService.class.getSimpleName();

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.notif_rcclient_service_started;

	// Binder given to clients
	private final IBinder binder = new RemoteControlClientBinder();

	/** For showing and hiding our notification. */
	private NotificationManager notificationManager;

	private TCPClient client;

	private Display display;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private Handler handler;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class RemoteControlClientBinder extends Binder {
		public RemoteControlClientService getService() {
			return RemoteControlClientService.this;
		}

	}

	@Override
	public void onCreate() {
		// The service is being created
		Log.d(TAG, "onCreate");
		// Get an instance of the SensorManager
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// and instantiate the display to know the device orientation
		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Log.i(TAG, "accelerometer maximum range =" + accelerometer.getMaximumRange());
		Log.i(TAG, "accelerometer name =" + accelerometer.getName());
		Log.i(TAG, "accelerometer vendor =" + accelerometer.getVendor());
		Log.i(TAG, "accelerometer version =" + accelerometer.getVersion());
		Log.i(TAG, "accelerometer resolution =" + accelerometer.getResolution());
		Log.i(TAG, "accelerometer power =" + accelerometer.getPower());
		Log.i(TAG, "accelerometer min delay =" + accelerometer.getMinDelay());
		Log.i(TAG, "accelerometer type =" + accelerometer.getType());

		startClient();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
	}

	private void startClient() {
		if (client == null) {
			int serverPort = ((ArduinoADK) getApplicationContext()).getSettings().getRCServerTCPPort();
			String host = ((ArduinoADK) getApplicationContext()).getSettings().getRCServer();
			client = new TCPClient(host, serverPort);
			client.start();
		}
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(TAG, "onDestroy");
		stopClient();
		// Cancel the persistent notification.
		clearNotification();
	}

	private void stopClient() {
		if (client!=null) {
			sensorManager.unregisterListener(this);
			client.cancel();
			try {
				client.join();
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			client = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "onStartCommand startId " + startId + ": intent " + intent);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		Log.d(TAG, "onBind");
		return this.binder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return true;
	}

	public boolean isRunning() {
		return client!=null && client.isAlive();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		CharSequence text = getText(R.string.notif_rcclient_service_started);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, RemoteControlClientActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		Notification notification = new Notification.Builder(this).setTicker(text).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(text).setContentTitle(getText(R.string.notif_rcclient_service_label)).setContentIntent(contentIntent).getNotification();
		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		notificationManager.notify(NOTIFICATION, notification);
	}

	private void clearNotification() {
		notificationManager.cancel(NOTIFICATION);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
		this.client.setHandler(handler);
	}

	public void setPosition(float x, float y) {
		float valueX = MathHelper.constrain(x, -10, 10);
		float valueY = MathHelper.constrain(y, -10, 10);
		valueX = 180 - MathHelper.map(valueX, -10, 10, 0, 180);
		valueY = MathHelper.map(valueY, -10, 10, 0, 180);
		if (isRunning() && client.isConnected()) {
			client.writeContent("STICK:x=" + valueX + ":y=" + valueY + "\n");
		}

		Message m = Message.obtain(handler, WhatAbout.RCCLIENT_POSITION.ordinal(), new PositionMessage(valueX, valueY));
		handler.sendMessage(m);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		float x = 0, y = 0;
		// Depending on the device orientation get the x,y value of the
		// acceleration
		switch (display.getRotation()) {
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
