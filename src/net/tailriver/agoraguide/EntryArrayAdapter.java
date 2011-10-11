package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import net.tailriver.agoraguide.Entry.*;

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

public class EntryArrayAdapter extends ArrayAdapter<String> implements ListAdapter, OnItemClickListener {
	private final static int textViewResourceId = R.layout.entrylist_item;
	private final Context context;
	private final int adapterViewResourceId;
	private final List<String> objects;			// it shares reference with private list of the superclass

	public EntryArrayAdapter(Context context, int adapterViewResourceId) {
		this(context, adapterViewResourceId, new ArrayList<String>());
	}

	private EntryArrayAdapter(Context context, int adapterViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.adapterViewResourceId = adapterViewResourceId;
		this.objects = objects;
	}

	public void add(List<String> objects) {
		for (String e : objects)
			super.add(e);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		final Entry entry = AgoraData.getEntry(getItem(position));

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrylist_item_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrylist_item_sponsor);
		sponsorView.setText(entry.getString(Tag.Sponsor));

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrylist_item_schedule);
		scheduleView.setText(entry.getColoredSchedule());

		final TextView categoryView = (TextView) convertView.findViewById(R.id.entrylist_item_category);
		categoryView.setText(entry.getCategory().toString());

		final TextView reservaionView = (TextView) convertView.findViewById(R.id.entrylist_item_reservation);
		if (entry.getString(Tag.Reservation) == null)
			reservaionView.setVisibility(View.INVISIBLE);

		return convertView;
	}

	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(context, EntryGalleryActivity.class);
		intent.putExtra("entryIdList", objects.toArray(new String[objects.size()]));
		intent.putExtra("position", position);
		((Activity) context).startActivityForResult(intent, textViewResourceId);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != textViewResourceId || resultCode != Activity.RESULT_OK)
			return;

		final int position = data.getIntExtra("position", 0);
		if (position == AdapterView.INVALID_POSITION)
			return;

		final AdapterView<?> view = (AdapterView<?>) ((Activity) context).findViewById(adapterViewResourceId);
		view.requestFocusFromTouch();	// it is need when DPAD operation and back
		view.setSelection(position);
	}
}
