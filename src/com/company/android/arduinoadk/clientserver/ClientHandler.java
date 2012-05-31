package com.company.android.arduinoadk.clientserver;

import java.net.Socket;

import android.os.Handler;

public interface ClientHandler extends Runnable {

	void setHandler(Handler handler);

	void setSocket(Socket socket);

}
