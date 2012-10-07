package net.tailriver.agoraguide;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Location {
	private Area area;
	private float x;
	private float y;

	public Location(EntrySummary es) {
		SQLiteDatabase database = AgoraGuideActivity.getDatabase();

		String table = "location";
		String[] columns = { "area", "x", "y" };
		String selection = "entry=?";
		String[] selectionArgs = { es.toString() };
		Cursor c = database.query(table, columns, selection, selectionArgs, null, null, null);

		c.moveToFirst();
		area = Area.get(c.getString(0));
		x = c.getFloat(1);
		y = c.getFloat(2);
	}

	public Area getArea() {
		return area;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@Override
	public String toString() {
		return "area=" + area + ",x=" + x + ",y=" + y;
	}
}
