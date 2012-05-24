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
import android.widget.Toast;

import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

public class LocalService extends Service {

	private static final String TAG = "LocalService";
	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.rcserver_service_started;
	private RemoteControlServer rcServer;

	// Binder given to clients
	private final IBinder binder = new LocalServiceBinder();

	/** For showing and hiding our notification. */
	NotificationManager mNM;

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
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
		usbAccessoryManager = new UsbAccessoryManager(this.getApplicationContext(), null);
		usbAccessoryManager.setupAccessory(null);
		usbAccessoryManager.reOpenAccessory();

		this.rcServer = new RemoteControlServer(this.usbAccessoryManager, 12345, null);
		// rcServer.startServer();
		// rcServer.execute();
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		// Tell the user we stopped.
		Toast.makeText(this, R.string.rcserver_service_stopped, Toast.LENGTH_SHORT).show();

		//getRcServer().cancel(true);
		//getRcServer().stopServer();

		usbAccessoryManager.closeUsbAccessory();
		usbAccessoryManager.unregisterReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

		Bundle extras = intent.getExtras();
		if (extras != null) {
			messenger = (Messenger) extras.get("MESSENGER");
		}
		getRcServer().setMessenger(messenger);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		return this.binder;
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

	public RemoteControlServer getRcServer() {
		return rcServer;
	}

}
