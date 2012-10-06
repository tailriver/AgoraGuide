package net.tailriver.agoraguide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;

public class EntryGalleryActivity extends Activity implements OnItemSelectedListener {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrygallery);

		AgoraDatabase.init(getApplicationContext());

		final String[] entryIdList = getIntent().getStringArrayExtra("entryIdList");
		final int position = getIntent().getIntExtra("position", 0);

		final Gallery gallery = (Gallery) findViewById(R.id.entrygallery);
		gallery.setAdapter(new EntryGalleryAdapter(EntryGalleryActivity.this, entryIdList));
		gallery.setOnItemSelectedListener(this);
		gallery.setSelection(position, false);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("position", ((Gallery) findViewById(R.id.entrygallery)).getSelectedItemPosition());
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(EntryGalleryActivity.this);
		mi.inflate(R.menu.entrygallery, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		EntryDetail detail = new EntryDetail(selectedId);

		final boolean isFavorite = Favorite.isFavorite(detail.getSummary());
		menu.findItem(R.id.menu_entrygallery_favorites_add).setVisible(!isFavorite);
		menu.findItem(R.id.menu_entrygallery_favorites_remove).setVisible(isFavorite);

		final MenuItem reserveItem = menu.findItem(R.id.menu_entrygallery_reserve);
		final String reserveAddress = detail.getDetailValue("original");
		if (reserveAddress != null) {
			reserveItem.setEnabled(true);
			reserveItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(reserveAddress)));
		}
		else
			reserveItem.setEnabled(false);

		final MenuItem websiteItem = menu.findItem(R.id.menu_entrygallery_website);
		final String website = detail.getDetailValue("website");
		if (website != null) {
			websiteItem.setEnabled(true);
			websiteItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
		}
		else
			websiteItem.setEnabled(false);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		EntrySummary summary = EntrySummary.parse(selectedId);
		switch (item.getItemId()) {
		case R.id.menu_entrygallery_favorites_add:
		case R.id.menu_entrygallery_favorites_remove:
			Favorite.setFavorite(summary, !Favorite.isFavorite(summary));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		final String entryId = getIntent().getStringArrayExtra("entryIdList")[position];

		setTitle(EntrySummary.parse(entryId).getTitle());
	}

	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		final View v = ((Gallery) findViewById(R.id.entrygallery)).getSelectedView().findViewById(R.id.entrygallery_scroll);
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN)
			v.scrollBy(0, -20);
		else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN)
			v.scrollBy(0,  20);
		else
			return super.dispatchKeyEvent(event);

		return true;
	}
}
