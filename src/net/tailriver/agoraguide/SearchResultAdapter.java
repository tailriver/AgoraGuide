package net.tailriver.agoraguide;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends BaseAdapter implements ListAdapter {
	private LayoutInflater inflater;
	private Collection<? extends EntrySummary> origin;
	private Comparator<? super EntrySummary> comparator;
	private List<EntrySummary> list;

	public SearchResultAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list     = Collections.emptyList();
	}

	public void setSource(Collection<? extends EntrySummary> collection,
			Comparator<? super EntrySummary> comparator) {
		this.origin     = new HashSet<EntrySummary>(collection);
		this.comparator = comparator;
	}

	public void filter(Object... filter) {
		new SearchTask().execute(filter);
	}

	public int getCount() {
		return list.size();
	}

	public EntrySummary getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.entry_summary, null);
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

	private final class SearchTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			EntryFilter filter = new EntryFilter(origin);
			for (Object p : params) {
				if (p instanceof Collection<?>) {
					filter.applyFilter((Collection<?>) p);
				} else if (p instanceof String) {
					filter.applyFilter((String) p);
				} else {
					throw new UnsupportedOperationException("unsupported filter");
				}
			}
			list = filter.getResult(comparator);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			notifyDataSetChanged();
		}
	}
}
