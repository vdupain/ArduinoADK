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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.company.android.arduinoadk.SimpleGestureFilter.SimpleGestureListener;
import com.company.android.arduinoadk.libusb.UsbAccessoryCommunication;

public class ArduinoADKActivity extends Activity implements SimpleGestureListener {
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

	private ArduinoController arduinoController;
	private RemoteControlServerController serverController;

	private PowerManager.WakeLock wakeLock;
	private SimpleGestureFilter detector;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case TELEMETRY:
				handleTelemetryMessage((ArduinoMessage) msg.obj);
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

		setContentView(R.layout.main);
		findViewById(R.id.container1).setVisibility(View.VISIBLE);
		findViewById(R.id.container2).setVisibility(View.GONE);

		// setupActionBarForTabs();

		initControllers();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.company.android.arduinoadk.wakelock");

		detector = new SimpleGestureFilter(this, this);
	}

	/*
	 * private void setupActionBarForTabs() { ActionBar actionBar =
	 * getActionBar();
	 * actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	 * actionBar.setDisplayShowTitleEnabled(true);
	 * 
	 * Tab tab = actionBar.newTab().setText(R.string.arduino);
	 * tab.setTabListener(new MyTabListener<ArduinoFragment>(this, "arduino",
	 * ArduinoFragment.class)); actionBar.addTab(tab); tab =
	 * actionBar.newTab().setText(R.string.server); tab.setTabListener(new
	 * MyTabListener<ServerFragment>(this, "server", ServerFragment.class));
	 * actionBar.addTab(tab); }
	 */

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
		arduinoController.usbAccessoryDetached();
		serverController.usbAccessoryDetached();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rcserver:
			findViewById(R.id.container1).setVisibility(View.VISIBLE);
			findViewById(R.id.container2).setVisibility(View.GONE);
			return true;
		case R.id.menu_arduino:
			findViewById(R.id.container1).setVisibility(View.GONE);
			findViewById(R.id.container2).setVisibility(View.VISIBLE);
			return true;
		case R.id.menu_quit:
			quit();
			return true;
		case R.id.menu_test:
			// TestUtils.test(handler);
			startService(new Intent(this, RemoteControlService.class));
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_abouthelp:
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

	protected void initControllers() {
		arduinoController = new ArduinoController(this);
		arduinoController.usbAccessoryAttached();

		serverController = new RemoteControlServerController(this);
		serverController.usbAccessoryAttached();
	}

	private void handleTelemetryMessage(ArduinoMessage message) {
		arduinoController.setRadarPosition(message.getDegree(), message.getDistance());
	}

	public UsbAccessoryCommunication getUsbAccessoryCommunication() {
		return usbAccessoryCommunication;
	}

	@Override
	public void onSwipe(int direction) {
		String str = "";
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT:
			str = "Swipe Right";
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			str = "Swipe Left";
			break;
		case SimpleGestureFilter.SWIPE_DOWN:
			str = "Swipe Down";
			break;
		case SimpleGestureFilter.SWIPE_UP:
			str = "Swipe Up";
			break;
		}
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDoubleTap() {
		Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

}