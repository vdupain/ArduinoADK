package com.company.android.arduinoadk;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {

	public BaseActivity() {
		super();
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
			// quit();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_abouthelp:
			Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}