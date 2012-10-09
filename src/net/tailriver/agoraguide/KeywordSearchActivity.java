package net.tailriver.agoraguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;

public class KeywordSearchActivity extends SearchActivity
implements OnClickListener, OnMultiChoiceClickListener, TextWatcher
{
	private Category[] categoryArray;
	private EditText searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.searchbykeyword);

		categoryArray = Category.values().toArray(new Category[Category.values().size()]);

		searchText = (EditText) findViewById(R.id.sbk_text);
		searchText.addTextChangedListener(this);
		searchText.setText(null);
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
		String[]  choices = new String[categoryArray.length];
		boolean[] checked = new boolean[categoryArray.length];
		for (int i = 0; i < categoryArray.length; i++) {
			choices[i] = categoryArray[i].toString();
			checked[i] = searchAdapter.getFilter(categoryArray[i]);
		}

		new AlertDialog.Builder(KeywordSearchActivity.this)
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
				searchAdapter.setFilter(cat, true);
		}

		dialog.dismiss();
		search(null);
	}

	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		searchAdapter.setFilter(categoryArray[which], isChecked);
	}

	private void search(Editable s) {
		if (s == null) {
			s = searchText.getText();
		}

		searchAdapter.clear();
		searchAdapter.add(EntrySummary.getEntryByKeyword(s.toString(), searchAdapter.tellFilter()));
	}
}
