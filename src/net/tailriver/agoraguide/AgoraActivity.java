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
	private static final String CLASS_NAME = AgoraActivity.class.getSimpleName();
	private static final String databaseHost =
			BuildConfig.DEBUG ? "http://10.0.2.2:3000" : "http://tailriver.net";
	private static final String databaseURL  = databaseHost + "/agoraguide/2012.sqlite3.gz";
	private static final String databaseName = "2012.sqlite3";

	private static Context applicationContext;
	private static SQLiteDatabase database;
	private static Boolean initFinished = Boolean.FALSE;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Debug.startMethodTracing();
		if (!initFinished) {
			synchronized (initFinished) {
				onPreInitialize();
				if (!initFinished) {
					applicationContext = getApplicationContext();
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
				onPostInitialize(savedInstanceState);
			}
		} else {
			onPreInitialize();
			onPostInitialize(savedInstanceState);
		}
		//Debug.stopMethodTracing();
	}

	abstract public void onPreInitialize();

	abstract public void onPostInitialize(Bundle savedInstanceState);

	private final void initMore() {
		SQLiteDatabase.releaseMemory();
		try {
			Downloader d = new Downloader(this);
			int hour = 3600 * 100;
			for (Area area : Area.values()) {
				d.addTask(area.getImageURL(), area.getImageFile(), 24 * hour);
			}
			d.addTask(databaseURL, getDatabaseFile(), 6 * hour);
			d.execute();
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

	public static final SQLiteDatabase getDatabase() {
		if (database == null || !database.isOpen()) {
			return null;
		}
		return database;
	}

	public static final File getDatabaseFile() {
		File databaseFile = applicationContext.getFileStreamPath(databaseName);
		databaseFile.getParentFile().mkdirs();
		return databaseFile;
	}

	private static final void openDatabase() {
		if (!getDatabaseFile().exists()) {
			return;
		}
		try {
			database = SQLiteDatabase.openDatabase(
					getDatabaseFile().getPath(), null, SQLiteDatabase.OPEN_READONLY);
			database.rawQuery("SELECT count(*) FROM area", null);	// dummy query
		} catch(SQLiteDatabaseCorruptException e) {
			database.close();
			getDatabaseFile().delete();
			database = null;
		} catch (Exception e) {
			Log.w(CLASS_NAME, "openDatabase", e);
		}
	}

	public static void invalidateInit() {
		initFinished = Boolean.FALSE;
		Log.i(CLASS_NAME, "database updated; need to re-init");
	}

	public static final boolean isHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public final boolean isLandscape() {
		Configuration config = getResources().getConfiguration();
		return config.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
}
