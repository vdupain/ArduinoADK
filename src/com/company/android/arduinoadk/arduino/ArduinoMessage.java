package com.company.android.arduinoadk.arduino;

public class ArduinoMessage {

	private int degree;
	private int distance;

	public ArduinoMessage(int degree, int distance) {
		this.degree = degree;
		this.distance = distance;
	}

	public int getDegree() {
		return degree;
	}

	public int getDistance() {
		return distance;
	}

}
