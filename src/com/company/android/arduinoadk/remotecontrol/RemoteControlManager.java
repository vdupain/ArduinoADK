package com.company.android.arduinoadk.remotecontrol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.company.android.arduinoadk.ArduinoADK;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlManager implements Runnable {
	private static final String TAG = RemoteControlManager.class
			.getSimpleName();

	private RemoteControlServer remoteControlServer;
	private UsbAccessoryManager usbAccessoryManager;
	private Handler handler;
	private final Context context;
	private int port;
	private Thread workerThread;

	public RemoteControlManager(Context context, Handler messageHandler) {
		this.context = context;
		this.handler = messageHandler;
		this.remoteControlServer = new RemoteControlServer(usbAccessoryManager,
				handler);
	}

	public void start() {
		if (workerThread == null) {
			workerThread = new Thread(this,
					RemoteControlManager.class.getSimpleName() + "Thead");
			workerThread.start();
		}
	}

	public boolean isStarted() {
		if (workerThread != null)
			return workerThread.isAlive();
		return false;
	}

	public void stop() {
		remoteControlServer.stopServer();
		if (workerThread != null) {
			workerThread.interrupt();
			workerThread = null;
		}
	}

	@Override
	public void run() {
		this.port = ((ArduinoADK) this.context.getApplicationContext())
				.getSettings().getRCServerTCPPort();
		remoteControlServer.service(port);
	}

	public void setUsbAccessoryManager(UsbAccessoryManager usbAccessoryManager) {
		this.usbAccessoryManager = usbAccessoryManager;
	}

	public String getIpInfo() {
		StringBuffer s = new StringBuffer();
		// Determines if user is connected to a wireless network & displays ip
		WifiManager wifiManager = (WifiManager) this.context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo.getNetworkId() > -1) {
			int ipAddress = wifiInfo.getIpAddress();
			byte[] byteaddr = new byte[] { (byte) (ipAddress & 0xff),
					(byte) (ipAddress >> 8 & 0xff),
					(byte) (ipAddress >> 16 & 0xff),
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
