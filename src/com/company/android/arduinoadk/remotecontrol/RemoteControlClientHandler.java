package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.arduino.ArduinoManager;
import com.company.android.arduinoadk.clientserver.ClientHandler;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

public class RemoteControlClientHandler implements ClientHandler {
	private final String TAG = RemoteControlClientHandler.class.getSimpleName();

	private ArduinoManager arduinoManager;

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer = new byte[4096];
	private String request;

	private Handler handler;

	public RemoteControlClientHandler(UsbAccessoryManager usbAccessoryManager) {
		this.arduinoManager = new ArduinoManager(usbAccessoryManager);
	}

	@Override
	public void run() {
		int len = 0;
		try {
			log("Connection from " + this.socket.getInetAddress().getHostAddress());
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			while (socket.isConnected()) {
				try {
					len = inputStream.read(buffer, 0, buffer.length);
				} catch (IOException e) {
					break;
				}
				if (len <= 0)
					break;
				request = new String(buffer, 0, len);
				//Log.d(TAG, request);
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

		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeResources();
		}
	}

	private void closeResources() {
		if (inputStream != null) {
			try {
				inputStream.close();
				inputStream = null;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		if (outputStream != null) {
			try {
				outputStream.close();
				outputStream = null;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		if (socket != null) {
			try {
				socket.close();
				socket = null;
				log("Client disconnected");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				log(e.getMessage());
			}
		}
	}

	private void commandStick() {
		try {
			double actualX = Double.parseDouble(request.substring(
					request.indexOf("x=") + 2, request.indexOf(":y=")));
			double actualY = Double.parseDouble(request.substring(
					request.indexOf("y=") + 2, request.indexOf("\n")));
			this.arduinoManager.sendStickCommand(actualX, actualY);
			log(this.socket.getInetAddress().getHostAddress() + " - x=" + actualX
					+ " ,y=" + actualY);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void commandHelp() {
		writeContent("Available commands:\r\n");
		writeContent("STICK:x=valueX:y=valueY\r\n");
	}

	private void commandQuit() {
		writeContent("Command Quit\r\n");		
	}

	private void commandUnknown() {
		writeContent("Unknown Command : " + request + "\r\n");
		commandHelp();
	}

	public void writeContent(String content) {
		Log.d(TAG, content);
		try {
			outputStream.write(content.getBytes(), 0, content.length());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
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

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
		this.arduinoManager.setHandler(handler);
	}

	@Override
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
