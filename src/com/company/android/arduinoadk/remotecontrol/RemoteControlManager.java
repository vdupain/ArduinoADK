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

public class RemoteControlManager {
	private static final String TAG = RemoteControlManager.class.getSimpleName();

	private RemoteControlServer remoteControlServer;
	private RemoteControlClient remoteClientClient;
	private UsbAccessoryManager usbAccessoryManager;
	private Handler handler;
	private final Context context;
	private int serverPort;
	private String host;
	private Thread rcServerWorkerThread;
	private Thread rcClientWorkerThread;

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
			serverPort = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServerTCPPort();
			host = ((ArduinoADK) RemoteControlManager.this.context.getApplicationContext()).getSettings().getRCServer();
			remoteClientClient.connect(host, serverPort);
		}

	}

	public RemoteControlManager(Context context, Handler messageHandler) {
		this.context = context;
		this.handler = messageHandler;
		this.remoteControlServer = new RemoteControlServer(usbAccessoryManager, handler);
		this.remoteClientClient = new RemoteControlClient(host, serverPort);
	}

	public void startServer() {
		if (rcServerWorkerThread == null) {
			rcServerWorkerThread = new Thread(new RCServerRunnable(), RemoteControlManager.class.getSimpleName() + "Thead");
			rcServerWorkerThread.start();
		}
	}

	public void startClient() {
		if (rcClientWorkerThread == null) {
			rcClientWorkerThread = new Thread(new RCClientRunnable(), RemoteControlManager.class.getSimpleName() + "Thead");
			rcClientWorkerThread.start();
		}
	}

	public boolean isServerStarted() {
		return rcServerWorkerThread != null && rcServerWorkerThread.isAlive();
	}

	public boolean isClientStarted() {
		return rcClientWorkerThread != null && rcClientWorkerThread.isAlive();
	}

	public void stopServer() {
		remoteControlServer.stop();
		if (rcServerWorkerThread != null) {
			rcServerWorkerThread.interrupt();
			rcServerWorkerThread = null;
		}
	}

	public void stopClient() {
		remoteClientClient.stop();
		if (rcClientWorkerThread != null) {
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

}
