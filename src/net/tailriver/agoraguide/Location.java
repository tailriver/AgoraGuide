package net.tailriver.agoraguide;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Location {
	private Area area;
	private double x;
	private double y;

	public Location(EntrySummary es) {
		SQLiteDatabase dbh = AgoraDatabase.get();

		String table = "location";
		String[] columns = { "area", "x", "y" };
		String selection = "entry=?";
		String[] selectionArgs = { es.toString() };
		Cursor c = dbh.query(table, columns, selection, selectionArgs, null, null, null);

		c.moveToFirst();
		area = Area.get(c.getString(0));
		x = c.getDouble(1);
		y = c.getDouble(2);
	}

	public Area getArea() {
		return area;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "area=" + area + ",x=" + x + ",y=" + y;
	}
}
