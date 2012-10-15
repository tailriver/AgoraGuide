package net.tailriver.agoraguide;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SearchActivity extends AgoraActivity implements OnItemClickListener {
	public enum SearchType {
		Keyword, Schedule, Area, Favorite;
	}

	private SearchResultAdapter searchAdapter;
	private ListView resultView;
	private SearchType type;
	private int lastPosition;

	// search helper
	private SearchHelper helper;

	@Override
	public void onPreInitialize() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.search);

		searchAdapter = new SearchResultAdapter(this);
		resultView = (ListView) findViewById(R.id.searchResult);
		resultView.setAdapter(searchAdapter);
		resultView.setOnItemClickListener(this);
		resultView.setEmptyView(findViewById(R.id.searchNotFound));

		type = (SearchType) getIntent().getSerializableExtra(IntentExtra.SEARCH_TYPE);
		if (type != SearchType.Keyword) {
			findViewById(R.id.searchKeyword).setVisibility(View.GONE);
		}
		if (type != SearchType.Schedule) {
			findViewById(R.id.searchSchedule).setVisibility(View.GONE);
		}
	}

	@Override
	public void onPostInitialize(Bundle savedInstanceState) {
		switch (type) {
		case Keyword:
			setTitle(R.string.searchKeyword);
			helper = new KeywordSearchHelper();
			break;

		case Schedule:
			setTitle(R.string.searchSchedule);
			helper = new ScheduleSearchHelper();
			break;

		case Area:
			setTitle(R.string.searchArea);
			helper = new AreaSearchHelper();
			break;

		case Favorite:
			setTitle(R.string.favorite);
			helper = new FavoriteSearchHelper();
			break;
		}
		if (savedInstanceState != null) {
			helper.onRestoreInstanceState(savedInstanceState);
		}
		helper.search();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		helper.onSaveInstanceState(outState);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		lastPosition = position;
		EntrySummary summary = (EntrySummary) parent.getItemAtPosition(position);
		Intent intent = new Intent(getApplicationContext(), ProgramActivity.class);
		intent.putExtra(IntentExtra.ENTRY_ID, summary.getId());
		startActivityForResult(intent, R.id.searchResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (type == SearchType.Keyword && requestCode == R.id.searchResult) {
			resultView.setSelection(lastPosition);
			resultView.requestFocusFromTouch();	// it is need when DPAD operation and back			
		}

		if (type == SearchType.Favorite && searchAdapter.getCount() != Favorite.values().size()) {
			helper.search();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		switch (type) {
		case Keyword:
			menu.findItem(R.id.searchCategoryFiltering).setVisible(true);
			return true;
		case Favorite:
			menu.findItem(R.id.favoriteClear).setVisible(true);
			menu.findItem(R.id.favoriteClear).setEnabled( !searchAdapter.isEmpty() );
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.searchCategoryFiltering:
		case R.id.favoriteClear:
			helper.createDialog().show();
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}
	}

	private interface SearchHelper {
		public void onRestoreInstanceState(Bundle savedInstance);
		public void onSaveInstanceState(Bundle outState);
		public AlertDialog createDialog();
		public void search();
	}

	private final class KeywordSearchHelper
	implements SearchHelper,
	OnKeyListener, OnClickListener, OnMultiChoiceClickListener, OnScrollListener, TextWatcher
	{
		private EditText searchText;
		private Category[] category;
		private boolean[] categoryChecked;

		public KeywordSearchHelper() {
			searchAdapter.setSource(EntrySummary.values(), ENTRY_COMPARATOR);
			category = Category.values().toArray(new Category[Category.values().size()]);
			categoryChecked = new boolean[category.length];
			Arrays.fill(categoryChecked, true);
			searchText = (EditText) findViewById(R.id.searchKeyword);
			searchText.addTextChangedListener(this);
			searchText.setOnKeyListener(this);	
			resultView.setOnScrollListener(this);
		}

		public void onRestoreInstanceState(Bundle savedInstanceState) {
			searchText.setText(savedInstanceState.getString("keyword"));
			categoryChecked = savedInstanceState.getBooleanArray("categoryChecked");
		}

		public void onSaveInstanceState(Bundle outState) {
			outState.putString("keyword", searchText.getText().toString());
			outState.putBooleanArray("categoryChecked", categoryChecked);
		}

		public AlertDialog createDialog() {
			Configuration config = getResources().getConfiguration();
			boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
			String[] categoryName = new String[category.length];
			for (int i = 0; i < category.length; i++) {
				String allday = Hint.get("$", category[i].isAllday() ? "allday" : "not_allday");
				String name = isLandscape ? category[i].toString() : category[i].getShortName();
				categoryName[i] = allday + " " + name;
			}
			return new AlertDialog.Builder(SearchActivity.this)
			.setTitle(R.string.searchCategoryFiltering)
			.setIcon(android.R.drawable.ic_search_category_default)
			.setMultiChoiceItems(categoryName, categoryChecked, this)
			.setPositiveButton(android.R.string.ok, this)
			.setNeutralButton(R.string.selectAll, this)
			.create();
		}

		public void search() {
			String s = searchText.getText().toString();
			Collection<Category> categoryFilter = new HashSet<Category>();
			for (int i = 0; i < category.length; i++) {
				if (categoryChecked[i]) {
					categoryFilter.add(category[i]);
				}
			}
			searchAdapter.filter(categoryFilter, s.toString());
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void afterTextChanged(Editable s) {
			search();
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			closeSoftKeyboard(searchText);
		}

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
				closeSoftKeyboard(v);
				return true;
			}
			return false;
		}

		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			categoryChecked[which] = isChecked;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (which == AlertDialog.BUTTON_NEUTRAL) {
				Arrays.fill(categoryChecked, true);
			}
			dialog.dismiss();
			search();
		}

		private void closeSoftKeyboard(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null && v != null) {
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}

	private final class ScheduleSearchHelper implements SearchHelper, OnItemSelectedListener {
		private Spinner daySpinner;
		private Spinner timeSpinner;

		public ScheduleSearchHelper() {
			Collection<EntrySummary> set = new HashSet<EntrySummary>();
			for (TimeFrame tf : TimeFrame.values()) {
				set.add(tf.getSummary());
			}
			searchAdapter.setSource(set, TIMEFRAME_COMPARATOR);
			searchAdapter.filter();

			ArrayAdapter<Day> dayAdapter = new ArrayAdapter<Day>(SearchActivity.this,
					android.R.layout.simple_spinner_item, Day.values());
			ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
					SearchActivity.this,
					R.array.searchScheduleTimes, android.R.layout.simple_spinner_item);

			dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			daySpinner  = (Spinner) findViewById(R.id.searchDay);
			timeSpinner = (Spinner) findViewById(R.id.searchTime);
			daySpinner.setAdapter(dayAdapter);
			timeSpinner.setAdapter(timeAdapter);
			daySpinner.setOnItemSelectedListener(this);
			timeSpinner.setOnItemSelectedListener(this);
		}

		public void onRestoreInstanceState(Bundle savedInstanceState) {
			daySpinner.setSelection(savedInstanceState.getInt("day"));
			timeSpinner.setSelection(savedInstanceState.getInt("time"));
		}

		public void onSaveInstanceState(Bundle outState) {
			outState.putInt("day",  daySpinner.getSelectedItemPosition());
			outState.putInt("time", timeSpinner.getSelectedItemPosition());
		}

		public AlertDialog createDialog() {
			return null;
		}

		public void search() {
			Day day = (Day) daySpinner.getSelectedItem();
			int time = Integer.parseInt(((String) timeSpinner.getSelectedItem()).replace(":", ""));
			int viewPosition = - Collections.binarySearch(
					TimeFrame.values(), TimeFrame.makePivot(day, time)) - 1;
			resultView.setSelection(viewPosition);
		}

		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			search();
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	private final class AreaSearchHelper implements SearchHelper {
		public AreaSearchHelper() {
			searchAdapter.setSource(EntrySummary.values(), ENTRY_COMPARATOR);
		}

		public void onRestoreInstanceState(Bundle savedInstance) {
		}

		public void onSaveInstanceState(Bundle outState) {
		}

		public AlertDialog createDialog() {
			return null;
		}

		public void search() {
			Area area = Area.get(getIntent().getStringExtra(IntentExtra.AREA_ID));
			searchAdapter.filter(Collections.singleton(area));
		}
	}

	private final class FavoriteSearchHelper implements SearchHelper, OnClickListener {
		public FavoriteSearchHelper() {
			searchAdapter.setSource(EntrySummary.values(), ENTRY_COMPARATOR);
		}

		public void onRestoreInstanceState(Bundle savedInstance) {
		}

		public void onSaveInstanceState(Bundle outState) {
		}

		public AlertDialog createDialog() {
			return new AlertDialog.Builder(SearchActivity.this)
			.setTitle(R.string.favoriteClear)
			.setIcon(android.R.drawable.ic_menu_delete)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, null)
			.create();
		}

		public void search() {
			searchAdapter.filter(Favorite.values());
		}

		public void onClick(DialogInterface dialog, int which) {
			Favorite.clear();
			search();
			ActivityCompat.invalidateOptionsMenu(SearchActivity.this);	// for < HONEYCOMB
		}
	}

	private static final Comparator<EntrySummary> ENTRY_COMPARATOR =
			new Comparator<EntrySummary>() {
		public int compare(EntrySummary lhs, EntrySummary rhs) {
			return lhs.compareTo(rhs);
		}
	};

	private static final Comparator<EntrySummary> TIMEFRAME_COMPARATOR =
			new Comparator<EntrySummary>() {
		public int compare(EntrySummary lhs, EntrySummary rhs) {
			return TimeFrame.get(lhs).compareTo(TimeFrame.get(rhs));
		}
	};
}
