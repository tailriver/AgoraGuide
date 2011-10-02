package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;

import net.tailriver.agoraguide.AgoraData.*;

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

		final Entry entry = AgoraData.getEntry(this.getItem(position));
		if (entry != null) {
			((TextView) convertView.findViewById(R.id.entrylist_item_title)).setText(entry.getLocaleTitle());
			((TextView) convertView.findViewById(R.id.entrylist_item_sponsor)).setText(entry.getString(EntryKey.Sponsor));
			((TextView) convertView.findViewById(R.id.entrylist_item_schedule)).setText(entry.getString(EntryKey.Schedule));
			((TextView) convertView.findViewById(R.id.entrylist_item_category)).setText(entry.getCategory().toString());

			// FIXME not worked
			TextView reservaionView = (TextView) convertView.findViewById(R.id.entrylist_item_reservation);
			if (entry.getString(EntryKey.Reservation) == null)
				reservaionView.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(context, EntryDetailActivity.class);
		intent.putExtra("entryIdList", idList.toArray(new String[idList.size()]));
		intent.putExtra("position", position);
		context.startActivity(intent);	// TODO return value?
	}
}
