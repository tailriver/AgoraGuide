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

		AgoraData.setApplicationContext(getApplicationContext());

		final ListView entryList = (ListView) findViewById(R.id.sbs_result);
		entryList.setAdapter(new EntryArrayAdapter(SearchByScheduleActivity.this, entryList.getId()));
		entryList.setOnItemClickListener(theAdapter());
		entryList.setEmptyView(findViewById(R.id.sbs_empty));

		final List<String> timeFrameOrderedEntry = new ArrayList<String>();
		for (TimeFrame tf : AgoraData.getAllTimeFrame())
			timeFrameOrderedEntry.add(tf.getId());

		theAdapter().add(timeFrameOrderedEntry);

		final SpinnerAdapter dayAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this,
				android.R.layout.simple_spinner_item, Day.getDaysLocale());
		final SpinnerAdapter timeAdapter = new ArrayAdapter<String>(SearchByScheduleActivity.this,
				android.R.layout.simple_spinner_item, getApplicationContext().getResources().getStringArray(R.array.sbs_times));
		final Spinner daySpinner = (Spinner) findViewById(R.id.sbs_day);
		final Spinner timeSpinner = (Spinner) findViewById(R.id.sbs_time);

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
		final int day	= ((Spinner) findViewById(R.id.sbs_day)).getSelectedItemPosition();
		final int time	= Integer.parseInt(((String) ((Spinner) findViewById(R.id.sbs_time)).getSelectedItem()).replace(":", ""));

		final TimeFrame pivot = new TimeFrame(null, Day.getDays().get(day), time, time);
		final List<TimeFrame> timeFrame = AgoraData.getAllTimeFrame();

		int viewPosition = 0;
		while (viewPosition < timeFrame.size()) {
			final TimeFrame seek = timeFrame.get(viewPosition);
			if (seek.compareTo(pivot) > -1)
				break;
			viewPosition++;
		}

		((AdapterView<?>) findViewById(R.id.sbs_result)).setSelection(viewPosition);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.sbs_result)).getAdapter();
	}
}
