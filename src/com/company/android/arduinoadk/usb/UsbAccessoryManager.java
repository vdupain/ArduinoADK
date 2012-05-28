package com.company.android.arduinoadk.usb;

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
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

public class UsbAccessoryManager {
	private static final String TAG = UsbAccessoryManager.class.getSimpleName();

	private static final String ACTION_USB_PERMISSION = "com.company.android.arduinoadk.USB_PERMISSION";

	private UsbManager usbManager;
	private PendingIntent permissionIntent;
	private boolean permissionRequestPending;
	private UsbAccessory usbAccessory;
	private ParcelFileDescriptor fileDescriptor;
	private FileInputStream inputStream;
	private FileOutputStream outputStream;

	private final Context context;

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

	public UsbAccessoryManager(Context context) {
		this.context = context;
	}

	public void setupUsbAccessory() {
		Log.d(TAG, "setupAccessory");
		usbManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
		permissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		this.context.registerReceiver(this.usbReceiver, filter);
	}

	private boolean openUsbAccessory(UsbAccessory accessory) {
		Log.d(TAG, "openUsbAccessory: " + accessory);
		fileDescriptor = usbManager.openAccessory(accessory);
		if (fileDescriptor != null) {
			usbAccessory = accessory;
			FileDescriptor fd = fileDescriptor.getFileDescriptor();
			inputStream = new FileInputStream(fd);
			outputStream = new FileOutputStream(fd);
			Log.d(TAG, "USB Accessory opened");
			Toast.makeText(this.context, "openUsbAccessory", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			Log.d(TAG, "USB Accessory open fail");
		}
		return false;
	}

	public void openUsbAccessory() {
		Log.d(TAG, "openUsbAccessory");
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
			Toast.makeText(this.context, "closeUsbAccessory", Toast.LENGTH_SHORT).show();
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

	public FileInputStream getInputStream() {
		return inputStream;
	}

	public FileOutputStream getOutputStream() {
		return outputStream;
	}
}
