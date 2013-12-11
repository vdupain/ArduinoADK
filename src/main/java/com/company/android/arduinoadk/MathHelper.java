package com.company.android.arduinoadk;

public class MathHelper {

	public static final float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}

	public static final float constrain(float value, float low, float high) {
		return (value < low) ? low : ((value > high) ? high : value);
	}
}
