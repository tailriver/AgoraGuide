package net.tailriver.agoraguide;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EntryDetail {
	public static final String CLASS_NAME = EntryDetail.class.getSimpleName();
	private static List<String> columns;

	private EntrySummary summary;
	private Map<String, String> data;
	private Location location;

	public EntryDetail(String id) {
		this(EntrySummary.get(id));
	}

	public EntryDetail(EntrySummary summary) {
		this.summary = summary;
		data     = new HashMap<String, String>();
		location = new Location(summary);

		SQLiteDatabase dbh = AgoraDatabase.get();
		String table = "entry";
		String[] columns = (String[]) EntryDetail.columns.toArray();
		String selection = "id=?";
		String[] selectionArgs = { summary.toString() };
		Cursor c = dbh.query(table, columns, selection, selectionArgs, null, null, null);

		c.moveToFirst();
		for (int i = 0; i < columns.length; i++) {
			data.put(columns[i], c.getString(i));
		}
		c.close();
	}

	public static synchronized void init() {
		if (columns == null) {
			Resources res = AgoraDatabase.getContext().getResources();
			columns = Arrays.asList(res.getStringArray(R.array.entrydetail));
		}
	}

	public EntrySummary getSummary() {
		return summary;
	}

	public String getDetailName(int index) {
		Resources res = AgoraDatabase.getContext().getResources();
		return res.getStringArray(R.array.entrydetail_locale)[index];
	}

	public String getDetailName(String key) {
		return getDetailName(Collections.binarySearch(columns, key));
	}

	public String getDetailValue(int index) {
		return getDetailValue(columns.get(index));
	}

	public String getDetailValue(String key) {
		return data.get(key);
	}

	public Location getLocation() {
		return location;
	}
}
