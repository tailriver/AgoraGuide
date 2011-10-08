package net.tailriver.agoraguide;

import java.util.ArrayList;

public class TimeFrame implements Comparable<TimeFrame> {
	public enum Days { Fri, Sat, Sun }

	private final String eid;	// entry id
	private final Days day;
	private final int start;
	private final int end;

	public TimeFrame(String eid, String day, int start, int end) {
		this.eid	= eid;
		this.day	= Days.valueOf(day);
		this.start	= start;
		this.end	= end;
	}

	public String getId() {
		return eid;
	}

	public int getStart() {
		return start;
	}

	public boolean equalsDay(String day) {
		return this.day.equals(Days.valueOf(day));
	}

	public static String[] getDaysString() {
		final ArrayList<String> days = new ArrayList<String>();
		for (Days d : Days.values())
			days.add(d.name());
		return days.toArray(new String[days.size()]);
	}

	@Override
	public String toString() {
		return String.format("%s: %s %02d:%02d-%02d:%02d", eid, day, start/100, start%100, end/100, end%100);
	}

	@Override
	public int compareTo(TimeFrame another) {
		if (this.day != another.day)
			return this.day.ordinal() - another.day.ordinal();
		if (this.start != another.start)
			return this.start - another.start;
		if (this.end != another.end)
			return this.end - another.end;
		return this.eid.compareTo(another.eid);
	}
}
