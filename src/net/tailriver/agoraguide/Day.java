package net.tailriver.agoraguide;

import java.util.List;

import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

public class Day extends AbstractModel<Day> {
	private static ModelFactory<Day> factory = new ModelFactory<Day>();

	private String name;
	private int color;
	private int order;

	/*package*/ Day() {}

	private Day(String common, String name, int color, int order) {
		super(common);
		this.name  = name;
		this.color = color;
		this.order = order;
	}

	@Override
	protected void init(SQLiteDatabase database) {
		factory.clear();
		Resources res = AgoraActivity.getStaticApplicationContext().getResources();
		String[] common = res.getStringArray(R.array.days);
		String[] local  = res.getStringArray(R.array.days_locale);
		int[]    color  = res.getIntArray(R.array.days_color);

		for (int i = 0; i < common.length; i++) {
			Day day = new Day(common[i], local[i], color[i], i);
			factory.put(common[i], day);
		}
	}

	public static Day get(String id) {
		return factory.get(id);
	}

	public static List<Day> values() {
		return factory.sortedValues();
	}

	public int getDay() {
		return getId().equals("Sat") ? 10 : 11;
	}

	public int getColor() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Day && this.order == ((Day) o).order;
	}

	@Override
	public String toString() {
		return name;
	}
}