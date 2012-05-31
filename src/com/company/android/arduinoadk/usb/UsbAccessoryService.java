package com.company.android.arduinoadk.usb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.company.android.arduinoadk.MainActivity;
import com.company.android.arduinoadk.R;

/**
 * This service is only used to contain the UsbAccessoryManager running in the
 * background. To use it, you need to bind to this service and get it from the
 * binder.
 */
public class UsbAccessoryService extends Service {

	private static final String TAG = UsbAccessoryService.class.getSimpleName();

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_started;

	// Binder given to clients
	private final IBinder binder = new UsbAccessoryBinder();

	/** For showing and hiding our notification. */
	private NotificationManager notificationManager;

	private UsbAccessoryManager usbAccessoryManager;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class UsbAccessoryBinder extends Binder {
		public UsbAccessoryService getService() {
			return UsbAccessoryService.this;
		}

		public UsbAccessoryManager getUsbAccessoryManager() {
			return UsbAccessoryService.this.usbAccessoryManager;
		}
	}

	@Override
	public void onCreate() {
		// The service is being created
		Log.d(TAG, "onCreate");

		if (usbAccessoryManager == null) {
			usbAccessoryManager = new UsbAccessoryManager(this.getApplicationContext());
		}
		usbAccessoryManager.setupUsbAccessory();
		usbAccessoryManager.openUsbAccessory();
		// FIXME
		// usbAccessoryManager.start();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(TAG, "onDestroy");
		// Cancel the persistent notification.
		clearNotification();
		usbAccessoryManager.closeUsbAccessory();
		usbAccessoryManager.unregisterReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "onStartCommand startId " + startId + ": intent " + intent);
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
		Log.d(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return true;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		CharSequence text = getText(R.string.service_started);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		Notification notification = new Notification.Builder(this).setTicker(text).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(text).setContentTitle(getText(R.string.service_label)).setContentIntent(contentIntent).getNotification();
		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		notificationManager.notify(NOTIFICATION, notification);
	}

	private void clearNotification() {
		notificationManager.cancel(NOTIFICATION);
	}
}
