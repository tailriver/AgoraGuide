package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends ArrayAdapter<EntrySummary> implements ListAdapter {
	private final static int textViewResourceId = R.layout.entry_summary;

	public SearchResultAdapter(Context context) {
		super(context, textViewResourceId, new ArrayList<EntrySummary>());
	}

	@Override
	public void addAll(Collection<? extends EntrySummary> collection) {
		// ListAdapter.addAll() requires API Level 11
		for (EntrySummary summary : collection) {
			add(summary);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		EntrySummary summary = getItem(position);

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
