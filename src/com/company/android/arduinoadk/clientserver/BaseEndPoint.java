package com.company.android.arduinoadk.clientserver;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseEndPoint implements EndPoint {
	protected AtomicBoolean stop = new AtomicBoolean(false);

	public BaseEndPoint() {
		super();
	}

	public void start() {
		new Thread(this, this.getClass().getSimpleName()).start();
	}

	public void run() {
		stop.set(false);
		doBeforeRun();
		try {
			while (!stop.get()) {
				doInRun();
			}
		} finally {
			doAfterRun();
		}
	}

	public void stop() {
		if (stop.compareAndSet(false, true))
			onStop();
	}

	abstract void onStop();

	abstract void doBeforeRun();

	abstract void doInRun();

	abstract void doAfterRun();

}