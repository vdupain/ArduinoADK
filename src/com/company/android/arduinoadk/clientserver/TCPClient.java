package com.company.android.arduinoadk.clientserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.company.android.arduinoadk.WhatAbout;

public class TCPClient extends BaseEndPoint {
	private final String TAG = TCPClient.class.getSimpleName();

	private Socket socket = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] buffer = new byte[4096];
	private Handler handler;
	private final String host;
	private final int port;

	public TCPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public boolean connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			return true;
		} catch (UnknownHostException e) {
			Log.w(TAG, e.getMessage(), e);
			this.handler.sendMessage(Message.obtain(handler,
					WhatAbout.SERVER_CONNECTION_FAILURE.ordinal(), e));
		} catch (ConnectException e) {
			Log.w(TAG, e.getMessage(), e);
			this.handler.sendMessage(Message.obtain(handler,
					WhatAbout.SERVER_CONNECTION_FAILURE.ordinal(), e));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}

	private void handleRequest(String req) {
		Log.d(TAG, req);
	}

	public void write(String content) {
		this.write(content.getBytes());
	}

	private void write(byte[] buffer) {
		try {
			outputStream.write(buffer, 0, buffer.length);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
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
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void onStop() {
		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	@Override
	void doBeforeRun() {
		boolean connected = this.connect(host, port);
		if (!connected)
			stop.set(true);
	}

	@Override
	void doInRun() {
		int len = 0;
		try {
			len = inputStream.read(buffer, 0, buffer.length);
		} catch (IOException e) {
			return;
		}
		if (len <= 0)
			return;
		handleRequest(new String(buffer, 0, len));
	}

	@Override
	void doAfterRun() {
		closeResources();
	}

}
