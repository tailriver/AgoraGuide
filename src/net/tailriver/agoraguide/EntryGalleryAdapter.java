package net.tailriver.agoraguide;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import net.tailriver.agoraguide.AgoraData.*;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryGalleryAdapter extends ArrayAdapter<String> {
	private final Context context;

	public EntryGalleryAdapter(Context context) {
		super(context, R.layout.entrydetail, new ArrayList<String>());
		this.context = context;
	}

	public void add(String[] objects) {
		for (String e : objects)
			super.add(e);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.entrydetail, null);
		}

		final String id = getItem(position);
		final Entry entry = AgoraData.getEntry(id);

		final TextView iconView = (TextView) convertView.findViewById(R.id.entrydetail_icon);
		iconView.setText(entry.getId());
		iconView.append(" " + entry.getCategory().toString());

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrydetail_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrydetail_sponsor);
		sponsorView.setText(entry.getString(EntryKey.Sponsor));
		final String coSponsor = entry.getString(EntryKey.CoSponsor);
		if (coSponsor != null)
			sponsorView.append("\n" + coSponsor);

		final TextView abstractView = (TextView) convertView.findViewById(R.id.entrydetail_abstract);
		abstractView.setText(entry.getString(EntryKey.Abstract));

		final TextView contentView = (TextView) convertView.findViewById(R.id.entrydetail_content);
		final String content = entry.getString(EntryKey.Content);
		if (content != null)
			contentView.setText(content.replace("&#xA;", "\n"));
		else
			contentView.setVisibility(View.GONE);

		final TextView reservationView = (TextView) convertView.findViewById(R.id.entrydetail_reservation);
		final String reservation = entry.getString(EntryKey.Reservation);
		if (reservation != null)
			reservationView.setText(reservation);
		else
			reservationView.setVisibility(View.GONE);

		final TextView noteView = (TextView) convertView.findViewById(R.id.entrydetail_note);
		final String note = entry.getString(EntryKey.Note);
		if (note != null)
			noteView.setText(note);
		else
			noteView.setVisibility(View.GONE);

		final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.entrydetail_thumbnail);
		final URL imageURL = entry.getURL(EntryKey.Image);
		if (imageURL != null && new AgoraData(context).isConnected()) {
			HttpURLConnection huc = null;
			try {
				huc = (HttpURLConnection) imageURL.openConnection();
				huc.setDoInput(true);
				huc.connect();
				thumbnail.setImageBitmap(BitmapFactory.decodeStream(huc.getInputStream()));
			}
			catch (Exception e) {
				thumbnail.setVisibility(View.GONE);
			}
			finally {
				if (huc != null)
					huc.disconnect();
			}
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		return convertView;
	}
}
