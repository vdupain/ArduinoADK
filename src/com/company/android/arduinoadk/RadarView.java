package com.company.android.arduinoadk;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RadarView extends View {
	private Paint mPaint;
	private Paint paint = new Paint();
	private Paint paintArc = new Paint();
	private Paint paint2 = new Paint();
	private Paint paint3 = new Paint();

	private int height, width;
	private int[] newValues = new int[181]; // create an array to store each new
											// value
	private int[] oldValues = new int[181]; // to store the previous values.
	private int degree;
	private int distance;
	private boolean clockwise = true;
	private int angle, radius = 0;
	private float x, y, x0, y0, xi, yi = 0;
	private RectF oval = new RectF();
	private float[] points;

	public RadarView(Context context) {
		super(context);
		initRadarView(context);
	}

	public RadarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRadarView(context);
	}

	private void initRadarView(Context context) {
		mPaint = new Paint();
		mPaint.setColor(Color.GREEN);
		mPaint.setTextSize(18);
		mPaint.setAntiAlias(true);
		mPaint.setShadowLayer(1, 2, 2, Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);

		paintArc = new Paint();
		paintArc.setColor(Color.GREEN);
		paintArc.setStrokeWidth(1);
		paintArc.setStyle(Paint.Style.STROKE);
		paintArc.setAlpha(128);

		paint3 = new Paint();
		paint3.setColor(Color.GREEN);
		paint3.setStrokeWidth(1);
		paint3.setStyle(Paint.Style.FILL);
		paint3.setAlpha(128);

		paint2 = new Paint();
		paint2.setColor(Color.RED);
		paint2.setStrokeWidth(5);
		paint2.setStyle(Paint.Style.FILL);

		// test();
	}

	private void test() {
		Random random = new Random();
		for (int i = 0; i < 180; i++) {
			setPosition(i, 100 + random.nextInt(10));
		}
		for (int i = 180; i >= 0; i--) {
			setPosition(i, 100 + random.nextInt(10));
		}
	}

	public void setPosition(int degree, int distance) {
		Log.d("RadarView", "degree=" + degree + ", distance=" + distance);
		this.degree = degree;
		this.distance = distance;
		oldValues[degree] = newValues[degree];
		newValues[degree] = distance;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		height = this.getHeight();
		width = this.getWidth();

		canvas.translate(width / 2, height / 2);
		// if at the far right then set motion = 1/ true we're about to go right
		// to left
		if (degree >= 179) {
			// this changes the animation to run right to left
			clockwise = false;
		}
		// if 0 degrees then we're about to go left to right
		if (degree <= 1) {
			// this sets the animation to run left to right
			clockwise = true;
		}
		paint.setColor(Color.rgb(0, 0, 0));

		// set the radar distance rings and out put their values 50, 100, 150,
		// etc...
		for (int i = 0; i <= 6; i++) {
			oval.set(radius - 30 * i, radius - 30 * i, 30 * i, 30 * i);
			canvas.drawArc(oval, 0, 360, false, paintArc);
			canvas.drawText((30 * i) + "", radius, -(30 * i), mPaint);
		}

		// draw the grid lines on the radar every 30 degrees and write their
		// values 180, 210, 240 etc..
		points = new float[12 * 4];
		for (int i = 0; i < 12; i++) {
			angle = 180 + (30 * i);
			xi = (float) (Math.cos(Math.toRadians(angle))) * 220;
			yi = (float) (Math.sin(Math.toRadians(angle))) * 220;
			points[i * 4] = radius - xi;
			points[i * 4 + 1] = radius - yi;
			points[i * 4 + 2] = radius + xi;
			points[i * 4 + 3] = radius + yi;
			canvas.drawText((30 * i) + "", xi - 10, yi, mPaint);
		}
		canvas.drawLines(points, mPaint);

		paint.setColor(Color.rgb(0, 255, 0));
		points = new float[20 * 4];
		oval.set(radius - 220, radius - 220, 220, 220);
		if (clockwise) {// if going left to right
			canvas.drawArc(oval, degree + 180, 20, true, paint3);
		} else {// if going right to left
			canvas.drawArc(oval, degree + 180, 20, true, paint3);
		}

		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.rgb(0, 0, 255));
		// 1rst
		for (int i = 0; i < 180; i++) { // for each degree in the array
			x = (float) (radius + Math.cos(Math.toRadians((180 + i))) * oldValues[i]);
			y = (float) (radius + Math.sin(Math.toRadians((180 + i))) * oldValues[i]);
			canvas.drawPoint(x, y, paint);
		}
		paint.setColor(Color.rgb(0, 255, 0));
		// 2nd

		for (int i = 0; i < 180; i++) { // for each degree in the array
			x = (float) (radius + Math.cos(Math.toRadians((180 + i))) * newValues[i]);
			y = (float) (radius + Math.sin(Math.toRadians((180 + i))) * newValues[i]);
			canvas.drawPoint(x, y, paint);
			canvas.drawLine(x0, y0, x, y, paint);
			x0 = x;
			y0 = y;
		}
		paint.setColor(Color.rgb(255, 255, 255));
		// average
		for (int i = 0; i < 180; i++) { // for each degree in the array
			x = (float) (radius + Math.cos(Math.toRadians((180 + i))) * ((newValues[i] + oldValues[i]) / 2));
			y = (float) (radius + Math.sin(Math.toRadians((180 + i))) * ((newValues[i] + oldValues[i]) / 2));
			canvas.drawPoint(x, y, paint);
		}
		for (int i = 0; i < 180; i++) {
			if (Math.abs(oldValues[i] - newValues[i]) >= 30) {
				x = (float) (radius + Math.cos(Math.toRadians((180 + i))) * (newValues[i]));
				y = (float) (radius + Math.sin(Math.toRadians((180 + i))) * (newValues[i]));
				canvas.drawPoint(x, y, paint2);
				// canvas.drawText("x", x, y, paint2);
			}
		}

	}

}
