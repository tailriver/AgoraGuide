package net.tailriver.agoraguide;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class SearchActivity extends Activity
implements OnItemClickListener
{
	protected SearchResultAdapter searchAdapter;
	protected ListView resultView;
	private int firstVisiblePosition;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		AgoraGuideActivity.initDatabase(getApplicationContext());
		searchAdapter = new SearchResultAdapter(this, R.id.search_result);

		resultView = (ListView) findViewById(R.id.search_result);
		resultView.setAdapter(searchAdapter);
		resultView.setOnItemClickListener(this);
		resultView.setEmptyView(findViewById(R.id.search_not_found));
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		firstVisiblePosition = position;
		EntrySummary summary = (EntrySummary) parent.getItemAtPosition(position);
		Intent intent = new Intent(parent.getContext(), EntryDetailActivity.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		startActivityForResult(intent, R.id.search_result);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == R.id.search_result) {
			resultView.setSelection(firstVisiblePosition);
			resultView.requestFocusFromTouch();	// it is need when DPAD operation and back			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
