package com.company.android.arduinoadk;

import java.util.Random;

import android.os.Handler;
import android.os.Message;

public class TestUtils {

	public static void test(final Handler handler) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Random random = new Random();
				while (true) {
					for (int i = 0; i <= 180; i++) {
						Message m = Message.obtain(handler, WhatAbout.TELEMETRY.ordinal());
						m.obj = new ArduinoMessage(i, 120 + random.nextInt(60));
						handler.sendMessage(m);
						try {
							Thread.sleep(10);
						} catch (InterruptedException ignored) {
						}
					}
					for (int i = 180; i >= 0; i--) {
						Message m = Message.obtain(handler, WhatAbout.TELEMETRY.ordinal());
						m.obj = new ArduinoMessage(i, 120 + random.nextInt(60));
						handler.sendMessage(m);
						try {
							Thread.sleep(10);
						} catch (InterruptedException ignored) {
						}
					}
				}
			}
		});
		thread.start();
	}
}
