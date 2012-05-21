package com.company.android.arduinoadk;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ArduinoADKActivity extends Activity {
	private static final String TAG = "ArduinoADKActivity";
	public static final int DEFAULT_PORT = 12345;

	private static final String ACTION_USB_PERMISSION = "com.company.android.arduinoadk.USB_PERMISSION";

	private UsbManager usbManager;
	private PendingIntent permissionIntent;
	private boolean permissionRequestPending;

	private UsbAccessory usbAccessory;
	private ParcelFileDescriptor fileDescriptor;
	protected FileInputStream inputStream;
	protected FileOutputStream outputStream;
	protected UsbAccessoryCommunication usbAccessoryCommunication;

	private TelemetrieController telemetrieController;
	private ServerController serverController;

	private PowerManager.WakeLock wakeLock;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case TELEMETRIE:
				handleTelemetrieMessage((TelemetrieMessage) msg.obj);
				break;
			case SERVER_LOG:
				logServerConsole((String) msg.obj);
				break;
			case SERVER_START:
				break;
			case SERVER_STOP:
				break;
			}
		}
	};

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "Permission denied for USB accessory " + accessory);
					}
					permissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(usbAccessory)) {
					closeAccessory();
				}
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupAccessory();
		showControls();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.company.android.arduinoadk.wakelock");

		// Print version number
		/*
		 * try { logConsole("<b>ArduinoADK v" +
		 * this.getPackageManager().getPackageInfo(this.getPackageName(),
		 * 0).versionName + "</b>"); } catch (NameNotFoundException e) {
		 * logConsole("<b>ArduinoADK</b>"); }
		 */

	}

	private void setupAccessory() {
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		if (getLastNonConfigurationInstance() != null) {
			usbAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(usbAccessory);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (usbAccessory != null) {
			return usbAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		wakeLock.acquire();

		Intent intent = getIntent();
		if (inputStream != null && outputStream != null) {
			return;
		}

		UsbAccessory[] accessories = usbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (usbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!permissionRequestPending) {
						usbManager.requestPermission(accessory, permissionIntent);
						permissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "USB Accessory is null");
		}

		this.serverController.displayIP();

	}

	@Override
	public void onPause() {
		super.onPause();
		closeAccessory();
		wakeLock.release();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	private void openAccessory(UsbAccessory accessory) {
		Log.d(TAG, "openAccessory: " + accessory);
		fileDescriptor = usbManager.openAccessory(accessory);
		if (fileDescriptor != null) {
			usbAccessory = accessory;
			FileDescriptor fd = fileDescriptor.getFileDescriptor();
			inputStream = new FileInputStream(fd);
			outputStream = new FileOutputStream(fd);
			usbAccessoryCommunication = new UsbAccessoryCommunication(inputStream, outputStream, handler);
			Thread thread = new Thread(null, usbAccessoryCommunication, "UsbAccessoryThread");
			thread.start();
			Log.d(TAG, "USB Accessory opened");
		} else {
			Log.d(TAG, "USB Accessory open fail");
		}
	}

	private void closeAccessory() {
		try {
			if (fileDescriptor != null) {
				fileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			fileDescriptor = null;
			usbAccessory = null;
		}
		telemetrieController.usbAccessoryDetached();
		serverController.usbAccessoryDetached();
	}

	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit:
			quit();
			return true;
		case R.id.test:
			TestUtils.test(handler);
			return true;
		case R.id.settings:
			Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.abouthelp:
			Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void quit() {
		finish();
		System.exit(0);
	}

	public void logServerConsole(String message) {
		serverController.logConsole(message);
	}

	protected void showControls() {
		setContentView(R.layout.main);
		telemetrieController = new TelemetrieController(this);
		telemetrieController.usbAccessoryAttached();

		serverController = new ServerController(this);
		serverController.usbAccessoryAttached();
	}

	private void handleTelemetrieMessage(TelemetrieMessage message) {
		telemetrieController.setRadarPosition(message.getDegree(), message.getDistance());
	}

	public UsbAccessoryCommunication getUsbAccessoryCommunication() {
		return usbAccessoryCommunication;
	}

}