package net.tailriver.agoraguide;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ListView;

public class SearchByKeywordActivity extends Activity implements TextWatcher, OnMultiChoiceClickListener, OnClickListener {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchbykeyword);

		AgoraDatabase.init(getApplicationContext());

		final ListView entryList = (ListView) findViewById(R.id.sbk_result);
		entryList.setAdapter(new EntryArrayAdapter(SearchByKeywordActivity.this, entryList.getId()));
		entryList.setOnItemClickListener(theAdapter());
		entryList.setEmptyView(findViewById(R.id.sbk_empty));

		final EditText editText = (EditText) findViewById(R.id.sbk_text);
		editText.addTextChangedListener(this);
		editText.setText(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		search(null);
		theAdapter().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void afterTextChanged(Editable s) {
		search(s);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		List<Category> categoryList = Category.values();
		String[] choices  = new String[categoryList.size()];
		boolean[] checked = new boolean[categoryList.size()];
		for (int i = 0; i < categoryList.size(); i++) {
			choices[i] = categoryList.get(i).getName();
			checked[i] = theAdapter().getFilter(categoryList.get(i));
		}

		new AlertDialog.Builder(SearchByKeywordActivity.this)
		.setTitle(R.string.filtering)
		.setIcon(android.R.drawable.ic_search_category_default)
		.setMultiChoiceItems(choices, checked, this)
		.setPositiveButton(android.R.string.ok, this)
		.setNeutralButton(R.string.selectAll, this)
		.create()
		.show();

		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == AlertDialog.BUTTON_NEUTRAL) {
			for (Category cat : Category.values())
				theAdapter().setFilter(cat, true);
		}

		dialog.dismiss();
		search(null);
	}

	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		theAdapter().setFilter(Category.values().get(which), isChecked);
	}

	private void search(Editable s) {
		if (s == null)
			s = ((EditText) findViewById(R.id.sbk_text)).getText();
		theAdapter().clear();
		theAdapter().add(EntrySummary.getEntryByKeyword(s.toString(), theAdapter().tellFilter()));	
	}

	private EntryArrayAdapter theAdapter() {
		return (EntryArrayAdapter) ((ListView) findViewById(R.id.sbk_result)).getAdapter();
	}
}
