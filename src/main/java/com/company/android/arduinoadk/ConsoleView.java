package com.company.android.arduinoadk;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

public class ConsoleView extends TextView {

	public ConsoleView(Context context) {
		super(context);
	}

	public ConsoleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ConsoleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void log(String message) {
		String t = getText().toString();
		if (t.split("\n").length >= 20) {
			setText(t.substring(t.indexOf("\n") + 1, t.length()));
		}
		append(Html.fromHtml(message + "<br />"));
	}

}
