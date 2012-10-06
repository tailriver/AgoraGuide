package net.tailriver.agoraguide;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class Area extends AbstractModel<Area> {
	private static Area singleton = new Area();
	private static ModelFactory<Area> factory;

	private String name;
	private String url;

	private Area() {}

	private Area(String id, String name) {
		super(id);
		this.name = name;
	}

	public static synchronized void init() {
		if (factory == null) {
			factory = new ModelFactory<Area>();
			singleton.init_base();
		}
	}

	@Override
	protected void init_factory() {
		SQLiteDatabase dbh = AgoraDatabase.get();
		String table1 = "area";
		String[] columns1 = { "id", "name" };
		Cursor c1 = dbh.query(table1, columns1, null, null, null, null, null);

		c1.moveToFirst();
		for (int i = 0, rows = c1.getCount(); i < rows; i++) {
			String id = c1.getString(0);
			String name = c1.getString(1);
			factory.put(id, new Area(id, name));
			c1.moveToNext();
		}
		c1.close();

		String device = selectDevice();

		String table2 = "area_image";
		String[] columns2 = { "area", "src" };
		String selection2 = "device=?";
		String[] selectionArgs2 = { device };
		Cursor c2 = dbh.query(table2, columns2, selection2, selectionArgs2, null, null, null);

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

	public String getName() {
		return name;
	}

	public BitmapDrawable getImage() throws StandAloneException {
		Context context = AgoraDatabase.getContext();
		File imageFile = context.getFileStreamPath("2012/area/" + super.toString() + ".png");
		if (System.currentTimeMillis() - imageFile.lastModified() > 86400 * 1000) {
			try {
				HttpClient http = new HttpClient(url);
				http.download(imageFile);
			} catch (StandAloneException e) {
				throw e;
			}catch (IOException e) {
				Log.w(getClass().getSimpleName(), "image download fail", e);
			}
		}

		if (imageFile.exists()) {
			return new BitmapDrawable(context.getResources(), imageFile.getPath());
		}
		return null;
	}
}
