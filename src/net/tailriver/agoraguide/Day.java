package net.tailriver.agoraguide;

import java.util.List;

import android.content.res.Resources;

public class Day extends AbstractModel<Day> {
	private static Day singleton = new Day();
	private static ModelFactory<Day> factory;

	private String local;
	private int color;
	private int order;

	private Day() {}

	private Day(String common, String local, int color, int order) {
		super(common);
		this.local  = local;
		this.color  = color;
		this.order  = order;
	}

	public static synchronized void init() {
		if (factory == null) {
			factory = new ModelFactory<Day>();
			singleton.init_base();
		}
	}
	
	@Override
	protected void init_factory() {
		Resources res = AgoraDatabase.getContext().getResources();
		String[] common = res.getStringArray(R.array.days);
		String[] local  = res.getStringArray(R.array.days_locale);
		int[] color  = res.getIntArray(R.array.days_color);

		for (int i = 0; i < common.length; i++) {
			Day day = new Day(common[i], local[i], color[i], i);
			factory.put(common[i], day);
		}
	}

	public static Day get(String id) {
		return factory.get(id);
	}

	public static List<Day> values() {
		return factory.values();
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
}