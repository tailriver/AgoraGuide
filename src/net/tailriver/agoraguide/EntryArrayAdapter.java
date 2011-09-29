package net.tailriver.agoraguide;

import java.util.List;

import net.tailriver.agoraguide.AgoraData.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class EntryArrayAdapter extends ArrayAdapter<Entry> {
	private final LayoutInflater inflater;

	public EntryArrayAdapter(Context context, int textViewResourceId,
			List<Entry> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.actlist_item, null);
		}

		AgoraData.Entry entry = this.getItem(position);
		if (entry != null) {
			((TextView) view.findViewById(R.id.actlist_item_title)).setText(entry.getLocaleString(EntryKey.TitleJa));
			((TextView) view.findViewById(R.id.actlist_item_exhibitor)).setText(entry.getLocaleString(EntryKey.ExhibitorJa));
			((TextView) view.findViewById(R.id.actlist_item_schedule)).setText(entry.getString(EntryKey.Schedule));
		}
		return view;
	}
}
