package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TimeFrame implements Comparable<TimeFrame> {
	private static List<TimeFrame> cache;

	private EntrySummary summary;
	private Day day;
	private int start;
	private int end;

	private TimeFrame(EntrySummary summary, Day day, int start, int end) {
		this.summary = summary;
		this.day     = day;
		this.start   = start;
		this.end     = end;
	}

	public static synchronized final void init() {
		if (cache != null) {
			return;
		}

		EntrySummary.init();
		Day.init();

		long startTime = System.currentTimeMillis();
		cache = new ArrayList<TimeFrame>();

		SQLiteDatabase dbh = AgoraDatabase.get();
		String table = "timeframe";
		String[] columns = { "entry", "day", "start", "end" };
		Cursor c = dbh.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			EntrySummary summary = EntrySummary.parse(c.getString(0));
			Day day   = Day.parse(c.getString(1));
			int start = c.getInt(2);
			int end   = c.getInt(3);
			if (!summary.getCategory().isAllday()) {
				TimeFrame tf = new TimeFrame(summary, day, start, end);
				cache.add(tf);
			}
			c.moveToNext();
		}
		c.close();
		Collections.sort(cache);
		cache = Collections.unmodifiableList(cache);

		long endTime = System.currentTimeMillis();
		Log.d("timer", "TimeFrame#init() took " + (endTime - startTime) + "ms");
	}

	public static List<TimeFrame> asList() {
		return cache;
	}

	public static int search(Day day, int time) {
		TimeFrame pivot = new TimeFrame(null, day, time, time);
		return - Collections.binarySearch(cache, pivot) - 1;
	}

	public EntrySummary getSummary() {
		return summary;
	}

	public static void clear() {
		cache = null;
	}

	public int compareTo(TimeFrame another) {
		if (!this.day.equals(another.day))
			return this.day.compareTo(another.day);
		if (this.start != another.start)
			return this.start - another.start;
		if (this.end != another.end)
			return this.end - another.end;
		return summary.compareTo(another.summary);
	}

	@Override
	public String toString() {
		return String.format("%s: %s %04d-%04d", summary.getId(), day, start, end);
	}
}
