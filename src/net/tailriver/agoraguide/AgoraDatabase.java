package net.tailriver.agoraguide;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AgoraDatabase {
	static final String thisClass = AgoraDatabase.class.getSimpleName();
	static final String databaseURL  = "http://tailriver.net/agoraguide/2012.sqlite3.gz";
	static final String databaseName = "agora2012_distributed";

	static Context context;
	static SQLiteDatabase dbh;

	public static void init(Context context) {
		if (AgoraDatabase.context != null) {
			return;
		}

		AgoraDatabase.context = context.getApplicationContext();
		HttpClient.init(context);
		Day.init();
	}

	public static void update() throws IOException {
		File databaseFile = context.getDatabasePath(databaseName);
		databaseFile.getParentFile().mkdirs();

		if (dbh != null) {
			dbh.close();
			dbh = null;
		}

		try {
			HttpClient ahc = new HttpClient(databaseURL);
			ahc.download(databaseFile);
		} catch (StandAloneException e) {
			// noop
		}
	}

	public static SQLiteDatabase get() {
		if (dbh == null || !dbh.isOpen()) {
			if (!context.getDatabasePath(databaseName).exists()) {
				Log.e(thisClass, "NO DATABASE");
			}
			dbh = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
		}
		return dbh;
	}

	public static Context getContext() {
		return context;
	}

}
