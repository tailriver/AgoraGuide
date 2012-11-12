package net.tailriver.agoraguide;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Hint extends AbstractModel<Hint> {
	private static Map<String, String> factory = new HashMap<String, String>();

	/* package */Hint() {
	}

	@Override
	protected void init(SQLiteDatabase database) {
	}

	public static final String get(String table, String column) {
		factory.clear();
		String key = new StringBuilder(table).append('.').append(column)
				.toString();
		if (!factory.containsKey(key)) {
			String value = select(table, column);
			factory.put(key, value);
			return value;
		}
		return factory.get(key);
	}

	private static String select(String table, String column) {
		String selection = "table_name=? AND column_name=?";
		String[] selectionArgs = { table, column };
		return select(selection, selectionArgs);
	}

	private static String select(int ref) {
		String selection = "id=?";
		String[] selectionArgs = { String.valueOf(ref) };
		return select(selection, selectionArgs);
	}

	private static String select(String selection, String[] selectionArgs) {
		SQLiteDatabase database = AgoraActivity.getDatabase();
		String table = "hint";
		String[] columns = { "ja", "warning", "ref" };
		Cursor c = database.query(table, columns, selection, selectionArgs,
				null, null, null);
		try {
			if (!c.moveToFirst() || c.getCount() != 1) {
				throw new IllegalArgumentException("not found");
			}
			if (!c.isNull(1)) {
				throw new IllegalArgumentException(c.getString(1));
			}
			if (!c.isNull(2)) {
				return select(c.getInt(2));
			}
			if (c.isNull(0)) {
				throw new IllegalArgumentException("null");
			}
			return c.getString(0);
		} finally {
			c.close();
		}
	}
}
