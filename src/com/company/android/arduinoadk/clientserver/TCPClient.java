package com.company.android.arduinoadk.clientserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.company.android.arduinoadk.WhatAbout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TCPClient {
	private final String TAG = TCPClient.class.getSimpleName();

	private AtomicBoolean stop = new AtomicBoolean(false);
	private Socket socket = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer = new byte[4096];
	private String request;
	private Handler handler;

	public TCPClient() {
	}

	public void connect(String host, int port) {
		int len = 0;
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			while (!stop.get()) {
				try {
					len = inputStream.read(buffer, 0, buffer.length);
				} catch (IOException e) {
					break;
				}
				if (len <= 0)
					break;
				request = new String(buffer, 0, len);
				handleRequest(request);
			}
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage(), e);
			this.handler.sendMessage(Message.obtain(handler,
					WhatAbout.SERVER_CONNECTION_FAILURE.ordinal(), e));
		} catch (ConnectException e) {
			Log.e(TAG, e.getMessage(), e);
			this.handler.sendMessage(Message.obtain(handler,
					WhatAbout.SERVER_CONNECTION_FAILURE.ordinal(), e));

		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeResources();
		}

	}

	private void handleRequest(String req) {
		Log.d(TAG, req);
	}

	public void stop() {
		this.stop.set(true);
	}

	public void writeContent(String content) {
		Log.d(TAG, content);
		if (outputStream != null && content != null) {

			try {
				outputStream.write(content.getBytes(), 0, content.length());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
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
				// log("Client disconnected");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				// log(e.getMessage());
			}
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
