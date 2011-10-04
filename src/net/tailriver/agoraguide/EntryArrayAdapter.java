package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import net.tailriver.agoraguide.AgoraData.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

class EntryArrayAdapter extends ArrayAdapter<String> implements OnItemClickListener {
	private final Context context;
	private final List<String> idList;

	public EntryArrayAdapter(Context context) {
		super(context, R.layout.entrylist_item, (List<String>) new ArrayList<String>());
		this.context = context;
		this.idList = new ArrayList<String>();
	}

	public void add(List<String> objects) {
		for (String e : objects) {
			idList.add(e);
			super.add(e);
		}
	}

	@Override
	public void clear() {
		idList.clear();
		super.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.entrylist_item, null);
		}

		final Entry entry = AgoraData.getEntry(getItem(position));

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrylist_item_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrylist_item_sponsor);
		sponsorView.setText(entry.getString(EntryKey.Sponsor));

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrylist_item_schedule);
		scheduleView.setText(entry.getString(EntryKey.Schedule));

		final TextView categoryView = (TextView) convertView.findViewById(R.id.entrylist_item_category);
		categoryView.setText(entry.getCategory().toString());

		final TextView reservaionView = (TextView) convertView.findViewById(R.id.entrylist_item_reservation);
		if (entry.getString(EntryKey.Reservation) == null)
			reservaionView.setVisibility(View.INVISIBLE);

		return convertView;
	}

	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(context, EntryDetailActivity.class);
		intent.putExtra("entryIdList", idList.toArray(new String[idList.size()]));
		intent.putExtra("position", position);
		intent.putExtra("adapterView", parent.getId());
		((Activity) context).startActivityForResult(intent, R.layout.entrylist_item);
	}

	// TODO
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != R.layout.entrylist_item || resultCode != Activity.RESULT_OK)
			return;

		int position = data.getIntExtra("position", 0);
		int adapterViewId = data.getIntExtra("adapterView", -1);
		if (adapterViewId != -1 && position != AdapterView.INVALID_POSITION)
			((AdapterView<?>) ((Activity) context).findViewById(adapterViewId)).setSelection(position);
	}
}
