package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ActArrayAdapter extends ArrayAdapter<String> {
	private LayoutInflater inflater;
	private ArrayList<String> items;

	public ActArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = (ArrayList<String>) objects;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public String getItem(int position) {
		return (String) items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.actlist_item, null);
		}
		AgoraData.Entry act = AgoraData.getEntry(this.getItem(position));
		if (act != null) {
			TextView titleText = (TextView)view.findViewById(R.id.actlist_item_title);
			titleText.setText(act.getTitle());
			titleText.setSingleLine(true);
			titleText.setTextSize(17.0f);
			titleText.setTextColor(Color.WHITE);

			TextView exhibitorText = (TextView)view.findViewById(R.id.actlist_item_exhibitor);
			exhibitorText.setText(act.getExhibitor());
			exhibitorText.setSingleLine(true);
			exhibitorText.setTextColor(Color.GRAY);
			exhibitorText.setTextSize(12.0f);

			TextView scheduleText = (TextView) view.findViewById(R.id.actlist_item_schedule);
			scheduleText.setText(act.getScheduleString());
			scheduleText.setTextColor(Color.GRAY);
			scheduleText.setTextSize(12.0f);
		}
		return view;
	}
}
