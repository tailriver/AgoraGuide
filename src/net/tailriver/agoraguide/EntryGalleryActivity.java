package net.tailriver.agoraguide;

import java.net.URL;

import net.tailriver.agoraguide.AgoraEntry.*;

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

		AgoraData.setApplicationContext(getApplicationContext());

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
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();

		final boolean isFavorite = AgoraData.isFavorite(selectedId);
		menu.findItem(R.id.menu_entrygallery_favorites_add).setVisible(!isFavorite);
		menu.findItem(R.id.menu_entrygallery_favorites_remove).setVisible(isFavorite);

		final MenuItem reserveItem = menu.findItem(R.id.menu_entrygallery_reserve);
		final URL reserveAddress = AgoraData.getEntry(selectedId).getURL(Tag.RESERVE_ADDRESS);
		if (reserveAddress != null) {
			reserveItem.setEnabled(true);
			reserveItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(reserveAddress.toExternalForm())));
		}
		else
			reserveItem.setEnabled(false);

		final MenuItem websiteItem = menu.findItem(R.id.menu_entrygallery_website);
		final URL website = AgoraData.getEntry(selectedId).getURL(Tag.WEBSITE);
		if (website != null) {
			websiteItem.setEnabled(true);
			websiteItem.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(website.toExternalForm())));
		}
		else
			websiteItem.setEnabled(false);

		final MenuItem agoriItem = menu.findItem(R.id.menu_entrygallery_agori);
		final MenuItem agoruItem = menu.findItem(R.id.menu_entrygallery_agoru);
		agoriItem.setEnabled(AgoraData.isConnected());
		agoruItem.setEnabled(AgoraData.isConnected());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String selectedId = ((Gallery) findViewById(R.id.entrygallery)).getSelectedItem().toString();
		switch (item.getItemId()) {
		case R.id.menu_entrygallery_favorites_add:
		case R.id.menu_entrygallery_favorites_remove:
			AgoraData.setFavorite(selectedId, !AgoraData.isFavorite(selectedId));
			return true;

		case R.id.menu_entrygallery_agori:
			final Intent agoriIntent = new Intent(EntryGalleryActivity.this, AgoriActivity.class);
			agoriIntent.putExtra("entryId", selectedId);
			startActivity(agoriIntent);
			return true;
		
		case R.id.menu_entrygallery_agoru:
			final Intent agoruIntent = new Intent(EntryGalleryActivity.this, AgoruActivity.class);
			agoruIntent.putExtra("entryId", selectedId);
			startActivity(agoruIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		final String entryId = getIntent().getStringArrayExtra("entryIdList")[position];

		setTitle(AgoraData.getEntry(entryId).getLocaleTitle());
	}

	@Override
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
