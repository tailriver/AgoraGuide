package net.tailriver.agoraguide;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

public class SearchByKeywordActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchbykeyword);

		ListView entryList = (ListView) this.findViewById(R.id.sbk_result);
		EditText keywordText = (EditText) this.findViewById(R.id.sbk_text);

		EntryArrayAdapter adapter = new EntryArrayAdapter(SearchByKeywordActivity.this, new ArrayList<AgoraData.Entry>());
		entryList.setAdapter(adapter);
		entryList.setOnItemClickListener(adapter.goToDetail());

		keywordText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				EntryArrayAdapter adapter = (EntryArrayAdapter) ((ListView) findViewById(R.id.sbk_result)).getAdapter();
				if (s.length() == 0)
					adapter.clear();
				else
					adapter.resetEntry(AgoraData.getEntryByKeyword(s.toString()));
			}
		});
	}
}
