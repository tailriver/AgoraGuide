package net.tailriver.agoraguide;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EntryDetail {
	private static final String[] columns = {
		"cosponsor", "abstract", "content", "guest", "website", "reservation", "note" };

	private Map<String, String> data;
	private Location location;

	public EntryDetail(EntrySummary summary) {
		SQLiteDatabase database = AgoraActivity.getDatabase();
		if (database == null) {
			throw new IllegalStateException("database is not opened");
		}

		data     = new HashMap<String, String>();
		location = new Location(summary);

		String table = "entry";
		String selection = "id=?";
		String[] selectionArgs = { summary.getId() };
		Cursor c = database.query(table, columns, selection, selectionArgs, null, null, null);

		c.moveToFirst();
		for (int i = 0, max = c.getColumnCount(); i < max; i++) {
			String key = c.getColumnName(i);
			try {
				data.put(key, c.isNull(i) ? null : c.getString(i));
			} catch (RuntimeException e) {
				Log.w("EntryDetail", "getString", e);
				data.put(key, "");
			}
		}
		c.close();
	}

	public String getName(String key) {
		return Hint.get("entry", key);
	}

	public String getValue(String key) {
		if (!data.containsKey(key)) {
			throw new IllegalArgumentException();
		}
		return data.get(key);
	}

	public Location getLocation() {
		return location;
	}
}
