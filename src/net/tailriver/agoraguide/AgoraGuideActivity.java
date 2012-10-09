package net.tailriver.agoraguide;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class AgoraGuideActivity extends Activity implements OnClickListener {
	private static final String databaseURL  = "http://tailriver.net/agoraguide/2012.sqlite3.gz";
	private static final String databaseName = "2012.sqlite3";
	private static Context applicationContext;
	private static SQLiteDatabase database;
	private static boolean initFinished;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initDatabase(getApplicationContext());

		ViewGroup vg = ((ViewGroup) findViewById(R.id.main_buttons));
		for (int i = 0, max = vg.getChildCount(); i < max; i++) {
			View v = vg.getChildAt(i);
			v.setOnClickListener(this);
		}
		findViewById(R.id.button_search_area).setVisibility(View.GONE);
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
			return false;
		}
	}

	private final void jumpNextActivity(int resID) {
		Class<?> nextActivity;
		switch (resID) {
		case R.id.button_search_keyword:
		case R.id.menu_sbk:
			nextActivity = KeywordSearchActivity.class;
			break;
		case R.id.button_search_schedule:
		case R.id.menu_sbs:
			nextActivity = ScheduleSearchActivity.class;
			break;
		case R.id.button_search_area:
		case R.id.menu_sbm:
			throw new UnsupportedOperationException("under construction...");
		case R.id.button_search_favorite:
		case R.id.menu_favorites:
			nextActivity = FavoritesActivity.class;
			break;
		case R.id.button_information:
		case R.id.menu_credits:
			nextActivity = CreditsActivity.class;
			break;
		default:
			throw new UnsupportedOperationException("invalid call");
		}
		startActivity(new Intent(applicationContext, nextActivity));
	}

	public static final void initDatabase(Context context) {
		applicationContext = context.getApplicationContext();
		if (!initFinished) {
			initDatabase();
		}
	}

	private static synchronized void initDatabase() {
		if (!initFinished) {
			final File databaseFile = applicationContext.getFileStreamPath(databaseName);
			databaseFile.getParentFile().mkdirs();

			class DatabaseLoader extends Downloader {
				public DatabaseLoader(Context context) throws StandAloneException {
					super(context);
				}

				@Override
				protected Void doInBackground(Downloader.Pair... params) {
					super.doInBackground(params);

					if (!databaseFile.exists()) {
						Log.e("AgoraGuide", "NO DATABASE");
					}
					database = SQLiteDatabase.openDatabase(databaseFile.getPath(), null, Context.MODE_PRIVATE);
					Area.init();
					Category.init();
					Day.init();
					EntrySummary.init();
					EntryDetail.init();
					TimeFrame.init();
					initFinished = true;
					return null;
				}
			}

			Downloader.Pair pair = null;
			if (System.currentTimeMillis() - databaseFile.lastModified() > 3600 * 1000) {
				pair = new Downloader.Pair(databaseURL, databaseFile);
			}

			try {
				new DatabaseLoader(applicationContext).execute(pair);
			} catch (StandAloneException e) {}
		}
	}

	public static final SQLiteDatabase getDatabase() {
		if (database == null || !database.isOpen()) {
			throw new IllegalArgumentException("database has been closed");
		}
		return database;
	}

	public static final boolean isInitFinished() {
		return initFinished;
	}

	/** returns application context. */
	public static final Context getContext() {
		return applicationContext;
	}
}
