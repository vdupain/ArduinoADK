package com.company.android.arduinoadk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

public class LocalService extends Service {

	private static final String TAG = "LocalService";
	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.rcserver_service_started;
	private Thread rcServer;

	/** For showing and hiding our notification. */
	NotificationManager mNM;
	private UsbAccessoryManager usbAccessoryManager;

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
		usbAccessoryManager = new UsbAccessoryManager(this.getApplicationContext(), null);
		usbAccessoryManager.setupAccessory(null);
		usbAccessoryManager.reOpenAccessory();

		rcServer = new RemoteControlServer(this.usbAccessoryManager, 12345, null);
		rcServer.start();
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		// Tell the user we stopped.
		Toast.makeText(this, R.string.rcserver_service_stopped, Toast.LENGTH_SHORT).show();
		
		usbAccessoryManager.closeUsbAccessory();
		usbAccessoryManager.unregisterReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		CharSequence text = getText(R.string.rcserver_service_started);

		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ArduinoADKMainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.rcserver_service_label), text, contentIntent);

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(NOTIFICATION, notification);
	}

}
