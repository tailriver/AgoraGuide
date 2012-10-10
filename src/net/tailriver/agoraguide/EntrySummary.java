package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class EntrySummary extends AbstractModel<EntrySummary> {
	private static EntrySummary singleton = new EntrySummary();
	private static ModelFactory<EntrySummary> factory;

	private String title;
	private Category category;
	private String sponsor;
	private CharSequence schedule;
	private Area area;

	private EntrySummary() {}

	private EntrySummary(String id, String title, Category category, String sponsor, String schedule) {
		super(id);
		this.title    = title;
		this.category = category;
		this.sponsor  = sponsor;
		this.schedule = schedule;
	}

	public static void init() {
		factory = new ModelFactory<EntrySummary>();
		singleton.execute();
	}

	@Override
	protected void init_factory(SQLiteDatabase database) {
		String table1 = "entry";
		String[] columns1 = { "id", "title", "category", "sponsor", "schedule" };
		Cursor c1 = database.query(table1, columns1, null, null, null, null, null);

		c1.moveToFirst();
		for (int i = 0, rows = c1.getCount(); i < rows; i++) {
			String id = c1.getString(0);
			String title = c1.getString(1);
			Category category = Category.get(c1.getString(2));
			String sponsor = c1.getString(3);
			String schedule = c1.getString(4);
			EntrySummary es = new EntrySummary(id, title, category, sponsor, schedule);
			factory.put(id, es);
			c1.moveToNext();
		}
		c1.close();

		String table2 = "location";
		String[] columns2 = { "entry", "area" };
		Cursor c2 = database.query(table2, columns2, null, null, null, null, null);

		c2.moveToFirst();
		for (int i = 0, rows = c2.getCount(); i < rows; i++) {
			String id = c2.getString(0);
			Area area = Area.get(c2.getString(1));
			get(id).area = area;
			c2.moveToNext();
		}
		c2.close();
	}

	public static EntrySummary get(String id) {
		return factory.get(id);
	}

	public static Set<String> keySet() {
		return factory.keySet();
	}

	public static List<EntrySummary> values() {
		return factory.values();
	}

	@Deprecated
	public String getTitle() {
		return title;
	}

	public Category getCategory() {
		return category;
	}

	public String getSponsor() {
		return sponsor;
	}

	public CharSequence getSchedule() {
		if (schedule instanceof String)
			schedule = enhanceSchedule(schedule);
		return schedule;
	}

	public Area getArea() {
		return area;
	}

	private CharSequence enhanceSchedule(CharSequence schedule) {
		final SpannableStringBuilder text = new SpannableStringBuilder(schedule);
		for (Day day : Day.values()) {
			String seek = String.format("[%s]", day.getId());

			int p = text.toString().indexOf(seek);
			while (p > -1) {
				final SpannableString ss = new SpannableString(day.toString());
				ss.setSpan(android.graphics.Typeface.BOLD, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new ForegroundColorSpan(day.getColor()), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.replace(p, p + seek.length(), ss);

				p = text.toString().indexOf(seek, p + ss.length());
			}
		}
		return text;
	}

	@Deprecated
	public static List<EntrySummary> getEntryByKeyword(String query, Set<Category> filter) {
		List<EntrySummary> match = new ArrayList<EntrySummary>();

		// id match
		if (keySet().contains(query))
			match.add(EntrySummary.get(query));

		// keyword match
		for (EntrySummary es : values()) {
			if (!filter.contains(es.getCategory())) {
				continue;
			}

			if (query.length() == 0) {
				match.add(es);
				continue;
			}

			for (String word : new String[]{ es.title, es.sponsor }) {
				if (word != null && word.contains(query)) {
					match.add(es);
					break;
				}
			}
		}
		return match;
	}

	@Override
	public String toString() {
		return title;
	}
}
