package com.company.android.arduinoadk;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class ServerController extends AbstractController {
	private final String TAG = "ServerController";

	private static Server server;

	private TextView console;
	private TextView ip;

	public ServerController(ArduinoADKActivity activity) {
		super(activity);
		console = (TextView) findViewById(R.id.console);
		ip = (TextView) findViewById(R.id.ip);
	}

	@Override
	protected void onUsbAccesssoryAttached() {
		server = new Server(this.activity.usbAccessoryCommunication, ArduinoADKActivity.DEFAULT_PORT, this.activity.handler);
		server.start();
	}

	@Override
	protected void onUsbAccesssoryDetached() {
	}

	public void displayIP() {
		// Determines if user is connected to a wireless network & displays ip
		WifiManager wifiManager = (WifiManager) this.activity.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo.getNetworkId() > -1) {
			int ipAddress = wifiInfo.getIpAddress();
			byte[] byteaddr = new byte[] { (byte) (ipAddress & 0xff), (byte) (ipAddress >> 8 & 0xff), (byte) (ipAddress >> 16 & 0xff),
					(byte) (ipAddress >> 24 & 0xff) };
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByAddress(byteaddr);
				ip.setText("tcp://");
				ip.append(inetAddress.getHostAddress());
				ip.append(":" + this.activity.DEFAULT_PORT + "/");
			} catch (UnknownHostException e) {
				Log.e(TAG, e.getMessage(), e);
				ip.setText(e.getMessage());
			}
		} else {
			ip.setText("Wifi should be enabled !");
		}
	}

	public void logConsole(String message) {
		String t = console.getText().toString();
		if (t.split("\n").length > 8) {
			console.setText(t.substring(t.indexOf("\n") + 1, t.length()));
		}
		console.append(Html.fromHtml(message + "<br />"));
	}

}
