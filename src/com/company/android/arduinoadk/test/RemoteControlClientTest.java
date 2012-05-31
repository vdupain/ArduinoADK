package com.company.android.arduinoadk.test;

import org.junit.Test;

import com.company.android.arduinoadk.remotecontrol.RemoteControlClient;

public class RemoteControlClientTest {

	@Test
	public void test() {
		RemoteControlClient client = new RemoteControlClient("localhost", 12345);
		int x = 123;
		int y = 90;
		for (int i = 0; i < 10; i++) {
			System.out.println("i=" + i);
			client.write("STICK:x=" + i + ":y=" + y + "\n");
			if (client.available() > 0) {
				System.out.println(client.readString());
			}
		}
	}

}
