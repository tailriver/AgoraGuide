package net.tailriver.agoraguide;

import java.util.List;

import net.tailriver.agoraguide.AgoraData.*;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

class EntryArrayAdapter extends ArrayAdapter<Entry> {
	private final LayoutInflater inflater;
	private final OnItemClickListener onItemClickListener;

	public EntryArrayAdapter(final Context context,	List<Entry> objects) {
		super(context, R.layout.actlist_item, objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		onItemClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				Intent intent = new Intent(context, EntryDetailActivity.class);
				intent.putExtra("id", ((AgoraData.Entry) listView.getItemAtPosition(position)).getId());
				context.startActivity(intent);
			}
		};
	}

	public void resetEntry(List<Entry> objects) {
		super.clear();
		for (Entry e : objects)
			super.add(e);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.actlist_item, null);
		}

		AgoraData.Entry entry = this.getItem(position);
		if (entry != null) {
			((TextView) view.findViewById(R.id.actlist_item_title)).setText(entry.getLocaleTitle());
			((TextView) view.findViewById(R.id.actlist_item_exhibitor)).setText(entry.getString(EntryKey.Sponsor));
			((TextView) view.findViewById(R.id.actlist_item_schedule)).setText(entry.getString(EntryKey.Schedule));
		}
		return view;
	}

	public OnItemClickListener goToDetail() {
		return onItemClickListener;
	}
}
