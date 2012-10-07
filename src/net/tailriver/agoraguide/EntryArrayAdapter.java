package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class EntryArrayAdapter
extends ArrayAdapter<EntrySummary>
implements ListAdapter, OnItemClickListener
{
	private final static int textViewResourceId = R.layout.entry_summary;
	private final Context context;
	private final int adapterViewResourceId;

	private final Set<Category> filter;

	public EntryArrayAdapter(Context context, int adapterViewResourceId) {
		super(context, textViewResourceId, new ArrayList<EntrySummary>());
		this.context = context;
		this.adapterViewResourceId = adapterViewResourceId;

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
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		EntrySummary summary = getItem(position);

		TextView titleView = (TextView) convertView.findViewById(R.id.entrylist_item_title);
		titleView.setText(summary.getTitle());

		TextView sponsorView = (TextView) convertView.findViewById(R.id.entrylist_item_sponsor);
		sponsorView.setText(summary.getSponsor());

		TextView scheduleView = (TextView) convertView.findViewById(R.id.entrylist_item_schedule);
		scheduleView.setText(summary.getSchedule());

		TextView targetView = (TextView) convertView.findViewById(R.id.entrylist_item_target);
		targetView.setVisibility(View.INVISIBLE);

		return convertView;
	}

	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(context, EntryDetailActivity.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, EntrySummary.values().get(position));
		((Activity) context).startActivityForResult(intent, textViewResourceId);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != textViewResourceId || resultCode != Activity.RESULT_OK)
			return;

		View view = ((Activity) context).findViewById(adapterViewResourceId);
		view.requestFocusFromTouch();	// it is need when DPAD operation and back
	}
}
