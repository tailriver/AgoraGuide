package net.tailriver.agoraguide;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Area extends AbstractModel<Area> {
	private static Area singleton = new Area();
	private static ModelFactory<Area> factory;

	private String name;
	private String abbrev;
	private String url;

	private Area() {}

	private Area(String id, String name, String abbrev) {
		super(id);
		this.name   = name;
		this.abbrev = abbrev;
	}

	public static void init() {
		factory = new ModelFactory<Area>();
		singleton.execute();
	}

	@Override
	protected void init_factory(SQLiteDatabase database) {
		String table1 = "area";
		String[] columns1 = { "id", "name", "abbrev" };
		Cursor c1 = database.query(table1, columns1, null, null, null, null, null);

		c1.moveToFirst();
		for (int i = 0, rows = c1.getCount(); i < rows; i++) {
			String id     = c1.getString(0);
			String name   = c1.getString(1);
			String abbrev = c1.getString(2);
			factory.put(id, new Area(id, name, abbrev));
			c1.moveToNext();
		}
		c1.close();

		String device = selectDevice();

		String table2 = "area_image";
		String[] columns2 = { "area", "src" };
		String selection2 = "device=?";
		String[] selectionArgs2 = { device };
		Cursor c2 = database.query(table2, columns2, selection2, selectionArgs2, null, null, null);

		c2.moveToFirst();
		for (int i = 0, rows = c2.getCount(); i < rows; i++) {
			String id = c2.getString(0);
			String url = c2.getString(1);
			factory.get(id).url = url;
			c2.moveToNext();
		}
		c2.close();
	}

	private String selectDevice() {
		// TODO
		return "Android@ldpi";
	}

	public static Area get(String id) {
		return factory.get(id);
	}

	public static List<Area> values() {
		return factory.sortedValues();
	}

	public String getShortName() {
		return abbrev;
	}

	public Bitmap getBitmap() {
		File imageFile = getImageFile();
		if (imageFile.exists()) {
			return BitmapFactory.decodeFile(imageFile.getPath()).copy(Bitmap.Config.ARGB_8888, true);
		}
		return null;
	}

	public File getImageFile() {
		Context context = AgoraInitializer.getApplicationContext();
		File imageDir = context.getDir("2012_area", Context.MODE_PRIVATE);
		return new File(imageDir, getId() + ".png");		
	}

	public String getImageURL() {
		return url;
	}

	@Override
	public String toString() {
		return name;
	}
}
