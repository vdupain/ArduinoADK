package com.company.android.arduinoadk.libarduino;

import com.company.android.arduinoadk.ControllStick;
import com.company.android.arduinoadk.libusb.UsbAccessoryCommunication;

public class ArduinoManager {

	private final UsbAccessoryCommunication usbAccessoryCommunication;

	public ArduinoManager(UsbAccessoryCommunication usbAccessoryCommunication) {
		this.usbAccessoryCommunication = usbAccessoryCommunication;
	}

	public void sendSafeStickCommand() {
		this.sendStickCommand(new ControllStick(0.5, 0.5));
	}

	public void sendStickCommand(ControllStick controllStick) {
		byte x = (byte) (controllStick.getX() * 255);
		byte y = (byte) (controllStick.getY() * 255);
		if (usbAccessoryCommunication != null) {
			usbAccessoryCommunication.sendCommand(ArduinoCommand.SERVO.ordinal(), ArduinoTarget.DIRECTION_SERVO.ordinal(), x);
			usbAccessoryCommunication.sendCommand(ArduinoCommand.SERVO.ordinal(), ArduinoTarget.MOTOR_SERVO.ordinal(), y);
		}
	}

}
