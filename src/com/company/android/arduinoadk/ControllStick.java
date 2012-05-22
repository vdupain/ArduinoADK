package com.company.android.arduinoadk;

public class ControllStick {

	private double x;
	private double y;

	public ControllStick(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public ControllStick() {

	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public ControllStick setY(double y) {
		this.y = y;
		return this;
	}

	public ControllStick setX(double x) {
		this.x = x;
		return this;
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + getY();
	}

}
