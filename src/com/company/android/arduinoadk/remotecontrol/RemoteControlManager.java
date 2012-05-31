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
import android.util.Log;

import com.company.android.arduinoadk.ArduinoADK;
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
	private Thread rcServerWorkerThread;
	private Thread rcClientWorkerThread;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private Handler handler;

	class RCServerRunnable implements Runnable {
		@Override
		public void run() {
			serverPort = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServerTCPPort();
			remoteControlServer.service(serverPort);
		}
	}

	class RCClientRunnable implements Runnable {

		@Override
		public void run() {
			/*
			 * It is not necessary to get accelerometer events at a very high
			 * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
			 * automatic low-pass filter, which "extracts" the gravity component
			 * of the acceleration. As an added benefit, we use less power and
			 * CPU resources.
			 */
			sensorManager.registerListener(RemoteControlManager.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
			serverPort = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServerTCPPort();
			host = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServer();
			remoteControlClient.connect(host, serverPort);
		}

	}

	public RemoteControlManager(Context context) {
		this.context = context;
		this.remoteControlServer = new TCPServer();
		this.remoteControlServer.setClientHandler(new RemoteControlClientHandler(this.usbAccessoryManager));
		this.remoteControlClient = new TCPClient();
	}

	public void startServer() {
		if (rcServerWorkerThread == null) {
			rcServerWorkerThread = new Thread(new RCServerRunnable(), RCServerRunnable.class.getSimpleName() + "Thead");
			rcServerWorkerThread.start();
		}
	}

	public void startClient() {
		if (rcClientWorkerThread == null) {
			rcClientWorkerThread = new Thread(new RCClientRunnable(), RCClientRunnable.class.getSimpleName() + "Thead");
			rcClientWorkerThread.start();
		}
	}

	public boolean isServerStarted() {
		return rcServerWorkerThread != null;
	}

	public boolean isClientStarted() {
		return rcClientWorkerThread != null;
	}

	public void stopServer() {
		if (rcServerWorkerThread != null) {
			remoteControlServer.stop();
			rcServerWorkerThread.interrupt();
			rcServerWorkerThread = null;
		}
	}

	public void stopClient() {
		if (rcClientWorkerThread != null) {
			remoteControlClient.stop();
			sensorManager.unregisterListener(this);
			rcClientWorkerThread.interrupt();
			rcClientWorkerThread = null;
		}
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

	public TCPServer getRemoteControlServer() {
		return remoteControlServer;
	}

	public TCPClient getRemoteControlClient() {
		return remoteControlClient;
	}

	public void onCreate() {
		// Get an instance of the SensorManager
		sensorManager = (SensorManager) this.context.getSystemService(this.context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void setPosition(float x, float y) {
		if (!remoteControlClient.isRunning()) {
			remoteControlClient.writeContent("STICK:x=" + x + ":y=" + y + "\n");
		}
		Message m = Message.obtain(handler, WhatAbout.RCCLIENT_POSITION.ordinal());
		m.obj = new PositionMessage(x, y);
		handler.sendMessage(m);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		float x = 0, y = 0;
		x = event.values[0];
		y = event.values[1];
		setPosition(x, y);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
