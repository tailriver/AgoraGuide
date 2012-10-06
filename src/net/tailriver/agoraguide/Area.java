package net.tailriver.agoraguide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class Area implements Comparable<Area> {
	public static final String CLASS_NAME = Area.class.getSimpleName(); 
	private static Map<String, Area> cache;
	private String id;
	private String name;
	private String url;

	private Area(String id, String name) {
		this.id   = id;
		this.name = name;
	}

	public synchronized static final void init() {
		if (cache != null) {
			return;
		}

		cache = new HashMap<String, Area>();
		SQLiteDatabase dbh = AgoraDatabase.get();

		String table1 = "area";
		String[] columns1 = { "id", "name" };
		Cursor c1 = dbh.query(table1, columns1, null, null, null, null, null);

		c1.moveToFirst();
		for (int i = 0, rows = c1.getCount(); i < rows; i++) {
			String id = c1.getString(0);
			String name = c1.getString(1);
			cache.put(id, new Area(id, name));
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
			cache.get(id).url = url;
			c2.moveToNext();
		}
		c2.close();
	}

	private static String selectDevice() {
		// TODO
		return "Android@ldpi";
	}

	public static Area parse(String id) {
		return cache.get(id);
	}

	public static List<Area> asList() {
		List<Area> list = new ArrayList<Area>(cache.values());
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BitmapDrawable getImage() throws StandAloneException {
		Context context = AgoraDatabase.getContext();
		File imageFile = context.getFileStreamPath("2012/area/" + id + ".png");
		if (System.currentTimeMillis() - imageFile.lastModified() > 86400 * 1000) {
			try {
				HttpClient http = new HttpClient(url);
				http.download(imageFile);
			} catch (StandAloneException e) {
				throw e;
			}catch (IOException e) {
				Log.w(CLASS_NAME, "image download fail", e);
			}
		}

		if (imageFile.exists()) {
			return new BitmapDrawable(context.getResources(), imageFile.getPath());
		}
		return null;
	}

	public int compareTo(Area another) {
		return this.id.compareTo(another.id);
	}
}
