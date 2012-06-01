package com.company.android.arduinoadk.test;

import org.junit.Assert;
import org.junit.Test;

import com.company.android.arduinoadk.MathHelper;

public class MathHelperTests {

	@Test
	public void testMap() {
		float iMin=0, iMax=10;
		float oMin=0, oMax=100;
		//tests dans les bornes
		Assert.assertEquals(50, MathHelper.map(5, 0, 10, 0, 100), 0);
		Assert.assertEquals(7.5, MathHelper.map(5, -10, 10, 0, 10), 0);
		//tests aux limites
		Assert.assertEquals(oMin, MathHelper.map(iMin, iMin, iMax, oMin, oMax), 0);
		Assert.assertEquals(oMax, MathHelper.map(iMax, iMin, iMax, oMin, oMax), 0);

		//Assert.assertEquals(0, MathHelper.map(-11, -10, 10, 0, 180), 0);
	}

	@Test
	public void testConstrain() {
		float min=0, max=10;
		//tests dans les bornes
		Assert.assertEquals(min+1, MathHelper.constrain(min+1, min, max), 0);
		//tests aux limites
		Assert.assertEquals(min, MathHelper.constrain(min, min, max), 0);
		Assert.assertEquals(max, MathHelper.constrain(max, min, max), 0);
		//tests hors limites
		Assert.assertEquals(min, MathHelper.constrain(min-1, min, max), 0);
		Assert.assertEquals(max, MathHelper.constrain(max+1, min, max), 0);
	}
}
