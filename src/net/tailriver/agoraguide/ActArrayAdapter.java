package net.tailriver.agoraguide;

import java.util.List;

import net.tailriver.agoraguide.AgoraData.*;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class ActArrayAdapter extends ArrayAdapter<Entry> {
	private LayoutInflater inflater;
	private List<Entry> items;

	public ActArrayAdapter(Context context, int textViewResourceId, List<Entry> arrayList) {
		super(context, textViewResourceId, (List<Entry>) null);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = arrayList;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Entry getItem(int position) {
		return items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.actlist_item, null);
		}
		AgoraData.Entry entry = this.getItem(position);
		if (entry != null) {
			TextView titleText = (TextView)view.findViewById(R.id.actlist_item_title);
			titleText.setText(entry.getLocaleString(EntryKey.TitleJa));
			titleText.setSingleLine(true);
			titleText.setTextSize(17.0f);
			titleText.setTextColor(Color.WHITE);

			TextView exhibitorText = (TextView)view.findViewById(R.id.actlist_item_exhibitor);
			exhibitorText.setText(entry.getLocaleString(EntryKey.ExhibitorJa));
			exhibitorText.setSingleLine(true);
			exhibitorText.setTextColor(Color.GRAY);
			exhibitorText.setTextSize(12.0f);

			TextView scheduleText = (TextView) view.findViewById(R.id.actlist_item_schedule);
			scheduleText.setText(entry.getString(EntryKey.Schedule));
			scheduleText.setTextColor(Color.GRAY);
			scheduleText.setTextSize(12.0f);
		}
		return view;
	}
}
