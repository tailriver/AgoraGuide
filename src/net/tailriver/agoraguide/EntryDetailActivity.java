package net.tailriver.agoraguide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Gallery;

public class EntryDetailActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrygallery);

		final String[] entryIdList = getIntent().getStringArrayExtra("entryIdList");
		final int position = getIntent().getIntExtra("position", 0);

		final Gallery gallery = (Gallery) findViewById(R.id.entrygallery);
		gallery.setAdapter(new EntryGalleryAdapter(EntryDetailActivity.this));

		theAdapter().add(entryIdList);		
		gallery.setSelection(position, true);
	}

	@Override
	public void onBackPressed() {
		Intent intent = getIntent();
		intent.putExtra("position", ((Gallery) findViewById(R.id.entrygallery)).getSelectedItemPosition());
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(EntryDetailActivity.this);
		mi.inflate(R.menu.entrydetail, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem favoriteItem = menu.findItem(R.id.menu_entrydetail_favorite);
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		if (!AgoraData.isFavorite(selectedId))
			favoriteItem.setTitle(R.string.addFavorite);
		else
			favoriteItem.setTitle(R.string.removeFavorite);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		if (item.getItemId() == R.id.menu_entrydetail_favorite) {
			new AgoraData(this).setFavorite(selectedId, !AgoraData.isFavorite(selectedId));
			return false;
		}
		return false;
	}

	private EntryGalleryAdapter theAdapter() {
		return (EntryGalleryAdapter) ((Gallery) findViewById(R.id.entrygallery)).getAdapter();
	}
}
