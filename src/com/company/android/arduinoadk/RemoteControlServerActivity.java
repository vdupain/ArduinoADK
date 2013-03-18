package com.company.android.arduinoadk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;
import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService;
import com.company.android.arduinoadk.remotecontrol.RemoteControlServerService.RemoteControlServerBinder;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;
import com.company.android.arduinoadk.usb.UsbAccessoryService;
import com.company.android.arduinoadk.usb.UsbAccessoryService.UsbAccessoryBinder;

import java.util.List;

public class RemoteControlServerActivity extends BaseActivity implements ServiceConnected, OnCheckedChangeListener {
	private static final String TAG = RemoteControlServerActivity.class.getSimpleName();

	private RemoteControlController controller;
	private Switch switchRCServer;

	/** Defines callbacks for service binding, passed to bindService() */
	private ArduinoADKServiceConnection usbServiceConnection = new ArduinoADKServiceConnection(this);
	private ArduinoADKServiceConnection remoteControlServiceConnection = new ArduinoADKServiceConnection(this);

	public static  UsbAccessoryManager usbAccessoryManager;
	private RemoteControlServerService remoteControlServerService;

	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (WhatAbout.values()[msg.what]) {
			case SERVER_LOG:
				controller.logConsole("" + msg.obj);
				break;
			default:
				break;
			}
		}
	};
	private final BroadcastReceiver remoteControlServerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (MyIntent.ACTION_SERVER_STARTED.equals(action)) {
				handleServerStart();
			} else if (MyIntent.ACTION_SERVER_STOPPED.equals(action)) {
				handleServerStop();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcserver_main);
		RemoteControlServerFragment fragment = (RemoteControlServerFragment) getFragmentManager().findFragmentById(R.id.remoteControlServerFragment);
		switchRCServer = (Switch) fragment.getView().findViewById(R.id.switchRCServer);
		switchRCServer.setOnCheckedChangeListener(this);
		initController();
		createServices();

		try {
			controller.logConsole("<b>" + ArduinoADK.class.getSimpleName() + " v"
					+ this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + "</b>");
		} catch (NameNotFoundException e) {
			controller.logConsole("<b>" + ArduinoADK.class.getSimpleName() + "</b>");
		}

		if (this.getArduinoADKApplication().getSettings().isRCServerAutoStart())
			switchRCServer.setChecked(true);

	}

	private void handleServerStart() {
		controller.logConsole("RC Server started...");
	}

	private void handleServerStop() {
		controller.logConsole("RC Server stopped...");
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MyIntent.ACTION_SERVER_STARTED);
		filter.addAction(MyIntent.ACTION_SERVER_STOPPED);
		registerReceiver(remoteControlServerReceiver, filter);
		doBindServices();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		controller.usbAccessoryDetached();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(remoteControlServerReceiver);
		doUnbindServices();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void createServices() {
		Log.d(TAG, "createServices");
		startService(new Intent(this, UsbAccessoryService.class));
	}

	private void doBindServices() {
		Log.d(TAG, "bindServices");
		// Bind from the service
		doBindUsbAccessoryService();
		doBindRemoteControlServerService();
	}

	private void doBindRemoteControlServerService() {
		Intent intent = new Intent(this, RemoteControlServerService.class);
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		boolean success = bindService(intent, remoteControlServiceConnection, 0);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + RemoteControlServerService.class.getSimpleName());
		}
	}

	private void doBindUsbAccessoryService() {
		boolean success = bindService(new Intent(this, UsbAccessoryService.class), usbServiceConnection, Context.BIND_AUTO_CREATE);
		if (!success) {
			Log.e(TAG, "Failed to bind to " + UsbAccessoryService.class.getSimpleName());
		}
	}

	/**
	 * Disconnects from the local service.
	 */
	private void doUnbindServices() {
		Log.d(TAG, "doUnbindServices");
		// Detach our existing connection
		doUnbindRemoteControlServerService();
		doUnbindUsbAccessoryService();
	}

	private void doUnbindRemoteControlServerService() {
		if (isBoundToRemoteControlServerService()) {
			unbindService(remoteControlServiceConnection);
			this.remoteControlServerService = null;
		}
	}

	private void doUnbindUsbAccessoryService() {
		if (isBoundToUsbAccessoryManager()) {
			unbindService(usbServiceConnection);
			this.usbAccessoryManager = null;
		}
	}

	private void stopServices() {
		stopService(new Intent(this, RemoteControlServerService.class));
		stopService(new Intent(this, UsbAccessoryService.class));
	}

	private boolean isBoundToRemoteControlServerService() {
		return this.remoteControlServerService != null;
	}

	private boolean isBoundToUsbAccessoryManager() {
		return this.usbAccessoryManager != null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switchRCServer:
			if (buttonView.isChecked()) {
				startService(new Intent(this, RemoteControlServerService.class));
				doBindRemoteControlServerService();
                startIPWebcam();
			} else {
				doUnbindRemoteControlServerService();
				stopService(new Intent(this, RemoteControlServerService.class));
			}
			break;
		}
	}

    private void startIPWebcam() {
        /*
        Intent launcher = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        Intent ipwebcam =
                new Intent()
                        .setClassName("com.pas.webcam", "com.pas.webcam.Rolling")
                        .putExtra("hidebtn1", true)                // Hide help button
                        .putExtra("caption2", "Run in background") // Change caption on "Actions..."
                        .putExtra("intent2", launcher)             // And give button another purpose
                        .putExtra("returnto", new Intent().setClassName(RemoteControlServerActivity.this,RemoteControlServerActivity.class.getName())); // Set activity to return to
        */
        Intent ipwebcam = new Intent().setClassName("com.pas.webcam", "com.pas.webcam.Rolling");
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(ipwebcam, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            String errorMessage = RemoteControlServerActivity.this.getResources().getString(R.string.error_no_ipwebcam);
            //Logger.e(errorMessage);
            Toast.makeText(RemoteControlServerActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            return;
        }
        ipwebcam.putExtra("hidebtn1", true);
        startActivityForResult(ipwebcam, 1);
    }

    @Override
	public Object onRetainNonConfigurationInstance() {
		if (usbAccessoryManager != null) {
			return usbAccessoryManager;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}

	@Override
	void onQuit() {
		switchRCServer.setChecked(false);
		doUnbindServices();
		stopServices();
	}

	@Override
	void initController() {
		controller = new RemoteControlController(this);
		controller.usbAccessoryAttached();
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
				ArduinoManager arduinoHandlerThread = new ArduinoManager(usbAccessoryManager);
				arduinoHandlerThread.setHandler(handler);
				Thread thread = new Thread(null, arduinoHandlerThread, "arduinoHandlerThread");
				thread.start();
			}
		} else if (binder instanceof RemoteControlServerBinder) {
			remoteControlServerService = ((RemoteControlServerBinder) binder).getService();
            remoteControlServerService.setUsbAccessoryManager(usbAccessoryManager);
            remoteControlServerService.setHandler(this.handler);

			switchRCServer.setChecked(remoteControlServerService.isRunning());
			controller.displayIP();
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

	public RemoteControlServerService getRemoteControlServerService() {
		return remoteControlServerService;
	}

}