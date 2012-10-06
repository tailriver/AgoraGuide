package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.util.Log;

public class Category implements Comparable<Category> {
	public static final String CLASS_NAME = Category.class.getSimpleName();
	private static Map<String, Category> cache;

	private String id;
	private String name;
	private boolean isAllday;

	private Category(String id, String name, boolean isAllday) {
		this.id       = id;
		this.name     = name;
		this.isAllday = isAllday;
	}

	public static synchronized final void init() {
		if (cache != null) {
			return;
		}

		cache = new HashMap<String, Category>();

		String table = "category";
		String[] columns = { "id", "name", "is_allday" };
		Cursor c = AgoraDatabase.get().query(table, columns, null, null, null, null, null);
		
		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			String id = c.getString(0);
			String name = c.getString(1);
			boolean isAllday = c.getInt(2) == 1;
			Category category = new Category(id, name, isAllday);
			Log.v(CLASS_NAME, category.toString());
			cache.put(id, category);
			c.moveToNext();
		}
		
		c.close();
	}

	public static Category parse(String id) {
		return cache.get(id);
	}

	public static List<Category> asList() {
		ArrayList<Category> list = new ArrayList<Category>(cache.values());
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}

	public String getId() {
		return id;
	}

	public boolean isAllday() {
		return isAllday;
	}

	public int compareTo(Category another) {
		return this.id.compareTo(another.id);
	}

	@Override
	public String toString() {
		return name;
	}
}
