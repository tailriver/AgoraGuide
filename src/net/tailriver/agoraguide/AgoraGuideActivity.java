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

public class AgoraGuideActivity extends Activity {
	private static final String databaseURL  = "http://tailriver.net/agoraguide/2012.sqlite3.gz";
	private static final String databaseName = "2012.sqlite3";
	private static Context context;
	private static SQLiteDatabase database;
	private static boolean initFinished;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.main_progressLayout).setVisibility(View.INVISIBLE);

		initDatabase(getApplicationContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Class<?> nextActivity = null;
		switch (item.getItemId()) {
		case R.id.menu_sbk:
			nextActivity = SearchByKeywordActivity.class;
			break;

		case R.id.menu_sbs:
			nextActivity = SearchByScheduleActivity.class;
			break;

		case R.id.menu_favorites:
			nextActivity = FavoritesActivity.class;
			break;

		case R.id.menu_credits:
			nextActivity = CreditsActivity.class;
			break;

		default:
			break;
		}

		if (nextActivity != null) {
			startActivity(new Intent(AgoraGuideActivity.this, nextActivity));
			return true;
		}
		else
			return false;
	}

	public static final void initDatabase(Context context) {
		AgoraGuideActivity.context = context;
		if (!initFinished) {
			initDatabase();
		}
	}

	private static synchronized void initDatabase() {
		if (!initFinished) {
			final File databaseFile = context.getFileStreamPath(databaseName);
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
				new DatabaseLoader(context).execute(pair);
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

	public static final Context getContext() {
		return context;
	}

}
