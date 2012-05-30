package com.company.android.arduinoadk;

import android.content.res.Resources;
import android.view.View;

public abstract class AbstractController {

	protected BaseActivity activity;

	public AbstractController(BaseActivity activity) {
		this.activity = activity;
	}

	protected View findViewById(int id) {
		return activity.findViewById(id);
	}

	protected Resources getResources() {
		return activity.getResources();
	}

	void usbAccessoryAttached() {
		onUsbAccessoryAttached();
	}

	void usbAccessoryDetached() {
		onUsbAccessoryDetached();
	}

	abstract protected void onUsbAccessoryAttached();

	abstract protected void onUsbAccessoryDetached();

}