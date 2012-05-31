package com.company.android.arduinoadk.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class RemoteControlClient implements Runnable {
	private final String TAG = RemoteControlClientHandler.class.getSimpleName();

	private String host;
	private int port;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte buffer[] = new byte[32768];
	private int bufferIndex;
	private int bufferLast;
	private Thread thread;

	public RemoteControlClient() {

	}

	public RemoteControlClient(String host, int port) {
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
			socket = new Socket(this.host, this.port);
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
		*/
	}

	public void stop() {
		this.closeResources();
	}

	private void closeResources() {
		this.thread = null;
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

	@Override
	public void run() {
		while (Thread.currentThread() == this.thread) {
			try {
				// this will block
				while ((inputStream != null) && (inputStream.available() > 0)) {
					synchronized (buffer) {
						if (bufferLast == buffer.length) {
							byte temp[] = new byte[bufferLast << 1];
							System.arraycopy(buffer, 0, temp, 0, bufferLast);
							buffer = temp;
						}
						buffer[bufferLast++] = (byte) inputStream.read();
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public int available() {
		return (bufferLast - bufferIndex);
	}

	public void write(String data) {
		write(data.getBytes());
	}

	private void write(byte data[]) {
		try {
			outputStream.write(data);
			outputStream.flush();
		} catch (Exception e) {
			stop();
			throw new RuntimeException(e);
		}
	}

	public String readString() {
		if (bufferIndex == bufferLast)
			return null;
		return new String(readBytes());
	}

	private byte[] readBytes() {
		if (bufferIndex == bufferLast)
			return null;

		synchronized (buffer) {
			int length = bufferLast - bufferIndex;
			byte outgoing[] = new byte[length];
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

			bufferIndex = 0; // rewind
			bufferLast = 0;
			return outgoing;
		}
	}

	public void connect(String host2, int serverPort) {
	}
}
