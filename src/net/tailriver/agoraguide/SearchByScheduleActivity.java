package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import net.tailriver.agoraguide.TimeFrame.Days;

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

		final List<String> timeFrameOrderedEntry = new ArrayList<String>();
		for (TimeFrame tf : AgoraData.getAllTimeFrame())
			timeFrameOrderedEntry.add(tf.getId());

		theAdapter().add(timeFrameOrderedEntry);

		final String[] times = { "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00" };
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		theAdapter().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
		final String day	= (String) ((Spinner) findViewById(R.id.sbs_day) ).getSelectedItem();
		final String time	= (String) ((Spinner) findViewById(R.id.sbs_time)).getSelectedItem();

		final int dayOrdinal = Days.valueOf(day.toUpperCase()).ordinal();
		final int start = Integer.parseInt(time.replace(":", ""));

		final List<TimeFrame> timeFrame = AgoraData.getAllTimeFrame();
		int viewPosition = 0;
		while (viewPosition < timeFrame.size()) {
			final TimeFrame tf = timeFrame.get(viewPosition);
			if (tf.getDay().ordinal() > dayOrdinal || tf.equalsDay(day) && tf.getStart() >= start)
				break;
			viewPosition++;
		}

		((AdapterView<?>) findViewById(R.id.sbs_result)).setSelection(viewPosition);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.sbs_result)).getAdapter();
	}
}
