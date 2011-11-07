package net.tailriver.agoraguide;

import java.util.List;

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

public class AgoriAdapter extends ArrayAdapter<Agori> implements OnItemClickListener {
	private static final int textViewResourceId = R.layout.agori_item;

	private final Context context;
	private final List<Agori> objects;

	public AgoriAdapter(Context context, List<Agori> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		final Agori one = objects.get(position);

		final TextView titleView = (TextView) convertView.findViewById(R.id.agori_item_title);
		final String title = AgoraData.getEntry(one.getId()).getLocaleTitle();
		titleView.setText(one.getId() + " " + title);

		final TextView commentView = (TextView) convertView.findViewById(R.id.agori_item_comment);
		commentView.setText(one.getComment());

		final String name = one.getName();
		final TextView nameView = (TextView) convertView.findViewById(R.id.agori_item_name);
		if (name.length() > 0)
			nameView.setText(one.getName());
		else
			nameView.setVisibility(View.GONE);

		return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		final String[] ids = new String[objects.size()];
		for (int i = 0; i < objects.size(); i++)
			ids[i] = objects.get(i).getId();

		Intent intent = new Intent(context, EntryGalleryActivity.class);
		intent.putExtra("entryIdList", ids);
		intent.putExtra("position", position);
		((Activity) context).startActivityForResult(intent, textViewResourceId);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != textViewResourceId || resultCode != Activity.RESULT_OK)
			return;

		final int position = data.getIntExtra("position", 0);
		if (position == AdapterView.INVALID_POSITION)
			return;

		final AdapterView<?> view = (AdapterView<?>) ((Activity) context).findViewById(R.id.agori_list);
		view.requestFocusFromTouch();	// it is need when DPAD operation and back
		view.setSelection(position);
	}
}
