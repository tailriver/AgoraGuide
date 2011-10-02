package net.tailriver.agoraguide;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
		setTitle(R.string.favorite);

		final ListView entryListView = (ListView) findViewById(R.id.favorites_entrylist);
		entryListView.setAdapter(new EntryArrayAdapter(FavoritesActivity.this));
		entryListView.setOnItemClickListener(theAdapter());
		entryListView.setEmptyView((TextView) findViewById(R.id.favorites_empty));
	}

	@Override
	public void onResume() {
		super.onStart();
		final List<String> favs = AgoraData.getFavoriteEntryId();
		theAdapter().clear();
		theAdapter().add(favs);

		Log.i("TEST", Boolean.toString(theAdapter().isEmpty()));
	}

	@Override
	public void onPause() {
		super.onPause();
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
			new AlertDialog.Builder(this)
			.setTitle(R.string.clearFavorite)
			.setMessage("Really?")
			.setIcon(R.drawable.icon)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new AgoraData(FavoritesActivity.this).clearFavorite();
					theAdapter().clear();
					findViewById(R.id.favorites_entrylist).invalidate();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// nothing to do
				}
			})
			.create()
			.show();
		}
		return false;
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.favorites_entrylist)).getAdapter();
	}
}