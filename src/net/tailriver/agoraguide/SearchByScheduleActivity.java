package net.tailriver.agoraguide;

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
		setTitle(R.string.searchBySchedule);

		final ListView entryList = (ListView) findViewById(R.id.sbs_result);
		entryList.setAdapter(new EntryArrayAdapter(SearchByScheduleActivity.this, entryList.getId()));
		entryList.setOnItemClickListener(theAdapter());
		entryList.setEmptyView(findViewById(R.id.sbs_empty));

		final String[] times = {"10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00"};
		final SpinnerAdapter dayAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this, android.R.layout.simple_spinner_item, TimeFrame.getDaysString());
		final SpinnerAdapter timeAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this, android.R.layout.simple_spinner_item, times);

		final Spinner daySpinner = (Spinner) findViewById(R.id.sbs_day);
		final Spinner timeSpinner = (Spinner) findViewById(R.id.sbs_time);

		daySpinner.setAdapter(dayAdapter);
		timeSpinner.setAdapter(timeAdapter);
		daySpinner.setOnItemSelectedListener(this);
		timeSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		theAdapter().clear();
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		theAdapter().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
		final String day	= (String) ((Spinner) findViewById(R.id.sbs_day) ).getSelectedItem();
		final String time	= (String) ((Spinner) findViewById(R.id.sbs_time)).getSelectedItem();

		final int start = Integer.parseInt(time.replace(":", ""));
		final int end	= start + 100;

		theAdapter().clear();
		theAdapter().add(AgoraData.getEntryBySchedule(day, start, end));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		theAdapter().clear();
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.sbs_result)).getAdapter();
	}
}
