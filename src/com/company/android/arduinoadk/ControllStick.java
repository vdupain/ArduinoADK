package com.company.android.arduinoadk;

public class ControllStick {

	private double x;
	private double y;

	public ControllStick(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "xy(" + x + "," + y + ")";
	}

}
