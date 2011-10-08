package net.tailriver.agoraguide;

import java.net.URL;

import net.tailriver.agoraguide.Entry.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

		final String[] entryIdList = getIntent().getStringArrayExtra("entryIdList");
		final int position = getIntent().getIntExtra("position", 0);

		final Gallery gallery = (Gallery) findViewById(R.id.entrygallery);
		gallery.setAdapter(new EntryGalleryAdapter(EntryGalleryActivity.this, gallery.getId(), entryIdList));
		gallery.setOnItemSelectedListener(this);
		gallery.setSelection(position, false);
	}

	@Override
	public void onBackPressed() {
		Intent intent = getIntent();
		intent.putExtra("position", theAdapter().getSelectedItemPosition());
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
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();

		final MenuItem favoriteItem = menu.findItem(R.id.menu_entrygallery_favorite);
		if (!AgoraData.isFavorite(selectedId))
			favoriteItem.setTitle(R.string.addFavorite);
		else
			favoriteItem.setTitle(R.string.removeFavorite);

		final MenuItem websiteItem = menu.findItem(R.id.menu_entrygallery_website);
		final URL website = AgoraData.getEntry(selectedId).getURL(Tag.Website);
		if (website != null) {
			websiteItem.setEnabled(true);
			websiteItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(website.toExternalForm())));
		}
		else
			websiteItem.setEnabled(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		if (item.getItemId() == R.id.menu_entrygallery_favorite) {
			new AgoraData(this).setFavorite(selectedId, !AgoraData.isFavorite(selectedId));
			return false;
		}
		return false;
	}

	private EntryGalleryAdapter theAdapter() {
		return (EntryGalleryAdapter) ((Gallery) findViewById(R.id.entrygallery)).getAdapter();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
		final String entryId = getIntent().getStringArrayExtra("entryIdList")[position];

		setTitle(AgoraData.getEntry(entryId).getLocaleTitle());
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
