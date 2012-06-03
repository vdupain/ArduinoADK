package com.company.android.arduinoadk.clientserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

/**
 * This server listens to client connection and delegates it to worker threads.
 */
public class TCPServer extends BaseEndPoint {
	static final String TAG = TCPServer.class.getSimpleName();

	private int port;
	private ClientHandler clientHandler;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	// One client handled at a time only
	ExecutorService pool = Executors.newSingleThreadExecutor();

	public TCPServer(int port) {
		this.port = port;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Log.d(TAG, e.getMessage(), e);
		}
	}

	void onStop() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		}
	}

	public int getPort() {
		return port;
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public ClientHandler getClientHandler() {
		return this.clientHandler;
	}

	@Override
	void doBeforeRun() {
	}

	@Override
	void doInRun() {
		Log.d(TAG, "Listening on port " + this.serverSocket.getLocalPort());
		try {
			clientSocket = serverSocket.accept();
			clientHandler.setSocket(clientSocket);
			pool.execute(clientHandler);
		} catch (SocketTimeoutException ignored) {
		} catch (SocketException ignored) {
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	void doAfterRun() {
		Log.d(TAG, "Stopping server...");
		try {
			serverSocket.close();
			serverSocket = null;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		if (clientSocket != null) {
			try {
				clientSocket.close();
				Log.d(TAG, "Client disconnected from Server");
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

	}

}
