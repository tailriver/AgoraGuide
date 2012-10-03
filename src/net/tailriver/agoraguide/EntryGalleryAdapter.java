package net.tailriver.agoraguide;

import java.io.IOException;
import java.net.URL;

import net.tailriver.agoraguide.AgoraEntry.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryGalleryAdapter extends ArrayAdapter<String> {
	private final static int textViewResourceId = R.layout.entrygallery_item;
	private final Context context;

	public EntryGalleryAdapter(Context context, String[] objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);
		}

		final String id = getItem(position);
		final AgoraEntry entry = AgoraData.getEntry(id);

		final TextView iconView = (TextView) convertView.findViewById(R.id.entrygallery_icon);
		iconView.setText(id);
		iconView.append(" " + entry.getCategory().toString());

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrygallery_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrygallery_sponsor);
		sponsorView.setText(entry.getString(Tag.SPONSOR));
		final String coSponsor = entry.getString(Tag.CO_SPONSOR);
		if (coSponsor != null)
			sponsorView.append("\n" + coSponsor);

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrygallery_schedule);
		scheduleView.setText(entry.getSchedule());

		final CharSequence delimiter = "\n\n";
		final SpannableStringBuilder text = new SpannableStringBuilder();
		for (Tag tag : new Tag[]{Tag.ABSTRACT, Tag.CONTENT, Tag.GUEST, Tag.RESERVATION, Tag.NOTE}) {
			final String tagValue = entry.getString(tag);
			if (tagValue != null) {
				final SpannableString section = new SpannableString(tag.toString() + "\n");
				section.setSpan(new BackgroundColorSpan(Color.LTGRAY), 0, section.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.append(section).append(tagValue).append(delimiter);
			}
		}

		if (text.length() > delimiter.length()) {
			text.delete(text.length() - delimiter.length(), text.length());
		}

		final TextView scrollView = (TextView) convertView.findViewById(R.id.entrygallery_content);
		scrollView.setText(text);

		final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.entrygallery_image);
		final URL imageURL = entry.getURL(Tag.IMAGE);
		if (imageURL != null) {
			try {
				AgoraHttpClient ahc = new AgoraHttpClient(context, imageURL);
				Bitmap bm = ahc.getBitmap();
				thumbnail.setImageBitmap(bm);
			} catch (IOException e) {
				thumbnail.setVisibility(View.GONE);
			}
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		return convertView;
	}
}
