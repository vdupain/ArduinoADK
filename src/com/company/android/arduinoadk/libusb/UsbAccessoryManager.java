package com.company.android.arduinoadk.libusb;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.company.android.arduinoadk.ArduinoMessage;
import com.company.android.arduinoadk.WhatAbout;

public class UsbAccessoryManager implements Runnable {
	private static final String TAG = "UsbAccessoryCommunication";

	private static final String ACTION_USB_PERMISSION = "com.company.android.arduinoadk.USB_PERMISSION";

	private final Handler handler;

	private UsbManager usbManager;
	private PendingIntent permissionIntent;
	private boolean permissionRequestPending;
	private UsbAccessory usbAccessory;
	private ParcelFileDescriptor fileDescriptor;
	private FileInputStream inputStream;
	private FileOutputStream outputStream;

	private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openUsbAccessory(accessory);
					} else {
						Log.d(TAG, "Permission denied for USB accessory " + accessory);
					}
					permissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(usbAccessory)) {
					closeUsbAccessory();
				}
			}
		}
	};

	private final Context context;

	public UsbAccessoryManager(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void setupAccessory(UsbAccessory usbAccessoryRetainInstance) {
		Log.d(TAG, "setupAccessory: " + usbAccessoryRetainInstance);
		usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
		permissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		this.context.registerReceiver(this.usbReceiver, filter);
		if (usbAccessoryRetainInstance != null) {
			this.usbAccessory = usbAccessoryRetainInstance;
			openUsbAccessory(this.usbAccessory);
		}
	}

	private void openUsbAccessory(UsbAccessory accessory) {
		Log.d(TAG, "openUsbAccessory: " + accessory);
		fileDescriptor = usbManager.openAccessory(accessory);
		if (fileDescriptor != null) {
			usbAccessory = accessory;
			FileDescriptor fd = fileDescriptor.getFileDescriptor();
			inputStream = new FileInputStream(fd);
			outputStream = new FileOutputStream(fd);
			
			// FIXME pour le moment, pas de rŽception depuis l'Arduino
			//Thread thread = new Thread(null, this, "UsbAccessoryThread");
			//thread.start();

			Log.d(TAG, "USB Accessory opened");
		} else {
			Log.d(TAG, "USB Accessory open fail");
		}
	}

	public void reOpenAccessory() {
		Log.d(TAG, "reOpenAccessory");
		if (inputStream != null && outputStream != null) {
			return;
		}
		UsbAccessory[] accessories = usbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (usbManager.hasPermission(accessory)) {
				openUsbAccessory(accessory);
			} else {
				synchronized (usbReceiver) {
					if (!permissionRequestPending) {
						usbManager.requestPermission(accessory, permissionIntent);
						permissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "USB Accessory is null");
		}
	}

	public void closeUsbAccessory() {
		Log.d(TAG, "closeUsbAccessory");
		try {
			if (fileDescriptor != null) {
				fileDescriptor.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			fileDescriptor = null;
			usbAccessory = null;
		}
	}

	public void unregisterReceiver() {
		this.context.unregisterReceiver(this.usbReceiver);
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
				// command
				switch (buffer[i]) {
				case 0x6:
					if (len >= 3) {
						Message m = Message.obtain(handler, WhatAbout.TELEMETRY.ordinal());
						// unsigned byte on arduino and signed in Java so...
						int degree = buffer[i + 1] & 0xFF;
						int distance = buffer[i + 2] & 0xFF;
						m.obj = new ArduinoMessage(degree, distance);
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

	/**
	 * 
	 * @param command
	 * @param target
	 * @param value
	 */
	public void sendCommand(byte command, byte target, int value) {
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

	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}

	public UsbAccessory getUsbAccessory() {
		return usbAccessory;
	}

}
