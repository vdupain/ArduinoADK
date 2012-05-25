package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

public class RemoteControlHandlerThread extends HandlerThread implements Runnable {

	private final String TAG = "RemoteControlHandlerThread";

	private int port;
	private ServerSocket server;
	private Socket client;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer = new byte[4096];
	private String request, response;

	private ArduinoManager arduinoManager;
	private ControllStick controllStick = new ControllStick();

	private Handler messageHandler;

	public RemoteControlHandlerThread() {
		super("RemoteControlHandlerThread", android.os.Process.THREAD_PRIORITY_AUDIO);
	}

	public RemoteControlHandlerThread(UsbAccessoryManager usbAccessoryCommunication, Handler messageHandler, int port) {
		this();
		this.arduinoManager = new ArduinoManager(usbAccessoryCommunication);
		this.messageHandler = messageHandler;
		this.port = port;
	}

	public void createServer() {
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

	public void stopServer() {
		Log.d(TAG, "stopServer");
		if (server == null)
			return;
		try {
			server.close();
			server = null;
			log("RC Server stopped...");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			log(e.getMessage());
		} finally {
			this.arduinoManager.sendSafeStickCommand();
		}
	}

	private boolean handleClient() {
		// Log.d(TAG, "handleClient");
		if (isCancelled() || server == null)
			return false;
		int len = 0;
		try {
			log("Waits for " + server.getSoTimeout() + "ms an incoming request...");
			client = server.accept();
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();
			log("Connection from " + getClientAddress().getHostAddress());
			while (!isCancelled()) {
				// Log.d(TAG, "While handleClient:" + isCancelled());
				try {
					len = inputStream.read(buffer, 0, buffer.length);
				} catch (IOException e) {
					break;
				}
				if (len <= 0)
					break;

				request = new String(buffer, 0, len);
				// Log.d(TAG, request);
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

	public boolean isCancelled() {
		return this.isAlive() && this.server == null;
	}

	private void commandStick() {
		double actualX = Double.parseDouble(request.substring(request.indexOf("x=") + 2, request.indexOf(":y=")));
		double actualY = Double.parseDouble(request.substring(request.indexOf("y=") + 2, request.indexOf("\n")));
		controllStick.setX(actualX).setY(actualY);
		this.arduinoManager.sendStickCommand(controllStick);
		// log(this.getClientAddress().getHostAddress() + " - " +
		// controllStick.toString());
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

	@Override
	public void run() {
		Log.d(this.TAG, "run");
		while (!this.isCancelled() && this.handleClient())
			;
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
		sendMessage(msg);
	}

	public int getPort() {
		return port;
	}

	private void sendMessage(String text) {
		Message.obtain(messageHandler, WhatAbout.SERVER_LOG.ordinal(), text);
		Message msg = new Message();
		msg.obj = text;
		msg.what = WhatAbout.SERVER_LOG.ordinal();
		// mTaskMaster.getMessageHandler().sendMessage(msg);
		this.messageHandler.sendMessage(msg);

	}
}
