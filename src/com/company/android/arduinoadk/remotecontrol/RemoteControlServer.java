package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlServer {
	private static final String TAG = RemoteControlServer.class.getSimpleName();

	private AtomicBoolean stopServer = new AtomicBoolean(false);
	private Handler handler;
	private int port;
	private final UsbAccessoryManager usbAccessoryManager;

	public RemoteControlServer(UsbAccessoryManager usbAccessoryManager,
			Handler handler) {
		this.usbAccessoryManager = usbAccessoryManager;
		this.handler = handler;
	}

	public void service(int port) {
		this.port = port;
		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(1000);
			// One client handled at a time only
			ExecutorService pool = Executors.newSingleThreadExecutor();
			this.handler.sendMessage(Message.obtain(handler,
					WhatAbout.SERVER_START.ordinal()));
			ArduinoManager arduinoManager = new ArduinoManager(
					usbAccessoryManager, handler);
			log("Accept client connection...");
			while (!stopServer.get()) {
				try {
					//Log.d(TAG, "Waits for " + serverSocket.getSoTimeout()
					//		+ "ms an incoming request...");
					clientSocket = serverSocket.accept();
					pool.execute(new RemoteControlClientHandler(clientSocket,
							this.handler, arduinoManager));
				} catch (SocketTimeoutException e) {
					//Log.d(TAG, "SocketTimeoutException");
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
					serverSocket = null;
					this.stopServer.set(false);
					this.handler.sendMessage(Message.obtain(handler,
							WhatAbout.SERVER_STOP.ordinal()));
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			if (clientSocket !=null ) {
				try {
					clientSocket.close();
					log("Client disconnected from Server");
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	public void stop() {
		this.stopServer.set(true);
	}

	public int getPort() {
		return port;
	}

	/**
	 * 
	 * @param msg
	 */
	private void log(String msg) {
		Log.d(TAG, msg);
		sendMessage(msg);
	}

	private void sendMessage(String text) {
		Message message = Message.obtain(handler,
				WhatAbout.SERVER_LOG.ordinal(), text);
		this.handler.sendMessage(message);
	}
}
