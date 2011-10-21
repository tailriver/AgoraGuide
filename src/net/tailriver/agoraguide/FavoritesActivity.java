package net.tailriver.agoraguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class FavoritesActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
		setTitle(R.string.favorites);

		AgoraData.setApplicationContext(getApplicationContext());

		final ListView entryListView = (ListView) findViewById(R.id.favorites_entrylist);
		entryListView.setAdapter(new EntryArrayAdapter(FavoritesActivity.this, entryListView.getId()));
		entryListView.setOnItemClickListener(theAdapter());
		entryListView.setEmptyView(findViewById(R.id.favorites_empty));

		theAdapter().add(AgoraData.getFavoriteEntryId());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (theAdapter().getCount() == AgoraData.getFavoriteEntryId().size())
			theAdapter().onActivityResult(requestCode, resultCode, data);
		else {
			theAdapter().clear();
			theAdapter().add(AgoraData.getFavoriteEntryId());
			findViewById(R.id.favorites_entrylist).invalidate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(FavoritesActivity.this);
		mi.inflate(R.menu.favorites, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_favorites_clear).setEnabled( !theAdapter().isEmpty() );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getIntent() != null) {
			startActivity(item.getIntent());
			return true;
		}
		if (item.getItemId() == R.id.menu_favorites_clear) {
			new AlertDialog.Builder(FavoritesActivity.this)
			.setTitle(R.string.clearFavorite)
			.setMessage("Really?")
			.setIcon(R.drawable.icon)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AgoraData.clearFavorite();
					theAdapter().clear();
					findViewById(R.id.favorites_entrylist).invalidate();
				}
			})
			.setNegativeButton("Cancel", null)
			.create()
			.show();
			return true;
		}
		return false;
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.favorites_entrylist)).getAdapter();
	}
}
