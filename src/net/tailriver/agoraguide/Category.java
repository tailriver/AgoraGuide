package net.tailriver.agoraguide;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Category extends AbstractModel<Category> {
	private static ModelFactory<Category> factory = new ModelFactory<Category>();

	private String name;
	private String abbrev;
	private boolean isAllday;

	/* package */Category() {
	}

	private Category(String id, String name, String abbrev, boolean isAllday) {
		super(id);
		this.name = name;
		this.abbrev = abbrev;
		this.isAllday = isAllday;
	}

	@Override
	protected void init(SQLiteDatabase database) {
		factory.clear();
		String table = "category";
		String[] columns = { "id", "name", "abbrev", "is_allday" };
		Cursor c = database.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			String id = c.getString(0);
			String name = c.getString(1);
			String abbrev = c.getString(2);
			boolean isAllday = c.getInt(3) == 1;
			Category category = new Category(id, name, abbrev, isAllday);
			factory.put(id, category);
			c.moveToNext();
		}
		c.close();
	}

	public static Category get(String id) {
		return factory.get(id);
	}

	public static List<Category> values() {
		return factory.sortedValues();
	}

	public String getShortName() {
		return abbrev;
	}

	public boolean isAllday() {
		return isAllday;
	}

	@Override
	public String toString() {
		return name;
	}
}
