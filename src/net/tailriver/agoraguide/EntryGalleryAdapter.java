package net.tailriver.agoraguide;

import java.net.HttpURLConnection;
import java.net.URL;

import net.tailriver.agoraguide.Entry.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryGalleryAdapter extends ArrayAdapter<String> {
	private final static int textViewResourceId = R.layout.entrygallery_item;
	private final Context context;
	private final int adapterViewResourceId;

	public EntryGalleryAdapter(Context context, int adapterViewResourceId, String[] objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.adapterViewResourceId = adapterViewResourceId;
	}

	/** like AdapterView */
	public int getSelectedItemPosition() {
		return ((AdapterView<?>) ((Activity) context).findViewById(adapterViewResourceId)).getSelectedItemPosition();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		final String id = getItem(position);
		final Entry entry = AgoraData.getEntry(id);

		final TextView iconView = (TextView) convertView.findViewById(R.id.entrydetail_icon);
		iconView.setText(entry.getId());
		iconView.append(" " + entry.getCategory().toString());

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrydetail_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrydetail_sponsor);
		sponsorView.setText(entry.getString(Tag.Sponsor));
		final String coSponsor = entry.getString(Tag.CoSponsor);
		if (coSponsor != null)
			sponsorView.append("\n" + coSponsor);

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrygallery_schedule);
		scheduleView.setText(entry.getColoredSchedule());

		final SpannableString abstractTag	 = getBackgroundColorSpannableString("Abstract", Color.LTGRAY);
		final SpannableString contentTag	 = getBackgroundColorSpannableString("Content", Color.LTGRAY);
		final SpannableString guestTag		 = getBackgroundColorSpannableString("Guest", Color.LTGRAY);
		final SpannableString reservationTag = getBackgroundColorSpannableString("Reservation", Color.LTGRAY);
		final SpannableString noteTag		 = getBackgroundColorSpannableString("Note", Color.LTGRAY);

		final SpannableStringBuilder text = new SpannableStringBuilder(abstractTag).append('\n').append(entry.getString(Tag.Abstract).replace("&#xA;", "\n"));
		final String content = entry.getString(Tag.Content);
		final String guest = entry.getString(Tag.Guest);
		final String reservation = entry.getString(Tag.Reservation);
		final String note = entry.getString(Tag.Note);

		if (content != null)
			text.append("\n\n").append(contentTag).append("\n").append(content.replace("&#xA;", "\n"));

		if (guest != null)
			text.append("\n\n").append(guestTag).append("\n").append(guest.replace("&#xA;", "\n"));

		if (reservation != null)
			text.append("\n\n").append(reservationTag).append("\n").append(reservation.replace("&#xA;", "\n"));

		if (note != null)
			text.append("\n\n").append(noteTag).append("\n").append(note.replace("&#xA;", "\n"));

		final TextView scrollView = (TextView) convertView.findViewById(R.id.entrygallery_content);
		scrollView.setText(text);

		final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.entrydetail_thumbnail);
		final URL imageURL = entry.getURL(Tag.Image);
		if (imageURL != null && AgoraData.isConnected(context)) {
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

	public SpannableString getBackgroundColorSpannableString(CharSequence source, int color) {
		SpannableString ss = new SpannableString(source);
		ss.setSpan(new BackgroundColorSpan(color), 0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
}
