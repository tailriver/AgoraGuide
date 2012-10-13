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
	private static InitStatus status = InitStatus.NULL;

	private enum InitStatus {
		NULL, UPDATE, SELECT, FINISH;
	}

	public static final void init(Context context) {
		Log.i("AgoraGuide", "init called; status:" + status);
		if (status != InitStatus.NULL) {
			return;
		}

		synchronized (status) {
			if (status != InitStatus.NULL) {
				return;
			}

			// update files and open database
			status = InitStatus.UPDATE;
			applicationContext = context.getApplicationContext();
			File databaseFile = applicationContext.getFileStreamPath(databaseName);
			databaseFile.getParentFile().mkdirs();
			updateDatabase(databaseFile);
			openDatabase(databaseFile);

			// SELECT * FROM ...
			status = InitStatus.SELECT;
			Area.init();
			Category.init();
			Day.init();
			EntrySummary.init();
			EntryDetail.init();
			Location.init();
			TimeFrame.init();
			SQLiteDatabase.releaseMemory();

			updateAreaImage();

			// finish
			status = InitStatus.FINISH;
		}
	}

	/** returns application context. */
	public static final Context getApplicationContext() {
		if (status == InitStatus.NULL) {
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
