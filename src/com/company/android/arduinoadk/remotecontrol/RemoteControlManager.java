package com.company.android.arduinoadk.remotecontrol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlManager {
	private static final String TAG = RemoteControlManager.class.getSimpleName();

	private RemoteControlHandlerThread rcServer;
	private UsbAccessoryManager usbAccessoryManager;
	private final Handler messageHandler;
	private final Context context;

	public RemoteControlManager(Context context, Handler messageHandler) {
		this.context = context;
		this.messageHandler = messageHandler;
	}

	public void start() {
		rcServer = new RemoteControlHandlerThread(usbAccessoryManager, messageHandler, 12345);
		rcServer.createServer();
		rcServer.start();
	}

	public boolean isStarted() {
		if (rcServer != null)
			return !rcServer.isCancelled();
		else
			return false;
	}

	public void stop() {
		if (rcServer != null) {
			rcServer.stopServer();
			rcServer = null;
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
				s.append(":" + rcServer.getPort() + "/");
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
