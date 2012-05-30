package com.company.android.arduinoadk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {

	private PowerManager.WakeLock wakeLock;

	public BaseActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getSimpleName(), "onCreate");
		super.onCreate(savedInstanceState);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// Create a wake lock
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass()
				.getName());
	}

	@Override
	protected void onStart() {
		Log.d(this.getClass().getSimpleName(), "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log.d(this.getClass().getSimpleName(), "onRestart");
		super.onRestart();
	}

	@Override
	public void onResume() {
		Log.d(this.getClass().getSimpleName(), "onResume");
		super.onResume();
		// when the activity is resumed, we acquire a wake-lock so that the
		// screen stays on
		wakeLock.acquire();
	}

	@Override
	public void onPause() {
		Log.d(this.getClass().getSimpleName(), "onPause");
		super.onPause();
		// and release our wake-lock
		wakeLock.release();
	}

	@Override
	protected void onStop() {
		Log.d(this.getClass().getSimpleName(), "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getSimpleName(), "onDestroy");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rcserver:
			startActivity(new Intent(this, HomeActivity.class));
			return true;
		case R.id.menu_rcclient:
			startActivity(new Intent(this, RCClientActivity.class));
			return true;
		case R.id.menu_arduino:
			startActivity(new Intent(this, ArduinoActivity.class));
			return true;
		case R.id.menu_quit:
			quit();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_abouthelp:
			Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT)
					.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void quit() {
		onQuit();
	}

	abstract void onQuit();

	abstract void initControllers();

}