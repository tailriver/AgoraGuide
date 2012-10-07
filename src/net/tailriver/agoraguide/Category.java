package net.tailriver.agoraguide;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Category extends AbstractModel<Category> {
	private static Category singleton = new Category();
	private static ModelFactory<Category> factory;

	private String name;
	private boolean isAllday;

	private Category() {}

	private Category(String id, String name, boolean isAllday) {
		super(id);
		this.name     = name;
		this.isAllday = isAllday;
	}

	public static void init() {
		factory = new ModelFactory<Category>();
		singleton.execute();
	}

	@Override
	protected void init_factory(SQLiteDatabase database) {
		String table = "category";
		String[] columns = { "id", "name", "is_allday" };
		Cursor c = database.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			String id = c.getString(0);
			String name = c.getString(1);
			boolean isAllday = c.getInt(2) == 1;
			Category category = new Category(id, name, isAllday);
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

	public String getName() {
		return name;
	}

	public boolean isAllday() {
		return isAllday;
	}
}
