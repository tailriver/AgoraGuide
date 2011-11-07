package net.tailriver.agoraguide;

public class TimeFrame implements Comparable<TimeFrame> {
	private final String eid;	// entry id
	private final Day day;
	private final int start;
	private final int end;

	public TimeFrame(String eid, String day, int start, int end) {
		this.eid	= eid;
		this.day	= new Day(day);
		this.start	= start;
		this.end	= end;
	}

	public String getId() {
		return eid;
	}

	@Override
	public int compareTo(TimeFrame another) {
		if (this.day != another.day)
			return this.day.compareTo(another.day);
		if (this.start != another.start)
			return this.start - another.start;
		if (this.end != another.end)
			return this.end - another.end;
		return this.eid.compareTo(another.eid);
	}

	@Override
	public String toString() {
		return String.format("%s: %s %02d:%02d-%02d:%02d", eid, day, start/100, start%100, end/100, end%100);
	}
}
