package com.company.android.arduinoadk;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ArduinoADKPhone extends ArduinoADKActivity implements OnClickListener {
	private List<LabelContainer> containers = new ArrayList<LabelContainer>();

	protected void showControls() {
		super.showControls();
		TextView label1 = (TextView) findViewById(R.id.label1);
		TextView label2 = (TextView) findViewById(R.id.label2);
		ViewGroup container1 = (ViewGroup) findViewById(R.id.container1);
		ViewGroup container2 = (ViewGroup) findViewById(R.id.container2);

		containers.add(0, new LabelContainer(label1, container1));
		containers.add(1, new LabelContainer(label2, container2));
		for (LabelContainer labelContainer : containers) {
			labelContainer.label.setOnClickListener(this);
		}

		showTabContents(0);
	}

	private void showTabContents(int indexContainer) {
		LabelContainer labelContainer;
		for (int i = 0; i < containers.size(); i++) {
			labelContainer = containers.get(i);
			if (i == indexContainer) {
				labelContainer.container.setVisibility(View.VISIBLE);
				labelContainer.label.setBackgroundResource(R.color.dark_cyan);
			} else {
				labelContainer.container.setVisibility(View.GONE);
				labelContainer.label.setBackgroundResource(R.color.black);
			}
		}
	}

	public void onClick(View v) {
		int vId = v.getId();
		switch (vId) {
		case R.id.label1:
			showTabContents(0);
			break;

		case R.id.label2:
			showTabContents(1);
			break;
		}
	}

	private class LabelContainer {
		TextView label;
		ViewGroup container;

		public LabelContainer(TextView label, ViewGroup container) {
			this.label = label;
			this.container = container;
		}
	}
}