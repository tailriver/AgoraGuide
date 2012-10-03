package net.tailriver.agoraguide;

import java.util.Arrays;
import java.util.List;

import android.content.res.Resources;

class Day implements Comparable<Day> {
	private static Resources res;
	private static List<String> days;

	public static void setResources(Resources res) {
		Day.res = res;
		Day.days = Arrays.asList(Day.res.getStringArray(R.array.days));
	}

	public static List<String> getDays() {
		return days;
	}

	public static List<String> getDaysLocale() {
		assert res == null;
		return Arrays.asList(res.getStringArray(R.array.days_locale));
	}

	private final int day;

	Day (String day) {
		this.day = days.indexOf(day);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Day && this.day == ((Day) o).day || super.equals(o);
	}

	public int compareTo(Day another) {
		return this.day - another.day;
	}

	@Override
	public String toString() {
		return res != null ? days.get(day) : String.valueOf(day);
	}
}