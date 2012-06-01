package com.company.android.arduinoadk.clientserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.company.android.arduinoadk.WhatAbout;

/**
 * This thread listens to client connection and delegates it to worker threads.
 */
public class TCPServer extends Thread {
	private static final String TAG = TCPServer.class.getSimpleName();

	private Handler handler;
	private int port;
	private ClientHandler clientHandler;
	private ServerSocket serverSocket;
	private AtomicBoolean isListen = new AtomicBoolean(false);

	public TCPServer(int port) {
		this.port = port;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Log.d(TAG, e.getMessage(), e);
		}
	}

	public void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		}
		this.interrupt();
	}

	@Override
	public void run() {
		log("Listening on port " + this.serverSocket.getLocalPort());
		Socket clientSocket = null;
		try {
			// One client handled at a time only
			ExecutorService pool = Executors.newSingleThreadExecutor();
			this.handler.sendMessage(Message.obtain(handler, WhatAbout.SERVER_START.ordinal()));
			log("Accept client connection...");
			isListen.set(true);
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(100);
					try {
						clientSocket = serverSocket.accept();
						clientHandler.setSocket(clientSocket);
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
			isListen.set(false);
			try {
				serverSocket.close();
				serverSocket = null;
				this.handler.sendMessage(Message.obtain(handler, WhatAbout.SERVER_STOP.ordinal()));
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
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

	public boolean isListen() {
		return isListen.get();
	}

	public int getPort() {
		return port;
	}

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

}
