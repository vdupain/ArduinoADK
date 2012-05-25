package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.company.android.arduinoadk.ControllStick;
import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

/**
 * 
 * Server implementation. One client handled at a time only
 * 
 */

public class RemoteControlServer extends AsyncTask<Void, String, Void> {
	private final String TAG = "RemoteControlServer";

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

	public RemoteControlServer(UsbAccessoryManager usbAccessoryCommunication, int port) {
		this();
		this.arduinoManager = new ArduinoManager(usbAccessoryCommunication);
		this.port = port;
	}

	private void createServer() {
		Log.d(TAG, "createServer");
		try {
			server = new ServerSocket(getPort());
			server.setSoTimeout(1000);
			log("RC Server started...");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
		}
		log("Accept client connection...");
	}

	@Override
	protected void onPostExecute(Void result) {
		Log.d(TAG, "onPostExecute");
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "onPreExecute");
		this.createServer();
		super.onPreExecute();
	}

	@Override
	protected void onCancelled(Void result) {
		Log.d(TAG, "onCancelled");
		stopServer();
	}

	private void stopServer() {
		Log.d(TAG, "stopServer");
		if (server == null)
			return;
		try {
			server.close();
			server = null;
			Log.d(TAG, "RC Server stopped...");
			sendMessage("RC Server stopped...");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
		} finally {
			this.arduinoManager.sendSafeStickCommand();
		}
	}

	private boolean handleClient() {
		//Log.d(TAG, "handleClient");
		if (isCancelled() || server == null)
			return false;
		int len = 0;
		try {
			log("Waits for " +  server.getSoTimeout() + "ms an incoming request...");
			client = server.accept();
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();
			log("Connection from " + getClientAddress().getHostAddress());
			while (!isCancelled()) {
				Log.d(TAG, "While handleClient:" + isCancelled());
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
		} catch (SocketTimeoutException ignored) {
			// FIXME
			// Log.d(TAG, "No Client connection");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
			return true;
		} finally {
			if (client != null) {
				try {
					client.close();
					client = null;
					log("Client disconnected");
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
					log(e.getMessage());
				}
			}
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
	}

	public int getPort() {
		return port;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "doInBackground");
		while (!isCancelled() && handleClient())
			;
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		sendMessage(values[0]);
	}

	private void sendMessage(String obj) {
		if (messenger != null) {
			Message message = Message.obtain();
			message.obj = obj;
			message.what = WhatAbout.SERVER_LOG.ordinal();
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public void setMessenger(Messenger messenger) {
		this.messenger = messenger;
	}

}