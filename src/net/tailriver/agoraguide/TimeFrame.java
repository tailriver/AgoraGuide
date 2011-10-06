package net.tailriver.agoraguide;

public class TimeFrame {
	private final String eid;	// entry id
	private final String day;
	private final int start;
	private final int end;

	public TimeFrame(String eid, String day, int start, int end) {
		this.eid	= eid;
		this.day	= day;
		this.start	= start;
		this.end	= end;
	}

	public String getId() {
		return eid;
	}

	public String getDay() {
		return day;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
