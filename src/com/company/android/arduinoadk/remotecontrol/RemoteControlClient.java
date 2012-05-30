package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class RemoteControlClient implements Runnable {

	private final String host;
	private final int port;

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Thread thread;

	public RemoteControlClient(String host, int port) {
		this.host = host;
		this.port = port;

		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			thread = new Thread(this);
			thread.start();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
