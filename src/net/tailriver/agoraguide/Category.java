package net.tailriver.agoraguide;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Category extends AbstractModel<Category> {
	private static Category singleton = new Category();
	private static ModelFactory<Category> factory;

	private String name;
	private String abbrev;
	private boolean isAllday;

	private Category() {}

	private Category(String id, String name, String abbrev, boolean isAllday) {
		super(id);
		this.name     = name;
		this.abbrev   = abbrev;
		this.isAllday = isAllday;
	}

	public static void init() {
		factory = new ModelFactory<Category>();
		singleton.execute();
	}

	@Override
	protected void init_factory(SQLiteDatabase database) {
		String table = "category";
		String[] columns = { "id", "name", "abbrev", "is_allday" };
		Cursor c = database.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			String id     = c.getString(0);
			String name   = c.getString(1);
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
		return factory.values();
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
