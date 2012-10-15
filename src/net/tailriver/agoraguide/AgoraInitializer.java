package net.tailriver.agoraguide;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class AgoraInitializer {
	private static final String databaseURL  = "http://tailriver.net/agoraguide/2012.sqlite3.gz";
	private static final String databaseName = "2012.sqlite3";

	private static Context applicationContext;
	private static SQLiteDatabase database;
	private static Boolean initFinished = Boolean.FALSE;

	public static final void init(Context context) {
		if (initFinished) {
			return;
		}

		synchronized (initFinished) {
			if (initFinished) {
				return;
			}
			Log.i("AgoraGuide", "initialization start");
			applicationContext = context.getApplicationContext();

			// update files and open database
			File databaseFile = applicationContext.getFileStreamPath(databaseName);
			databaseFile.getParentFile().mkdirs();
			updateDatabase(databaseFile);
			openDatabase(databaseFile);

			// SELECT * FROM ...
			new Area();
			new Category();
			new Day();
			new EntrySummary();
			new Hint();
			new TimeFrame();
			SQLiteDatabase.releaseMemory();

			updateAreaImage();

			// finish
			initFinished = Boolean.TRUE;
			Log.i("AgoraGuide", "initialization end");
		}
	}

	/** returns application context. */
	public static final Context getApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("init not called");
		}
		return applicationContext;
	}

	public static final SQLiteDatabase getDatabase() {
		if (database == null || !database.isOpen()) {
			throw new IllegalStateException("database has been closed");
		}
		return database;
	}

	private static final void updateDatabase(File file) {
		if (System.currentTimeMillis() - file.lastModified() > 3600 * 1000) {
			try {
				new Downloader().download(databaseURL, file);
			} catch (StandAloneException e) {
				// noop
			} catch (IOException e) {
				Log.e("AgoraGuide", e.getMessage(), e);
			}
		}
	}

	private static final void openDatabase(File file) {
		try {
			database = SQLiteDatabase.openDatabase(
					file.getPath(), null, SQLiteDatabase.OPEN_READONLY);			
		} catch (SQLiteException e) {
			Log.e("AgoraGuide", "openDatabase", e);
			throw new IllegalStateException("database not found");
		}
	}

	private static final void updateAreaImage() {
		for (Area area : Area.values()) {
			File imageFile = area.getImageFile();
			if (System.currentTimeMillis() - imageFile.lastModified() > 86400 * 1000) {
				try {
					new Downloader().download(area.getImageURL(), imageFile);
				} catch (StandAloneException e) {
					// noop
				} catch (IOException e) {
					Log.e("updateImage", e.getMessage(), e);
				}
			}
		}
	}
}
