package net.tailriver.agoraguide;

import java.util.Collections;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TimeFrame extends AbstractModel<TimeFrame> {
	private static TimeFrame singleton = new TimeFrame();
	private static ModelFactory<TimeFrame> factory;

	private EntrySummary summary;
	private Day day;
	private int start;
	private int end;

	private TimeFrame() {}

	private TimeFrame(EntrySummary summary, Day day, int start, int end) {
		super(summary.getId());
		this.summary = summary;
		this.day     = day;
		this.start   = start;
		this.end     = end;
	}

	public static void init() {
		factory = new ModelFactory<TimeFrame>();
		singleton.execute();
	}

	@Override
	protected void init_factory(SQLiteDatabase database) {
		String table = "timeframe";
		String[] columns = { "entry", "day", "start", "end" };
		Cursor c = database.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			EntrySummary summary = EntrySummary.get(c.getString(0));
			Day day   = Day.get(c.getString(1));
			int start = c.getInt(2);
			int end   = c.getInt(3);
			if (!summary.getCategory().isAllday()) {
				TimeFrame tf = new TimeFrame(summary, day, start, end);
				factory.put(tf.toString(), tf);
			}
			c.moveToNext();
		}
		c.close();
	}

	public static int search(Day day, int time) {
		TimeFrame pivot = new TimeFrame(null, day, time, time);
		return - Collections.binarySearch(values(), pivot) - 1;
	}

	public static TimeFrame get(EntrySummary summary) {
		return factory.get(summary.getId());
	}

	public static List<TimeFrame> values() {
		return factory.values();
	}

	public EntrySummary getSummary() {
		return summary;
	}

	@Override
	public int compareTo(TimeFrame another) {
		if (!day.equals(another.day))
			return day.compareTo(another.day);
		if (start != another.start)
			return start - another.start;
		if (end != another.end)
			return end - another.end;
		return summary.compareTo(another.summary);
	}

	@Override
	public String toString() {
		return getId();
	}
}
