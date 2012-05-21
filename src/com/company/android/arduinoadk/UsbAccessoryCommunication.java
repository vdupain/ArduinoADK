package com.company.android.arduinoadk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UsbAccessoryCommunication implements Runnable {
	private static final String TAG = "UsbAccessoryCommunication";

	private final FileInputStream inputStream;
	private final FileOutputStream outputStream;
	private final Handler handler;

	public UsbAccessoryCommunication(FileInputStream inputStream, FileOutputStream outputStream, Handler handler) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.handler = handler;
	}

	@Override
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;

		while (ret >= 0) { // read data
			try {
				ret = inputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;
				switch (buffer[i]) {
				case 0x6:
					if (len >= 3) {
						Message m = Message.obtain(handler, WhatAbout.TELEMETRIE.ordinal());
						// unsigned byte on arduino and signed in Java so...
						int degree = buffer[i + 1] & 0xFF;
						int distance = buffer[i + 2] & 0xFF;
						m.obj = new TelemetrieMessage(degree, distance);
						handler.sendMessage(m);
					}
					i += 3;
					break;

				default:
					Log.d(TAG, "Unknown msg: " + buffer[i]);
					i = len;
					break;
				}
			}
		}
	}

	public void sendCommand(int command, int target, int value) {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = (byte) command;
		buffer[1] = (byte) target;
		buffer[2] = (byte) value;
		if (outputStream != null && buffer[1] != -1) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

}
