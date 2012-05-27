package com.company.android.arduinoadk.usb;

public class HandshakeMessage {
	public byte byte1;
	public byte byte2;
	public byte byte3;

	public HandshakeMessage(byte byte1, byte byte2, byte byte3) {
		this.byte1 = byte1;
		this.byte2 = byte2;
		this.byte3 = byte3;
	}
}
