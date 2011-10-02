package net.tailriver.agoraguide;

import net.tailriver.agoraguide.AgoraData.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class AgoraGuideActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final AgoraData ad = new AgoraData(this);
		ad.XMLUpdater();

		try {
			ad.XMLParser();
		}
		catch (XMLParserAbortException e) {
			Log.e("AGActivity", e.toString());
		}

		final ListView entryListView = (ListView) findViewById(R.id.main_entrylist);
		entryListView.setAdapter(new EntryArrayAdapter(AgoraGuideActivity.this));
		entryListView.setOnItemClickListener(theAdapter());
	}

	@Override
	public void onStart() {
		super.onStart();
		theAdapter().add(AgoraData.getAllEntryId());
	}

	@Override
	public void onStop() {
		theAdapter().clear();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);

		menu.findItem(R.id.menu_sbk).setIntent(new Intent(AgoraGuideActivity.this, SearchByKeywordActivity.class));
		menu.findItem(R.id.menu_sbs).setEnabled(false);
		menu.findItem(R.id.menu_sbm).setEnabled(false);
		menu.findItem(R.id.menu_favorite).setIntent(new Intent(AgoraGuideActivity.this, FavoritesActivity.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getIntent() != null) {
			startActivity(item.getIntent());
			return true;
		}
		return false;
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.main_entrylist)).getAdapter();
	}
}
