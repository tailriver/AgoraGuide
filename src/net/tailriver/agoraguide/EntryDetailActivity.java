package net.tailriver.agoraguide;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryDetailActivity extends AgoraActivity implements OnClickListener {
	public static final String INTENT_ENTRY = "net.tailriver.agoraguide.entry_id";
	public static final String INTENT_NOTIFICATION_ID = "net.tailriver.agoraguide.notification_id";

	private EntrySummary summary;
	private EntryDetail  detail;

	@Override
	public void onPreInitialize() {
		setContentView(R.layout.entry_detail);
		setTitle(R.string.entryDetail);

		int notificationId = getIntent().getIntExtra(INTENT_NOTIFICATION_ID, -1);
		if (notificationId > -1) {
			ScheduleAlarm.cancelNotification(notificationId);
		}
	}

	@Override
	public void onPostInitialize() {
		summary = EntrySummary.get(getIntent().getStringExtra(INTENT_ENTRY));
		detail  = new EntryDetail(summary);

		TextView iconView = (TextView) findViewById(R.id.entrygallery_icon);
		iconView.setText(new SpannableStringBuilder(summary.getId()));
		iconView.append(" ");
		iconView.append(summary.getCategory().toString());

		ImageView favoriteButton = (ImageView) findViewById(R.id.favoriteButton);
		favoriteButton.setOnClickListener(this);
		onClick(favoriteButton);
		onClick(favoriteButton);

		TextView titleView = (TextView) findViewById(R.id.entrygallery_title);
		titleView.setText(summary.toString());

		TextView sponsorView = (TextView) findViewById(R.id.entrygallery_sponsor);
		sponsorView.setText(summary.getSponsor());
		String coSponsor = detail.getDetailValue("cosponsor");
		if (coSponsor != null) {
			sponsorView.append("\n");
			sponsorView.append(coSponsor);
		}

		TextView scheduleView = (TextView) findViewById(R.id.entrygallery_schedule);
		scheduleView.setText(summary.getSchedule());

		CharSequence delimiter = "\n\n";
		SpannableStringBuilder text = new SpannableStringBuilder();
		for (String tag : new String[]{ "abstract", "content", "guest", "reservation", "note" }) {
			String tagValue = detail.getDetailValue(tag);
			if (tagValue != null && tagValue.length() != 0) {
				SpannableStringBuilder section = new SpannableStringBuilder(tag).append("\n");
				section.setSpan(new BackgroundColorSpan(Color.LTGRAY), 0, section.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.append(section).append(tagValue).append(delimiter);
			}
		}

		if (text.length() > delimiter.length()) {
			text.delete(text.length() - delimiter.length(), text.length());
		}

		TextView detailView = (TextView) findViewById(R.id.entrygallery_content);
		detailView.setText(text);

		findViewById(R.id.mapButton).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(this);
		mi.inflate(R.menu.entrygallery, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem reserveItem = menu.findItem(R.id.menu_entrygallery_reserve);
		String reserveAddress = detail.getDetailValue("original");
		if (reserveAddress != null) {
			reserveItem.setEnabled(true);
			reserveItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(reserveAddress)));
		}
		else
			reserveItem.setEnabled(false);

		MenuItem websiteItem = menu.findItem(R.id.menu_entrygallery_website);
		String website = detail.getDetailValue("website");
		if (website != null) {
			websiteItem.setEnabled(true);
			websiteItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
		}
		else
			websiteItem.setEnabled(false);

		return super.onPrepareOptionsMenu(menu);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.favoriteButton) {
			ImageView favoriteButton = (ImageView) v;
			if (!Favorite.isFavorite(summary)) {
				Favorite.setFavorite(summary, true);
				favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
				favoriteButton.setContentDescription(getString(R.string.removeFavorite));
			} else {
				Favorite.setFavorite(summary, false);
				favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
				favoriteButton.setContentDescription(getString(R.string.addFavorite));
			}
		}

		if (v.getId() == R.id.mapButton) {
			ScheduleAlarm.setAlerm(summary, System.currentTimeMillis() + 10000);
			Intent intent = new Intent(getApplicationContext(), MapActivity.class);
			intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
			startActivity(intent);
		}
	}
}
