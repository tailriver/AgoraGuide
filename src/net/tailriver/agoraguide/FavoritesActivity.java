package net.tailriver.agoraguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FavoritesActivity extends SearchActivity implements OnClickListener {
	@Override
	public void onPreInitialize() {
		setContentView(R.layout.favorites);
	}

	@Override
	public void onPostInitialize() {
		searchAdapter.addAll(new EntryFilter().addFavoriteEntry().getResult());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (searchAdapter.getCount() != Favorite.values().size()) {
			searchAdapter.clear();
			searchAdapter.addAll(new EntryFilter().addFavoriteEntry().getResult());
			resultView.invalidate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(this);
		mi.inflate(R.menu.favorites, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_favorites_clear).setEnabled( !searchAdapter.isEmpty() );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_favorites_clear) {
			new AlertDialog.Builder(this)
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
		searchAdapter.clear();
		resultView.invalidate();
	}
}
