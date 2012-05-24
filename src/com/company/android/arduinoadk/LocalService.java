package com.company.android.arduinoadk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

/**
 * This service is only used to contain the UsbAccessoryManager and
 * RemoteControlServer running in the background. To use it, you need to bind to
 * this service and get it from the binder.
 */
public class LocalService extends Service {

	private static final String TAG = LocalService.class.getSimpleName();

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;
	private RemoteControlServer rcServer;

	// Binder given to clients
	private final IBinder binder = new LocalServiceBinder();

	/** For showing and hiding our notification. */
	NotificationManager mNotificationManager;

	private UsbAccessoryManager usbAccessoryManager;
	private Messenger messenger;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalServiceBinder extends Binder {
		LocalService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return LocalService.this;
		}
	}

	@Override
	public void onCreate() {
		// The service is being created
		Log.d(TAG, "onCreate");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
		if (usbAccessoryManager == null) {
			usbAccessoryManager = new UsbAccessoryManager(this.getApplicationContext(), null);
		}
		usbAccessoryManager.setupAccessory(null);
		usbAccessoryManager.reOpenAccessory();
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		// Cancel the persistent notification.
		Log.d(TAG, "onDestroy");
		clearNotification();
		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();

		// getRcServer().cancel(true);
		// getRcServer().stopServer();

		usbAccessoryManager.closeUsbAccessory();
		usbAccessoryManager.unregisterReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "onStartCommand startId " + startId + ": intent " + intent);
		Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

		Bundle extras = intent.getExtras();
		if (extras != null) {
			messenger = (Messenger) extras.get("MESSENGER");
		}
		if (rcServer!=null) {
			rcServer.setMessenger(messenger);
		}
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		Log.d(TAG, "onBind");
		return this.binder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		CharSequence text = getText(R.string.local_service_started);
		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ArduinoADKMainActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.local_service_label), text, contentIntent);
		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNotificationManager.notify(NOTIFICATION, notification);
	}

	private void clearNotification() {
		mNotificationManager.cancel(NOTIFICATION);
	}

	public RemoteControlServer getRcServer() {
		return rcServer;
	}

	public void startRcServer() {
		rcServer = new RemoteControlServer(this.usbAccessoryManager, 12345);
		rcServer.setMessenger(messenger);
		rcServer.createServer();
		rcServer.execute();
	}

	public void stopRcServer() {
		if (rcServer != null) {
			rcServer.cancel(true);
			rcServer = null;
		}
	}
}
