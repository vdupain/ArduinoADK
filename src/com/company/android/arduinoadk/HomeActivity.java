package com.company.android.arduinoadk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.company.android.arduinoadk.SimpleGestureFilter.SimpleGestureListener;
import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlService.RemoteControlBinder;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;
import com.company.android.arduinoadk.usb.UsbAccessoryService;
import com.company.android.arduinoadk.usb.UsbAccessoryService.UsbAccessoryBinder;

public class HomeActivity extends BaseActivity implements ServiceConnected, OnCheckedChangeListener, SimpleGestureListener {
	private static final String TAG = HomeActivity.class.getSimpleName();

	private SimpleGestureFilter detector;

	public RemoteControlController rcServerController;
	private Switch switchRcServer;

	/** Defines callbacks for service binding, passed to bindService() */
	private ArduinoADKServiceConnection usbServiceConnection = new ArduinoADKServiceConnection(this);
	private ArduinoADKServiceConnection remoteControlServiceConnection = new ArduinoADKServiceConnection(this);

	private RemoteControlManager remoteControlManager;
	private UsbAccessoryManager usbAccessoryManager;

	private PowerManager.WakeLock wakeLock;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_main);
		RCServerFragment fragment = (RCServerFragment) getFragmentManager().findFragmentById(R.id.rcServerFragment);
		switchRcServer = (Switch) fragment.getView().findViewById(R.id.switchRCServer);

		initControllers();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// Create a wake lock
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass().getName());

		detector = new SimpleGestureFilter(this, this);
		createServices();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		doBindServices();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		// when the activity is resumed, we acquire a wake-lock so that the
		// screen stays on
		wakeLock.acquire();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		rcServerController.usbAccessoryDetached();

		// and release our wake-lock
		wakeLock.release();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		doUnbindServices();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	private void createServices() {
		Log.d(TAG, "createServices");
		startService(new Intent(this, UsbAccessoryService.class));
		startService(new Intent(this, RemoteControlService.class));
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		boolean success = bindService(new Intent(this, UsbAccessoryService.class), usbServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to UsbAccessoryService");
		}
		success = bindService(new Intent(this, RemoteControlService.class), remoteControlServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to RemoteControlService");
		}
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		if (isBoundToRemoteControlManager()) {
			unbindService(remoteControlServiceConnection);
			this.remoteControlManager = null;
		}
		if (isBoundToUsbAccessoryManager()) {
			unbindService(usbServiceConnection);
			this.usbAccessoryManager = null;
		}
	}

	private void stopServices() {
		stopService(new Intent(this, RemoteControlService.class));
		stopService(new Intent(this, UsbAccessoryService.class));
	}

	private boolean isBoundToRemoteControlManager() {
		return this.remoteControlManager != null;
	}

	private boolean isBoundToUsbAccessoryManager() {
		return this.usbAccessoryManager != null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				remoteControlManager.start();
			} else {
				remoteControlManager.stop();
			}
			break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (usbAccessoryManager != null) {
			return usbAccessoryManager;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	void quit() {
		doUnbindServices();
		stopServices();
		moveTaskToBack(true);
	}

	public void logConsole(String message) {
		rcServerController.logConsole(message);
	}

	private void initControllers() {
		rcServerController = new RemoteControlController(this);
		rcServerController.usbAccessoryAttached();
	}

	@Override
	public void onConnected(IBinder binder) {
		Log.d(TAG, "onConnected");
		if (binder == null) {
			Log.e(TAG, "Failed to get binder");
			return;
		}

		// switch between the different services
		if (binder instanceof UsbAccessoryBinder) {
			usbAccessoryManager = ((UsbAccessoryBinder) binder).getUsbAccessoryManager();
			if (usbAccessoryManager.isOpened()) {
				ArduinoManager arduinoHandlerThread = new ArduinoManager(usbAccessoryManager, null);
				Thread thread = new Thread(null, arduinoHandlerThread, "arduinoHandlerThread");
				thread.start();
			}
		} else if (binder instanceof RemoteControlBinder) {
			RemoteControlBinder b = (RemoteControlBinder) binder;
			remoteControlManager = b.getRCServerManager();
			if (usbAccessoryManager != null)
				remoteControlManager.setUsbAccessoryManager(this.usbAccessoryManager);
			if (((ArduinoADK) getApplicationContext()).getSettings().isRCServerAutoStart()) {
				remoteControlManager.start();
			}
			switchRcServer.setChecked(remoteControlManager.isStarted());
			switchRcServer.setOnCheckedChangeListener(HomeActivity.this);
			b.getService().setActivity(this);
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

	public RemoteControlManager getRemoteControlManager() {
		return remoteControlManager;
	}

	@Override
	public void onSwipe(int direction) {
		String str = "";
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT:
			str = "Swipe Right";
			// setRCServerContainerVisible();
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			str = "Swipe Left";
			// setArduinoContainerVisible();
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
	public boolean dispatchTouchEvent(MotionEvent ev) {
		this.detector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

}