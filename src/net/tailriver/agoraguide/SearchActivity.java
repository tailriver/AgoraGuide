package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
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
	public static final String INTENT_SEARCH_TYPE = "net.tailriver.agoraguide.search_type";
	public static final String INTENT_AREA_ID     = "net.tailriver.agoraguide.area_id";
	public enum SearchType {
		Keyword, Schedule, Area, Favorite;
	}

	private SearchResultAdapter searchAdapter;
	private ListView resultView;
	private SearchType type;
	private int lastPosition;

	// search helper
	private KeywordSearchHelper ksh;
	private FavoriteSearchHelper fsh;

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
		switch (type) {
		case Keyword:
			setTitle(R.string.searchByKeyword);
			ksh = new KeywordSearchHelper();
			break;

		case Schedule:
			setTitle(R.string.searchBySchedule);
			new ScheduleSearchHelper();
			break;

		case Area:
			setTitle(R.string.searchByMap);
			new AreaSearchHelper();
			break;

		case Favorite:
			setTitle(R.string.favorites);
			fsh = new FavoriteSearchHelper();
			break;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		lastPosition = position;
		EntrySummary summary = (EntrySummary) parent.getItemAtPosition(position);
		Intent intent = new Intent(getApplicationContext(), EntryDetailActivity.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		startActivityForResult(intent, R.id.searchResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (type == SearchType.Keyword && requestCode == R.id.searchResult) {
			resultView.setSelection(lastPosition);
			resultView.requestFocusFromTouch();	// it is need when DPAD operation and back			
		}

		if (type == SearchType.Favorite && searchAdapter.getCount() != Favorite.values().size()) {
			fsh.search();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int menuRes;
		switch (type) {
		case Keyword:
			menuRes = R.menu.search;
			break;
		case Favorite:
			menuRes = R.menu.favorites;
			break;
		default:
			menuRes = 0;
			break;
		}

		if (menuRes > 0) {
			new MenuInflater(this).inflate(menuRes, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (type == SearchType.Favorite) {
			menu.findItem(R.id.menu_favorites_clear).setEnabled( !searchAdapter.isEmpty() );
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (type == SearchType.Keyword && item.getItemId() == R.id.searchFilterCategory) {
			ksh.createCategoryFilterDialog().show();
			return true;
		}
		if (type == SearchType.Favorite && item.getItemId() == R.id.menu_favorites_clear) {
			fsh.createDeleteAllDialog().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private final class KeywordSearchHelper
	implements OnKeyListener, OnClickListener, OnMultiChoiceClickListener, OnScrollListener, TextWatcher
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
			searchText.setText(null);
			resultView.setOnScrollListener(this);
		}

		public AlertDialog createCategoryFilterDialog() {
			String[] categoryName = new String[category.length];
			for (int i = 0; i < category.length; i++) {
				categoryName[i] = category[i].getShortName();
			}
			return new AlertDialog.Builder(SearchActivity.this)
			.setTitle(R.string.filtering)
			.setIcon(android.R.drawable.ic_search_category_default)
			.setMultiChoiceItems(categoryName, categoryChecked, this)
			.setPositiveButton(android.R.string.ok, this)
			.setNeutralButton(R.string.selectAll, this)
			.create();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void afterTextChanged(Editable s) {
			search(s);
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
			search(searchText.getText());
		}

		private void closeSoftKeyboard(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null && v != null) {
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}

		private void search(Editable s) {
			Collection<Category> categoryFilter = new HashSet<Category>();
			for (int i = 0; i < category.length; i++) {
				if (categoryChecked[i]) {
					categoryFilter.add(category[i]);
				}
			}
			searchAdapter.filter(categoryFilter, s.toString());
		}
	}

	private final class ScheduleSearchHelper implements OnItemSelectedListener {
		private Spinner daySpinner;
		private Spinner timeSpinner;

		public ScheduleSearchHelper() {
			Collection<EntrySummary> set = new HashSet<EntrySummary>();
			for (TimeFrame tf : TimeFrame.values()) {
				set.add(tf.getSummary());
			}
			searchAdapter.setSource(set, TIMEFRAME_COMPARATOR);
			searchAdapter.filter();

			String[] times = getResources().getStringArray(R.array.sbs_times);

			daySpinner  = (Spinner) findViewById(R.id.searchDay);
			timeSpinner = (Spinner) findViewById(R.id.searchTime);
			daySpinner.setAdapter(new ArrayAdapter<Day>(
					SearchActivity.this, android.R.layout.simple_spinner_item, Day.values()));
			timeSpinner.setAdapter(new ArrayAdapter<String>(
					SearchActivity.this, android.R.layout.simple_spinner_item, times));
			daySpinner.setOnItemSelectedListener(this);
			timeSpinner.setOnItemSelectedListener(this);
		}

		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Day day = (Day) daySpinner.getSelectedItem();
			int time = Integer.parseInt(((String) timeSpinner.getSelectedItem()).replace(":", ""));
			int viewPosition = search(day, time);
			resultView.setSelection(viewPosition);
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}

		private int search(Day day, int time) {
			List<TimeFrame> list = new ArrayList<TimeFrame>(TimeFrame.values());
			Collections.sort(list);
			return - Collections.binarySearch(list, TimeFrame.makePivot(day, time)) - 1;
		}
	}

	private final class AreaSearchHelper {
		public AreaSearchHelper() {
			searchAdapter.setSource(EntrySummary.values(), ENTRY_COMPARATOR);
			Area area = Area.get(getIntent().getStringExtra(INTENT_AREA_ID));
			search(Collections.singleton(area));
		}

		public void search(Collection<Area> area) {
			searchAdapter.filter(area);
		}
	}

	private final class FavoriteSearchHelper implements OnClickListener {
		public FavoriteSearchHelper() {
			searchAdapter.setSource(EntrySummary.values(), ENTRY_COMPARATOR);
			search();
		}

		public void onClick(DialogInterface dialog, int which) {
			Favorite.clear();
			search();
		}

		public Dialog createDeleteAllDialog() {
			return new AlertDialog.Builder(SearchActivity.this)
			.setTitle(R.string.clearFavorite)
			.setIcon(android.R.drawable.ic_menu_delete)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, null)
			.create();
		}

		public void search() {
			searchAdapter.filter(Favorite.values());
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
