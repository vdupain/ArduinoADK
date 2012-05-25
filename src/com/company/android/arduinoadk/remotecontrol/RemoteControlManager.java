package com.company.android.arduinoadk.remotecontrol;

import android.os.Messenger;

import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlManager {

	private RemoteControlServer rcServer;
	private UsbAccessoryManager usbAccessoryManager;
	private Messenger messenger;

	public RemoteControlManager() {
	}

	public RemoteControlManager(UsbAccessoryManager usbAccessoryManager) {
		this.usbAccessoryManager = usbAccessoryManager;
	}

	public void start() {
		rcServer = new RemoteControlServer(this.usbAccessoryManager, 12345);
		rcServer.setMessenger(messenger);
		rcServer.execute();
	}

	public boolean isStarted() {
		if (rcServer != null)
			return !rcServer.isCancelled();
		else
			return false;
	}

	public void stop() {
		if (rcServer != null) {
			rcServer.cancel(true);
			rcServer = null;
		}
	}

	public void setMessenger(Messenger messenger) {
		this.messenger = messenger;
	}

	public void setUsbAccessoryManager(UsbAccessoryManager usbAccessoryManager) {
		this.usbAccessoryManager = usbAccessoryManager;
	}

}
