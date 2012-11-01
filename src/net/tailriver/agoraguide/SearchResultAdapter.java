package net.tailriver.agoraguide;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends BaseAdapter implements ListAdapter {
	private Collection<? extends EntrySummary> origin;
	private LayoutInflater inflater;
	private List<EntrySummary> list;
	private TextView textView;

	public SearchResultAdapter(Activity activity) {
		origin   = new HashSet<EntrySummary>(EntrySummary.values());
		inflater = activity.getLayoutInflater();
		list     = Collections.emptyList();
		textView = (TextView) activity.findViewById(R.id.searchNotFound);
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
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.entry_summary, null);
			holder = new ViewHolder();
			holder.title    = (TextView) convertView.findViewById(R.id.entrylist_item_title);
			holder.sponsor  = (TextView) convertView.findViewById(R.id.entrylist_item_sponsor);
			holder.schedule = (TextView) convertView.findViewById(R.id.entrylist_item_schedule);
			holder.target   = (TextView) convertView.findViewById(R.id.entrylist_item_target);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		EntrySummary summary = getItem(position);
		holder.title.setText(summary.toString());
		holder.sponsor.setText(summary.getSponsor());
		holder.schedule.setText(summary.getSchedule());
		holder.target.setVisibility(View.GONE);
		return convertView;
	}

	private class ViewHolder {
		TextView title;
		TextView sponsor;
		TextView schedule;
		TextView target;
	}

	private final class SearchTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected void onPreExecute() {
			textView.setText(R.string.searchLoading);
		}

		@Override
		protected Void doInBackground(Object... params) {
			EntryFilter filter = new EntryFilter(origin);
			for (Object p : params) {
				if (p instanceof Collection<?>) {
					filter.applyFilter((Collection<?>) p);
				} else if (p instanceof String) {
					filter.applyFilter((String) p);
				} else {
					throw new UnsupportedOperationException();
				}
			}
			list = filter.getResult();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			notifyDataSetChanged();
			if (list.size() == 0) {
				textView.setText(R.string.searchNotFound);
			}
		}

	}
}
