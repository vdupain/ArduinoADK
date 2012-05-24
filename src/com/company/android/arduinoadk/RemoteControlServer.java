package com.company.android.arduinoadk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.company.android.arduinoadk.libarduino.ArduinoManager;
import com.company.android.arduinoadk.libusb.UsbAccessoryManager;

/**
 * 
 * Server implementation. One client handled at a time only
 * 
 */

public class RemoteControlServer extends AsyncTask<Void, String, Void> {
	private final String TAG = "RemoteControlServer";

	// private Handler handler;
	private int port;
	private ServerSocket server;
	private Socket client;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer = new byte[4096];
	private String request, response;

	private ArduinoManager arduinoManager;
	private ControllStick controllStick = new ControllStick();

	private Messenger messenger;

	public RemoteControlServer() {
		super();
	}

	public RemoteControlServer(UsbAccessoryManager usbAccessoryCommunication, int port, Handler handler) {
		this();
		this.arduinoManager = new ArduinoManager(usbAccessoryCommunication);
		this.port = port;
		// this.handler = handler;
	}

	public void startServer() {
		try {
			server = new ServerSocket(getPort());
			log("Remote Control Server started...");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
		}
		log("Accept client connection...");
	}

	public void stopServer() {
		try {
			server.close();
			server = null;
			log("Remote Control Server stopped...");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
		} finally {
			this.arduinoManager.sendSafeStickCommand();
		}
	}

	private boolean handleClient() {
		if (server == null || (server != null && server.isClosed()))
			return false;
		int len = 0;
		try {
			client = server.accept();
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
			return true;
		} finally {
			this.arduinoManager.sendSafeStickCommand();
		}

		log("Connection from " + getClientAddress().getHostAddress());

		while (true) {
			if (isCancelled())
				return false;
			;
			try {
				len = inputStream.read(buffer, 0, buffer.length);
			} catch (IOException e) {
				break;
			}
			if (len <= 0)
				break;

			request = new String(buffer, 0, len);
			Log.d(TAG, request);
			if (request.startsWith("STICK"))
				commandStick();
			else if (request.startsWith("HELP"))
				commandHelp();
			else if (request.startsWith("QUIT")) {
				commandQuit();
				break;
			} else
				commandUnknown();
		}

		// Inform the UI Thread that communication has stopped
		// handler.obtainMessage(WhatAbout.SERVER_STOP.ordinal()).sendToTarget();
		try {
			client.close();
			log("Client disconnected");
		} catch (IOException e) {
			return true;
		} finally {
			this.arduinoManager.sendSafeStickCommand();
		}
		return true;
	}

	private void commandStick() {
		double actualX = Double.parseDouble(request.substring(request.indexOf("x=") + 2, request.indexOf(":y=")));
		double actualY = Double.parseDouble(request.substring(request.indexOf("y=") + 2, request.indexOf("\n")));
		controllStick.setX(actualX).setY(actualY);
		this.arduinoManager.sendStickCommand(controllStick);
		log(this.getClientAddress().getHostAddress() + " - " + controllStick.toString());
	}

	private void commandHelp() {
	}

	private void commandQuit() {
		writeContent("Command Quit\r\n");
	}

	private void commandUnknown() {
		writeContent("Command unknown: " + request + "\r\n");
	}

	private void writeContent(String requestContent) {
		Log.d(TAG, requestContent);
		try {
			outputStream.write(requestContent.getBytes(), 0, requestContent.length());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return Returns local address
	 */
	private String getServerAddress() {
		return client.getLocalAddress().getHostAddress();
	}

	/**
	 * 
	 * @return Returns client address
	 */
	private InetAddress getClientAddress() {
		return client.getInetAddress();
	}

	/**
	 * 
	 * @param msg
	 *            String to send to the UI Thread
	 */
	private void log(String msg) {
		Log.d(TAG, msg);
		publishProgress(msg);
		// handler.obtainMessage(WhatAbout.SERVER_LOG.ordinal(),
		// msg).sendToTarget();
	}

	public int getPort() {
		return port;
	}

	@Override
	protected Void doInBackground(Void... params) {
		while (handleClient()) {
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		if (messenger != null) {
			Message message = Message.obtain();
			message.obj = values[0];
			message.what = WhatAbout.SERVER_LOG.ordinal();
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		super.onProgressUpdate(values);
	}

	public void setMessenger(Messenger messenger) {
		this.messenger = messenger;
	}

}
