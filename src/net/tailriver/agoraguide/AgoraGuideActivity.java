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

		AgoraData ad = new AgoraData(this.getApplicationContext());
		ad.XMLUpdater();

		try {
			ad.XMLParser();
		}
		catch (XMLParserAbortException e) {
			Log.e("AGActivity", e.toString());
		}
		ListView actList = (ListView) this.findViewById(R.id.main_actlist);

		EntryArrayAdapter adapter = new EntryArrayAdapter(AgoraGuideActivity.this, AgoraData.getAllEntry());
		actList.setAdapter(adapter);
		actList.setOnItemClickListener(adapter.goToDetail());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(getApplicationContext());
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals(getText(R.string.menu_sbk))) {
			startActivity(new Intent(AgoraGuideActivity.this, SearchByKeywordActivity.class));
			return true;
		}
		Log.i("AGActivity", item.getTitle().toString());
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("MainActivity", "onPause() called");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("MainActivity", "onResume() called");
	}
}
