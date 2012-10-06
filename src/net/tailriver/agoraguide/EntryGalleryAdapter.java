package net.tailriver.agoraguide;

import android.content.Context;
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

		String id = getItem(position);
		EntryDetail detail   = new EntryDetail(id);
		EntrySummary summary = detail.getSummary();

		final TextView iconView = (TextView) convertView.findViewById(R.id.entrygallery_icon);
		iconView.setText(id);
		iconView.append(" " + summary.getCategory().getName());

		final TextView titleView = (TextView) convertView.findViewById(R.id.entrygallery_title);
		titleView.setText(summary.getTitle());

		final TextView sponsorView = (TextView) convertView.findViewById(R.id.entrygallery_sponsor);
		sponsorView.setText(summary.getSponsor());
		final String coSponsor = detail.getDetailValue("cosponsor");
		if (coSponsor != null)
			sponsorView.append("\n" + coSponsor);

		final TextView scheduleView = (TextView) convertView.findViewById(R.id.entrygallery_schedule);
		scheduleView.setText(summary.getSchedule());

		final CharSequence delimiter = "\n\n";
		final SpannableStringBuilder text = new SpannableStringBuilder();
		for (String tag : new String[]{ "abstract", "content", "guest", "reservation", "note" }) {
			final String tagValue = detail.getDetailValue(tag);
			if (tagValue != null) {
				final SpannableString section = new SpannableString(tag + "\n");
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
		thumbnail.setVisibility(View.GONE);
//		final URL imageURL = entry.getURL(Tag.IMAGE);
//		if (imageURL != null) {
//			try {
//				HttpClient ahc = new HttpClient(imageURL);
//				Bitmap bm = ahc.getBitmap();
//				thumbnail.setImageBitmap(bm);
//			} catch (IOException e) {
//				thumbnail.setVisibility(View.GONE);
//			}
//		}
//		else {
//			thumbnail.setVisibility(View.GONE);
//		}

		return convertView;
	}
}
