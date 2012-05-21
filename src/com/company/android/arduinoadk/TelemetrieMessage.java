package com.company.android.arduinoadk;

public class TelemetrieMessage {

	private int degree;
	private int distance;

	public TelemetrieMessage(int degree, int distance) {
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
