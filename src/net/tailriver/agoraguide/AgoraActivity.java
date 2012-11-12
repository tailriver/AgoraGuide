package net.tailriver.agoraguide;

import java.io.File;

import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public abstract class AgoraActivity extends FragmentActivity {
	private static final String CLASS_NAME = AgoraActivity.class
			.getSimpleName();
	private static final String databaseHost = "http://tailriver.net";
	private static final String databaseURL = databaseHost
			+ "/agoraguide/2012.sqlite3.gz";
	private static final String databaseName = "2012.sqlite3";

	private static Context applicationContext;
	private static SQLiteDatabase database;
	private static Boolean initFinished = Boolean.FALSE;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// When cache files have been deleted during launch app
		if (getCacheDir().listFiles().length == 0) {
			invalidateInit();
		}

		onPreInitialize();
		if (!initFinished) {
			synchronized (initFinished) {
				if (!initFinished) {
					applicationContext = getApplicationContext();

					// TODO compatibility (since v1.11)
					File oldFile = getFileStreamPath(databaseName);
					if (oldFile.exists()) {
						oldFile.renameTo(getDatabaseFile());
					}

					openDatabase();
					new Area();
					new Category();
					new Day();
					new EntrySummary();
					new Hint();
					new TimeFrame();
					initFinished = Boolean.TRUE;
					initMore();
				}
			}
		}
		onPostInitialize(savedInstanceState);
	}

	abstract public void onPreInitialize();

	abstract public void onPostInitialize(Bundle savedInstanceState);

	private final void initMore() {
		SQLiteDatabase.releaseMemory();
		try {
			Downloader d1 = new Downloader(this);
			Downloader d2 = new Downloader(this);
			d1.setShowProgressDialog(true);
			d1.addTask(databaseURL, getDatabaseFile());
			for (Area area : Area.values()) {
				d2.addTask(area.getImageURL(), area.getImageFile());
			}
			d1.execute();
			d2.execute();
		} catch (StandAloneException e) {
			Log.i(CLASS_NAME, e.getMessage());
		}
	}

	/** returns application context. */
	public static final Context getStaticApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("init not called");
		}
		return applicationContext;
	}

	public static final File getStaticCacheDir() {
		return applicationContext.getCacheDir();
	}

	public static final SQLiteDatabase getDatabase() {
		if (database == null || !database.isOpen()) {
			return null;
		}
		return database;
	}

	public static final File getDatabaseFile() {
		return new File(getStaticCacheDir(), databaseName);
	}

	private static final void openDatabase() {
		File databaseFile = getDatabaseFile();
		if (!databaseFile.exists()) {
			return;
		}
		try {
			invalidateDatabase();
			database = SQLiteDatabase.openDatabase(databaseFile.getPath(),
					null, SQLiteDatabase.OPEN_READONLY);
			database.rawQuery("SELECT count(*) FROM area", null); // dummy query
		} catch (SQLiteDatabaseCorruptException e) {
			invalidateDatabase();
			databaseFile.delete();
		} catch (Exception e) {
			Log.w(CLASS_NAME, "openDatabase", e);
		}
	}

	public static void invalidateInit() {
		initFinished = Boolean.FALSE;
		Log.i(CLASS_NAME, "invalidateInit called");
	}

	private static void invalidateDatabase() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	public static final boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static final boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public final boolean isLandscape() {
		Configuration config = getResources().getConfiguration();
		return config.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
}
