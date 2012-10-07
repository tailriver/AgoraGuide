package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SearchByScheduleActivity extends Activity implements OnItemSelectedListener {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchbyschedule);

		AgoraGuideActivity.initDatabase(getApplicationContext());

		final ListView entryList = (ListView) findViewById(R.id.search_result);
		entryList.setAdapter(new EntryArrayAdapter(SearchByScheduleActivity.this, entryList.getId()));
		entryList.setOnItemClickListener(theAdapter());
		entryList.setEmptyView(findViewById(R.id.search_not_found));

		TimeFrame.init();
		List<EntrySummary> entries = new ArrayList<EntrySummary>();
		for (TimeFrame tf : TimeFrame.values()) {
			entries.add(tf.getSummary());
		}
		theAdapter().add(entries);

		List<String> localDays = new ArrayList<String>();
		for (Day d : Day.values()) {
			localDays.add(d.getLocalString());
		}
		SpinnerAdapter dayAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this,
				android.R.layout.simple_spinner_item, localDays);
		SpinnerAdapter timeAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this,
				android.R.layout.simple_spinner_item, getApplicationContext().getResources().getStringArray(R.array.sbs_times));
		Spinner daySpinner  = (Spinner) findViewById(R.id.sbs_day);
		Spinner timeSpinner = (Spinner) findViewById(R.id.sbs_time);

		daySpinner.setAdapter(dayAdapter);
		timeSpinner.setAdapter(timeAdapter);
		daySpinner.setOnItemSelectedListener(this);
		timeSpinner.setOnItemSelectedListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		theAdapter().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
		int day	 = ((Spinner) findViewById(R.id.sbs_day)).getSelectedItemPosition();
		int time = Integer.parseInt(((String) ((Spinner) findViewById(R.id.sbs_time)).getSelectedItem()).replace(":", ""));

		int viewPosition = TimeFrame.search(Day.values().get(day), time);
		((AdapterView<?>) findViewById(R.id.search_result)).setSelection(viewPosition);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.search_result)).getAdapter();
	}
}
