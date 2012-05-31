package com.company.android.arduinoadk.remotecontrol;

public class PositionMessage {

	private final float x;
	private final float y;

	public PositionMessage(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
