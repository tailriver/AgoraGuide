package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class ScheduleSearchActivity extends SearchActivity implements OnItemSelectedListener {
	private Spinner daySpinner;
	private Spinner timeSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchbyschedule);

		List<EntrySummary> entries = new ArrayList<EntrySummary>();
		for (TimeFrame tf : TimeFrame.values()) {
			entries.add(tf.getSummary());
		}
		String[] times = getResources().getStringArray(R.array.sbs_times);

		searchAdapter.add(entries);

		SpinnerAdapter dayAdapter =
				new ArrayAdapter<Day>(this, android.R.layout.simple_spinner_item, Day.values());
		SpinnerAdapter timeAdapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times);

		daySpinner  = (Spinner) findViewById(R.id.sbs_day);
		timeSpinner = (Spinner) findViewById(R.id.sbs_time);

		daySpinner.setAdapter(dayAdapter);
		timeSpinner.setAdapter(timeAdapter);
		daySpinner.setOnItemSelectedListener(this);
		timeSpinner.setOnItemSelectedListener(this);
	}

	public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
		Day day = (Day) daySpinner.getSelectedItem();
		int time = Integer.parseInt(((String) timeSpinner.getSelectedItem()).replace(":", ""));

		int viewPosition = TimeFrame.search(day, time);
		resultView.setSelection(viewPosition);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}
}
