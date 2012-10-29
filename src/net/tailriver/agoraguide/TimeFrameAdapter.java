package net.tailriver.agoraguide;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TimeFrameAdapter extends BaseAdapter implements ListAdapter {
	private LayoutInflater inflater;
	private List<TimeFrame> list;

	// TODO marges to SearchResultAdapter
	public TimeFrameAdapter(Activity activity) {
		inflater = activity.getLayoutInflater();
		list     = TimeFrame.values();
	}

	public int getCount() {
		return list.size();
	}

	public TimeFrame getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.entry_summary, null);
		}

		EntrySummary summary = getItem(position).getSummary();

		TextView titleView = (TextView) convertView.findViewById(R.id.entrylist_item_title);
		titleView.setText(summary.toString());

		TextView sponsorView = (TextView) convertView.findViewById(R.id.entrylist_item_sponsor);
		sponsorView.setText(summary.getSponsor());

		TextView scheduleView = (TextView) convertView.findViewById(R.id.entrylist_item_schedule);
		scheduleView.setText(summary.getSchedule());

		TextView targetView = (TextView) convertView.findViewById(R.id.entrylist_item_target);
		targetView.setVisibility(View.INVISIBLE);

		return convertView;
	}
}
