package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends ArrayAdapter<EntrySummary> implements ListAdapter {
	private final static int textViewResourceId = R.layout.entry_summary;
	private final Context context;
	private final Set<Category> filter;

	public SearchResultAdapter(Context context, int adapterViewResourceId) {
		super(context, textViewResourceId, new ArrayList<EntrySummary>());
		this.context = context;
		filter = new HashSet<Category>(Category.values());
	}

	public void add(List<EntrySummary> objects) {
		for (EntrySummary e : objects)
			super.add(e);
	}

	public boolean getFilter(Category cat) {
		return filter.contains(cat);
	}

	public void setFilter(Category category, boolean isChecked) {
		if (isChecked) {
			filter.add(category);
		} else {
			filter.remove(category);
		}
	}

	public Set<Category> tellFilter() {
		return filter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
