package net.tailriver.agoraguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class FavoritesActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);

		AgoraGuideActivity.initDatabase(getApplicationContext());

		final ListView entryListView = (ListView) findViewById(R.id.favorites_entrylist);
		entryListView.setAdapter(new EntryArrayAdapter(FavoritesActivity.this, entryListView.getId()));
		entryListView.setOnItemClickListener(theAdapter());
		entryListView.setEmptyView(findViewById(R.id.favorites_empty));

		theAdapter().add(Favorite.values());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (theAdapter().getCount() == Favorite.values().size())
			theAdapter().onActivityResult(requestCode, resultCode, data);
		else {
			theAdapter().clear();
			theAdapter().add(Favorite.values());
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
		if (item.getItemId() == R.id.menu_favorites_clear) {
			new AlertDialog.Builder(FavoritesActivity.this)
			.setTitle(R.string.clearFavorite)
			.setIcon(android.R.drawable.ic_menu_delete)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, null)
			.create()
			.show();
			return true;
		}
		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
		Favorite.clear();
		theAdapter().clear();
		findViewById(R.id.favorites_entrylist).invalidate();
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.favorites_entrylist)).getAdapter();
	}
}
