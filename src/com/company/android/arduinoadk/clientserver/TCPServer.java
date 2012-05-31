package com.company.android.arduinoadk.clientserver;

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

public class TCPServer {
	private static final String TAG = TCPServer.class.getSimpleName();

	private AtomicBoolean stop = new AtomicBoolean(false);
	private Handler handler;
	private int port;
	private ClientHandler clientHandler;

	public TCPServer() {
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
			log("Accept client connection...");
			while (!stop.get()) {
				try {
					Log.d(TAG, "Waiting for " + serverSocket.getSoTimeout()
							+ "ms an incoming request...");
					clientSocket = serverSocket.accept();
					clientHandler.setSocket(clientSocket);
					clientHandler.setHandler(handler);
					pool.execute(clientHandler);
				} catch (SocketTimeoutException e) {
					// Log.d(TAG, "SocketTimeoutException");
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
					serverSocket = null;
					this.stop.set(false);
					this.handler.sendMessage(Message.obtain(handler,
							WhatAbout.SERVER_STOP.ordinal()));
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			if (clientSocket != null) {
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
		this.stop.set(true);
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

	public void setHandler(Handler handler) {
		this.handler = handler;
		if (clientHandler != null) {
			clientHandler.setHandler(handler);
		}
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
}
