package com.company.android.arduinoadk.remotecontrol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.company.android.arduinoadk.ArduinoADK;
import com.company.android.arduinoadk.MathHelper;
import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.clientserver.TCPClient;
import com.company.android.arduinoadk.clientserver.TCPServer;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlManager implements SensorEventListener {
	private static final String TAG = RemoteControlManager.class.getSimpleName();

	private TCPServer remoteControlServer;
	private TCPClient remoteControlClient;
	private UsbAccessoryManager usbAccessoryManager;
	private final Context context;
	private int serverPort;
	private String host;
	
	private Display display;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private Handler handler;
	private Messenger messenger;

	public RemoteControlManager(Context context) {
		this.context = context;
		serverPort = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServerTCPPort();
		host = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServer();
	}

	public void startServer() {
		this.remoteControlServer = new TCPServer(serverPort);
		this.remoteControlServer.setClientHandler(new RemoteControlClientHandler(this.usbAccessoryManager));
		this.remoteControlServer.setHandler(this.handler);
		this.remoteControlServer.start();
	}

	public void stopServer() {
		if (remoteControlServer == null)
			return;
		this.remoteControlServer.stopServer();
		try {
			remoteControlServer.join();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		remoteControlServer = null;
	}

	public boolean isServerStarted() {
		return remoteControlServer != null && remoteControlServer.isListen();
	}

	public void startClient() {
		this.remoteControlClient = new TCPClient(host, serverPort);
		this.remoteControlClient.setHandler(this.handler);
		this.remoteControlClient.start();
		sensorManager.registerListener(RemoteControlManager.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}

	public boolean isClientStarted() {
		return remoteControlClient != null && remoteControlClient.isConnected();
	}

	public void stopClient() {
		if (remoteControlClient == null)
			return;
		sensorManager.unregisterListener(this);
		remoteControlClient.cancel();
		try {
			remoteControlClient.join();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		remoteControlClient = null;
	}

	public TCPServer getRemoteControlServer() {
		return remoteControlServer;
	}

	public TCPClient getRemoteControlClient() {
		return remoteControlClient;
	}

	public void onCreate() {
		// Get an instance of the SensorManager
		sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// and instantiate the display to know the device orientation
		display = ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Log.i(TAG, "accelerometer maximum range =" + accelerometer.getMaximumRange());
		Log.i(TAG, "accelerometer name =" + accelerometer.getName());
		Log.i(TAG, "accelerometer vendor =" + accelerometer.getVendor());
		Log.i(TAG, "accelerometer version =" + accelerometer.getVersion());
		Log.i(TAG, "accelerometer resolution =" + accelerometer.getResolution());
		Log.i(TAG, "accelerometer power =" + accelerometer.getPower());
		Log.i(TAG, "accelerometer min delay =" + accelerometer.getMinDelay());
		Log.i(TAG, "accelerometer type =" + accelerometer.getType());
	}

	public void setPosition(float x, float y) {
		float valueX = MathHelper.constrain(x, -10, 10);
		float valueY = MathHelper.constrain(y, -10, 10);
		valueX = MathHelper.map(valueX, -10, 10, 0, 180);
		valueY = MathHelper.map(valueY, -10, 10, 0, 180);
		if (remoteControlClient.isConnected()) {
			remoteControlClient.writeContent("STICK:x=" + valueX + ":y=" + valueY + "\n");
		}
		Message m = Message.obtain(handler, WhatAbout.RCCLIENT_POSITION.ordinal(), new PositionMessage(valueX, valueY));
		handler.sendMessage(m);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		float x = 0, y = 0;
		// Depending on the device orientation get the x,y value of the acceleration
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

	public void setHandler(Handler handler) {
		this.handler = handler;
		if (remoteControlServer != null)
			remoteControlServer.setHandler(this.handler);
		if (remoteControlClient != null)
			remoteControlClient.setHandler(handler);
	}

	public void setMessenger(Messenger messenger) {
		this.messenger = messenger;
	}

	public void setUsbAccessoryManager(UsbAccessoryManager usbAccessoryManager) {
		this.usbAccessoryManager = usbAccessoryManager;
	}

	public String getIpInfo() {
		StringBuffer s = new StringBuffer();
		// Determines if user is connected to a wireless network & displays ip
		WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo.getNetworkId() > -1) {
			int ipAddress = wifiInfo.getIpAddress();
			byte[] byteaddr = new byte[] { (byte) (ipAddress & 0xff), (byte) (ipAddress >> 8 & 0xff), (byte) (ipAddress >> 16 & 0xff),
					(byte) (ipAddress >> 24 & 0xff) };
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByAddress(byteaddr);
				s.append("tcp://");
				s.append(inetAddress.getHostAddress());
				// s.append(":" + rcServer.getPort() + "/");
			} catch (UnknownHostException e) {
				Log.e(TAG, e.getMessage(), e);
				s.delete(0, s.length());
				s.append(e.getMessage());
			}
		} else {
			s.append("Wifi should be enabled !");
		}
		return s.toString();
	}

}
