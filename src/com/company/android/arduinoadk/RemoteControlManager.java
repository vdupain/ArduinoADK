package com.company.android.arduinoadk;

import android.os.Messenger;

import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

public class RemoteControlManager {

	private RemoteControlServer rcServer;
	private final UsbAccessoryManager usbAccessoryManager;
	private Messenger messenger;

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

}
