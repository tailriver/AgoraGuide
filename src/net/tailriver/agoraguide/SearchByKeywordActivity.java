package net.tailriver.agoraguide;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchByKeywordActivity extends Activity implements TextWatcher {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchbykeyword);
		setTitle(R.string.searchByKeyword);

		final ListView entryList = (ListView) findViewById(R.id.sbk_result);
		entryList.setAdapter(new EntryArrayAdapter(SearchByKeywordActivity.this));
		entryList.setOnItemClickListener(theAdapter());

		final EditText editText = (EditText) findViewById(R.id.sbk_text);
		editText.addTextChangedListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();
		final String keyword = ((EditText) findViewById(R.id.sbk_text)).getText().toString();
		if (keyword.length() != 0)
			theAdapter().add(AgoraData.getEntryByKeyword(keyword));

		if (theAdapter().isEmpty()) {
			TextView emptyView = new TextView(this);
			emptyView.setText("None");
			((ListView) findViewById(R.id.sbk_result)).setEmptyView(emptyView);
		}
	}

	@Override
	public void onStop() {
		theAdapter().clear();
		super.onStop();
	}

	@Override
	public void afterTextChanged(Editable s) {
		theAdapter().clear();
		if (s.length() != 0)
			theAdapter().add(AgoraData.getEntryByKeyword(s.toString()));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.sbk_result)).getAdapter();
	}
}
