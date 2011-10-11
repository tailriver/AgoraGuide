package net.tailriver.agoraguide;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import net.tailriver.agoraguide.Entry.*;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryGalleryAdapter extends ArrayAdapter<String> {
	private final static int textViewResourceId;
	private final static Map<Tag, SpannableString> tagName;
	private final Context context;

	static {
		textViewResourceId = R.layout.entrygallery_item;

		tagName = new EnumMap<Tag, SpannableString>(Tag.class);
		tagName.put(Tag.Abstract,		getBackgroundColorSpannableString("Abstract\n", Color.LTGRAY));
		tagName.put(Tag.Content,		getBackgroundColorSpannableString("Content\n", Color.LTGRAY));
		tagName.put(Tag.Guest,			getBackgroundColorSpannableString("Guest\n", Color.LTGRAY));
		tagName.put(Tag.Reservation,	getBackgroundColorSpannableString("Reservation\n", Color.LTGRAY));
		tagName.put(Tag.Note,			getBackgroundColorSpannableString("Note\n", Color.LTGRAY));
	}

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
		final Entry entry = AgoraData.getEntry(id);

		final TextView iconView = (TextView) convertView.findViewById(R.id.entrygallery_icon);
		iconView.setText(entry.getId());
		iconView.append(" " + entry.getCategory().toString());

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrygallery_title);
		titleView.setText(entry.getLocaleTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrygallery_sponsor);
		sponsorView.setText(entry.getString(Tag.Sponsor));
		final String coSponsor = entry.getString(Tag.CoSponsor);
		if (coSponsor != null)
			sponsorView.append("\n" + coSponsor);

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrygallery_schedule);
		scheduleView.setText(entry.getColoredSchedule());

		final SpannableStringBuilder text = new SpannableStringBuilder(tagName.get(Tag.Abstract)).append(entry.getString(Tag.Abstract));
		for (Tag tag : new Tag[]{Tag.Content, Tag.Guest, Tag.Reservation, Tag.Note}) {
			final String tagValue = entry.getString(tag);
			if (tagValue != null)
				text.append("\n\n").append(tagName.get(tag)).append(tagValue);
		}

		final TextView scrollView = (TextView) convertView.findViewById(R.id.entrygallery_content);
		scrollView.setText(text);

		final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.entrygallery_image);
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

	private static SpannableString getBackgroundColorSpannableString(CharSequence source, int color) {
		final SpannableString ss = new SpannableString(source);
		ss.setSpan(new BackgroundColorSpan(color), 0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
}
