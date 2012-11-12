package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TimeFrame extends AbstractModel<TimeFrame> {
	private static ModelFactory<TimeFrame> factory = new ModelFactory<TimeFrame>();

	private EntrySummary summary;
	private Date start;
	private Date end;

	/* package */TimeFrame() {
	}

	private TimeFrame(Date pivot) {
		super(null);
		summary = null;
		start = pivot;
		end = pivot;
	}

	private TimeFrame(EntrySummary summary, Date start, Date end) {
		super(start.getTime() + " " + summary.getId());
		this.summary = summary;
		this.start = start;
		this.end = end;
	}

	@Override
	protected void init(SQLiteDatabase database) {
		factory.clear();
		Calendar cal = Calendar.getInstance(Locale.JAPAN);
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, Calendar.NOVEMBER);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		String table = "timeframe";
		String[] columns = { "entry", "day", "start", "end" };
		Cursor c = database.query(table, columns, null, null, null, null, null);

		c.moveToFirst();
		for (int i = 0, rows = c.getCount(); i < rows; i++) {
			EntrySummary summary = EntrySummary.get(c.getString(0));
			if (!summary.getCategory().isAllday()) {
				Day d = Day.get(c.getString(1));
				int s = c.getInt(2);
				int e = c.getInt(3);
				cal.set(Calendar.DAY_OF_MONTH, d.getDay());
				cal.set(Calendar.HOUR_OF_DAY, s / 100);
				cal.set(Calendar.MINUTE, s % 100);
				Date start = cal.getTime();
				cal.set(Calendar.HOUR_OF_DAY, e / 100);
				cal.set(Calendar.MINUTE, e % 100);
				Date end = cal.getTime();
				TimeFrame tf = new TimeFrame(summary, start, end);
				factory.put(tf.toString(), tf);
			}
			c.moveToNext();
		}
		c.close();
	}

	public static TimeFrame get(EntrySummary summary, Date start) {
		return factory.get(start.getTime() + " " + summary.getId());
	}

	public static List<TimeFrame> getAll(EntrySummary summary) {
		List<TimeFrame> list = new ArrayList<TimeFrame>();
		for (TimeFrame t : values()) {
			if (t.getId().endsWith(" " + summary.getId())) {
				list.add(t);
			}
		}
		return list;
	}

	public static List<TimeFrame> values() {
		return factory.sortedValues();
	}

	public static final TimeFrame makePivot(Day day, int time) {
		Calendar c = Calendar.getInstance(Locale.JAPAN);
		c.set(2012, Calendar.NOVEMBER, day.getDay(), time / 100, time % 100, 0);
		c.set(Calendar.MILLISECOND, 0);

		return new TimeFrame(c.getTime());
	}

	public EntrySummary getSummary() {
		return summary;
	}

	@SuppressWarnings("unused")
	public Calendar getStart() {
		Calendar c = Calendar.getInstance(Locale.JAPAN);
		c.setTime(start);

		// TODO hack for debug (start everyday)
		if (false) {
			Log.w("TimeFrame", "debug hack worked");
			Calendar cur = Calendar.getInstance();
			c.set(cur.get(Calendar.YEAR), cur.get(Calendar.MONTH),
					cur.get(Calendar.DAY_OF_MONTH));
			if (c.before(cur)) {
				c.add(Calendar.DAY_OF_MONTH, 1);
			}
			Log.w("TimeFrame", "getStart() returns " + c.getTime().toString());
		}
		return c;
	}

	@Override
	public int compareTo(TimeFrame another) {
		if (!start.equals(another.start))
			return start.compareTo(another.start);
		if (!end.equals(another.end))
			return end.compareTo(another.end);
		return super.compareTo(another);
	}

	@Override
	public String toString() {
		return getId();
	}
}
