package com.company.android.arduinoadk.test;

import org.junit.Assert;
import org.junit.Test;

public class ArduinoADKTests {

	@Test
	public void testParsingCommandJoystick() {
		double totalX = 0.5;
		double totalY=0.234;
		String stringToParse = "JOYSTICK:x=" + totalX + ":y=" + totalY  + "\n";
		System.out.println(stringToParse);
		
	    double actualX = Double.parseDouble(stringToParse.substring(stringToParse.indexOf("x=")+2, stringToParse.indexOf(":y=")));
	    double actualY = Double.parseDouble(stringToParse.substring(stringToParse.indexOf("y=")+2, stringToParse.indexOf("\n")));
		Assert.assertTrue(totalX==actualX);
		Assert.assertTrue(totalY==actualY);
	}

}
