package com.company.android.arduinoadk.clientserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.company.android.arduinoadk.WhatAbout;

public class TCPServer implements Runnable {
	private static final String TAG = TCPServer.class.getSimpleName();

	private Handler handler;
	private int port;
	private ClientHandler clientHandler;
	ServerSocket serverSocket = null;

	public TCPServer(int port) {
		this.port = port;
	}

	public void service(int port) {
		this.port = port;
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			// One client handled at a time only
			ExecutorService pool = Executors.newSingleThreadExecutor();
			this.handler.sendMessage(Message.obtain(handler, WhatAbout.SERVER_START.ordinal()));
			log("Accept client connection...");
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(100);
					// Log.d(TAG, "Waiting for an incoming request...");
					try {
						clientSocket = serverSocket.accept();
						clientHandler.setSocket(clientSocket);
						clientHandler.setHandler(handler);
						pool.execute(clientHandler);
					} catch (SocketTimeoutException ignored) {
					} catch (SocketException ignored) {
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupted();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
					serverSocket = null;
					this.handler.sendMessage(Message.obtain(handler, WhatAbout.SERVER_STOP.ordinal()));
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

	public void cancel() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		}
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
		Message message = Message.obtain(handler, WhatAbout.SERVER_LOG.ordinal(), text);
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

	@Override
	public void run() {
		service(port);
	}

}
