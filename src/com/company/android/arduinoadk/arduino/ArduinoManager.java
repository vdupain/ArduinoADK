package com.company.android.arduinoadk.arduino;

import com.company.android.arduinoadk.ControllStick;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class ArduinoManager {

	private final UsbAccessoryManager usbAccessoryManager;

	public ArduinoManager(UsbAccessoryManager usbAccessoryManager) {
		this.usbAccessoryManager = usbAccessoryManager;
	}

	public void sendSafeStickCommand() {
		this.sendStickCommand(new ControllStick(0.5, 0.5));
	}

	public void sendStickCommand(ControllStick controllStick) {
		byte x = (byte) (controllStick.getX() * 255);
		byte y = (byte) (controllStick.getY() * 255);
		usbAccessoryManager.sendCommand((byte) 2, (byte) 0x1, x);
		usbAccessoryManager.sendCommand((byte) 2, (byte) 0x2, y);
	}

}
