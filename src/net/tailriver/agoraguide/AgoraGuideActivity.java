package net.tailriver.agoraguide;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class AgoraGuideActivity extends AgoraActivity implements OnClickListener {
	private static Context applicationContext;

	@Override
	public void onPreInitialize() {
		setContentView(R.layout.main);
		applicationContext = getApplicationContext();

		ViewGroup vg = ((ViewGroup) findViewById(R.id.main_buttons));
		for (int i = 0, max = vg.getChildCount(); i < max; i++) {
			vg.getChildAt(i).setOnClickListener(this);
		}		
		vg.setEnabled(false);
	}

	@Override
	public void onPostInitialize() {
		ViewGroup vg = ((ViewGroup) findViewById(R.id.main_buttons));
		vg.setEnabled(true);
	}

	public void onClick(View v) {
		try {
			jumpNextActivity(v.getId());
		} catch (UnsupportedOperationException e) {
			Toast.makeText(applicationContext, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			jumpNextActivity(item.getItemId());
			return true;
		} catch (UnsupportedOperationException e) {
			Toast.makeText(applicationContext, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private final void jumpNextActivity(int resID) {
		Intent intent = new Intent();
		switch (resID) {
		case R.id.button_search_keyword:
		case R.id.menu_sbk:
			intent.setClass(applicationContext, SearchActivity.class);
			intent.putExtra(SearchActivity.INTENT_SEARCH_TYPE, SearchActivity.SearchType.Keyword);
			break;
		case R.id.button_search_schedule:
		case R.id.menu_sbs:
			intent.setClass(applicationContext, SearchActivity.class);
			intent.putExtra(SearchActivity.INTENT_SEARCH_TYPE, SearchActivity.SearchType.Schedule);
			break;
		case R.id.button_search_area:
		case R.id.menu_sbm:
			intent.setClass(applicationContext, AreaSearchIndexActivity.class);
			break;
		case R.id.button_search_favorite:
		case R.id.menu_favorites:
			intent.setClass(applicationContext, SearchActivity.class);
			intent.putExtra(SearchActivity.INTENT_SEARCH_TYPE, SearchActivity.SearchType.Favorite);
			break;
		case R.id.button_information:
		case R.id.menu_credits:
			intent.setClass(applicationContext, CreditsActivity.class);
			break;
		default:
			throw new UnsupportedOperationException("invalid call");
		}
		startActivity(intent);
	}
}
