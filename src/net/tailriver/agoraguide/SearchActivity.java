package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SearchActivity extends AgoraActivity
implements OnClickListener, OnItemClickListener, OnItemSelectedListener,
OnMultiChoiceClickListener, TextWatcher
{
	public static final String INTENT_SEARCH_TYPE = "net.tailriver.agoraguide.search_type";
	public static final String INTENT_AREA_ID     = "net.tailriver.agoraguide.area_id";
	public enum SearchType {
		Keyword, Schedule, Area, Favorite;
	}

	protected SearchResultAdapter searchAdapter;
	protected ListView resultView;
	private int firstVisiblePosition;
	private SearchType type;

	// for Keyword
	private EditText searchText;
	private Category[] category;
	private boolean[] categoryChecked;

	// for Schedule
	private Spinner daySpinner;
	private Spinner timeSpinner;

	@Override
	public void onPreInitialize() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.search);

		searchAdapter = new SearchResultAdapter(this);
		resultView = (ListView) findViewById(R.id.searchResult);
		resultView.setAdapter(searchAdapter);
		resultView.setOnItemClickListener(this);
		resultView.setEmptyView(findViewById(R.id.searchNotFound));

		type = (SearchType) getIntent().getSerializableExtra(INTENT_SEARCH_TYPE);
		if (type != SearchType.Keyword) {
			findViewById(R.id.searchKeyword).setVisibility(View.GONE);
		}
		if (type != SearchType.Schedule) {
			findViewById(R.id.searchSchedule).setVisibility(View.GONE);
		}
	}

	@Override
	public void onPostInitialize() {
		if (type == SearchType.Keyword) {
			setTitle(R.string.searchByKeyword);
			category = Category.values().toArray(new Category[Category.values().size()]);
			categoryChecked = new boolean[category.length];
			Arrays.fill(categoryChecked, true);

			searchText = (EditText) findViewById(R.id.searchKeyword);
			searchText.addTextChangedListener(this);
			searchText.setText(null);
		}

		if (type == SearchType.Schedule) {
			setTitle(R.string.searchBySchedule);
			List<EntrySummary> entries = new ArrayList<EntrySummary>();
			for (TimeFrame tf : TimeFrame.values()) {
				entries.add(tf.getSummary());
			}
			String[] times = getResources().getStringArray(R.array.sbs_times);

			searchAdapter.addAll(new EntryFilter().addTimeFrameEntry().getResultTimeOrder());
		
			SpinnerAdapter dayAdapter =
					new ArrayAdapter<Day>(this, android.R.layout.simple_spinner_item, Day.values());
			SpinnerAdapter timeAdapter =
					new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times);
		
			daySpinner  = (Spinner) findViewById(R.id.searchDay);
			timeSpinner = (Spinner) findViewById(R.id.searchTime);
		
			daySpinner.setAdapter(dayAdapter);
			timeSpinner.setAdapter(timeAdapter);
			daySpinner.setOnItemSelectedListener(this);
			timeSpinner.setOnItemSelectedListener(this);
		}

		if (type == SearchType.Area) {
			setTitle(R.string.searchByMap);
			Area area = Area.get(getIntent().getStringExtra(INTENT_AREA_ID));
			searchAdapter.addAll(new EntryFilter().addAllEntry().applyFilter(area).getResult());
		}

		if (type == SearchType.Favorite) {
			setTitle(R.string.favorites);
			searchAdapter.addAll(new EntryFilter().addFavoriteEntry().getResult());
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		firstVisiblePosition = position;
		EntrySummary summary = (EntrySummary) parent.getItemAtPosition(position);
		Intent intent = new Intent(getApplicationContext(), EntryDetailActivity.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		startActivityForResult(intent, R.id.searchResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (type == SearchType.Keyword && requestCode == R.id.searchResult) {
			resultView.setSelection(firstVisiblePosition);
			resultView.requestFocusFromTouch();	// it is need when DPAD operation and back			
		}

		if (type == SearchType.Favorite && searchAdapter.getCount() != Favorite.values().size()) {
			searchAdapter.clear();
			searchAdapter.addAll(new EntryFilter().addFavoriteEntry().getResult());
			resultView.invalidate();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	public void afterTextChanged(Editable s) {
		if (type == SearchType.Keyword) {
			searchByKeyword(s);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (type == SearchType.Favorite) {
			new MenuInflater(this).inflate(R.menu.favorites, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (type == SearchType.Keyword) {
			String[] categoryName = new String[category.length];
			for (int i = 0; i < category.length; i++) {
				categoryName[i] = category[i].getShortName();
			}
			new AlertDialog.Builder(this)
			.setTitle(R.string.filtering)
			.setIcon(android.R.drawable.ic_search_category_default)
			.setMultiChoiceItems(categoryName, categoryChecked, this)
			.setPositiveButton(android.R.string.ok, this)
			.setNeutralButton(R.string.selectAll, this)
			.create()
			.show();
			return true;
		}

		if (type == SearchType.Favorite) {
			menu.findItem(R.id.menu_favorites_clear).setEnabled( !searchAdapter.isEmpty() );
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (type == SearchType.Favorite && item.getItemId() == R.id.menu_favorites_clear) {
			new AlertDialog.Builder(this)
			.setTitle(R.string.clearFavorite)
			.setIcon(android.R.drawable.ic_menu_delete)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, null)
			.create()
			.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(DialogInterface dialog, int which) {
		if (type == SearchType.Keyword) {
			if (which == AlertDialog.BUTTON_NEUTRAL) {
				for (int i = 0; i < categoryChecked.length; i++) {
					categoryChecked[i] = true;
				}
			}
			dialog.dismiss();
			searchByKeyword(searchText.getText());
		}

		if (type == SearchType.Favorite) {
			Favorite.clear();
			searchAdapter.clear();
			resultView.invalidate();
		}
	}

	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (type == SearchType.Keyword) {
			categoryChecked[which] = isChecked;
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (type == SearchType.Schedule) {
			Day day = (Day) daySpinner.getSelectedItem();
			int time = Integer.parseInt(((String) timeSpinner.getSelectedItem()).replace(":", ""));

			int viewPosition = TimeFrame.search(day, time);
			resultView.setSelection(viewPosition);
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void searchByKeyword(Editable s) {
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
