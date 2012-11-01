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

		EntrySummary summary = getItem(position).getSummary();
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
}
