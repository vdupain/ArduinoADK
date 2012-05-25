package com.company.android.arduinoadk.remotecontrol;

import android.os.Handler;

import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlManager {

	private RemoteControlHandlerThread rcServer;
	private UsbAccessoryManager usbAccessoryManager;
	private final Handler messageHandler;

	public RemoteControlManager(Handler messageHandler) {
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

}
