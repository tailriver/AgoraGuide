package net.tailriver.agoraguide;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;

public class KeywordSearchActivity extends SearchActivity
implements OnClickListener, OnMultiChoiceClickListener, TextWatcher
{
	private EditText searchText;
	private Category[] category;
	private boolean[] categoryChecked;

	@Override
	public void onPreInitialize() {
		setContentView(R.layout.searchbykeyword);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void onPostInitialize() {
		category = Category.values().toArray(new Category[Category.values().size()]);
		categoryChecked = new boolean[category.length];
		Arrays.fill(categoryChecked, true);

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
		String[] categoryName = new String[category.length];
		for (int i = 0; i < category.length; i++) {
			categoryName[i] = category[i].toString();
		}
		new AlertDialog.Builder(KeywordSearchActivity.this)
		.setTitle(R.string.filtering)
		.setIcon(android.R.drawable.ic_search_category_default)
		.setMultiChoiceItems(categoryName, categoryChecked, this)
		.setPositiveButton(android.R.string.ok, this)
		.setNeutralButton(R.string.selectAll, this)
		.create()
		.show();

		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == AlertDialog.BUTTON_NEUTRAL) {
			for (int i = 0; i < categoryChecked.length; i++) {
				categoryChecked[i] = true;
			}
		}

		dialog.dismiss();
		search(null);
	}

	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		categoryChecked[which] = isChecked;
	}

	private void search(Editable s) {
		if (s == null) {
			s = searchText.getText();
		}

		Set<Category> categoryFilter = new HashSet<Category>();
		for (int i = 0; i < category.length; i++) {
			if (categoryChecked[i]) {
				categoryFilter.add(category[i]);
			}
		}

		List<EntrySummary> result = new EntryFilter()
		.addAllEntry()
		.applyFilter(categoryFilter)
		.applyFilter(s)
		.getResult();

		searchAdapter.clear();
		searchAdapter.addAll(result);
	}
}
