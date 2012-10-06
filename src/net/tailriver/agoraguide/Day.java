package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.Resources;

class Day implements Comparable<Day> {
	public static final String CLASS_NAME = Day.class.getSimpleName();
	private static List<Day> days;

	private String common;
	private String local;
	private int color;
	private int order;

	private Day(String common, String local, int color, int order) {
		this.common = common;
		this.local  = local;
		this.color  = color;
		this.order  = order;
	}

	public static synchronized final void init() {
		if (days != null) {
			return;
		}
		days = new ArrayList<Day>();

		Resources res = AgoraDatabase.getContext().getResources();
		String[] common = res.getStringArray(R.array.days);
		String[] local  = res.getStringArray(R.array.days_locale);
		int[] color  = res.getIntArray(R.array.days_color);

		for (int i = 0; i < common.length; i++) {
			Day day = new Day(common[i], local[i], color[i], i);
			days.add(day);
		}
		days = Collections.unmodifiableList(days);
	}

	public static List<Day> asList() {
		return days;
	}

	public static Day parse(String day) {
		for (Day d : days) {
			if (d.common.equals(day)) {
				return d;
			}
		}
		return null;
	}

	public String getLocalString() {
		return local;
	}

	public int getColor() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Day && this.order == ((Day) o).order;
	}

	public int compareTo(Day another) {
		return this.order - another.order;
	}

	@Override
	public String toString() {
		return common;
	}
}