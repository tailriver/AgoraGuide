package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class EntrySummary implements Comparable<EntrySummary> {
	private static Map<String, EntrySummary> cache;
	private static List<EntrySummary> cacheList;

	private String id;
	private String title;
	private Category category;
	private String sponsor;
	private CharSequence schedule;
	private Area area;

	private EntrySummary(String id, String title, Category category, String sponsor, String schedule) {
		this.id       = id;
		this.title    = title;
		this.category = category;
		this.sponsor  = sponsor;
		this.schedule = schedule;
	}

	public synchronized static final void init() {
		if (cache != null) {
			return;
		}

		Area.init();
		Category.init();
		EntryDetail.init();

		long startTime = System.currentTimeMillis();

		cache = new HashMap<String, EntrySummary>();

		SQLiteDatabase dbh = AgoraDatabase.get();
		String table1 = "entry";
		String[] columns1 = { "id", "title", "category", "sponsor", "schedule" };
		Cursor c1 = dbh.query(table1, columns1, null, null, null, null, null);
		
		c1.moveToFirst();
		for (int i = 0, rows = c1.getCount(); i < rows; i++) {
			String id = c1.getString(0);
			String title = c1.getString(1);
			Category category = Category.parse(c1.getString(2));
			String sponsor = c1.getString(3);
			String schedule = c1.getString(4);
			EntrySummary es = new EntrySummary(id, title, category, sponsor, schedule);
			cache.put(id, es);
			c1.moveToNext();
		}
		c1.close();

		String table2 = "location";
		String[] columns2 = { "entry", "area" };
		Cursor c2 = dbh.query(table2, columns2, null, null, null, null, null);
		
		c2.moveToFirst();
		for (int i = 0, rows = c2.getCount(); i < rows; i++) {
			String id = c2.getString(0);
			Area area = Area.parse(c2.getString(1));
			cache.get(id).area = area;
			c2.moveToNext();
		}
		c2.close();

		cacheList = new ArrayList<EntrySummary>(cache.values());
		Collections.sort(cacheList);
		cacheList = Collections.unmodifiableList(cacheList);

		long endTime = System.currentTimeMillis();
		Log.d("timer", "EntrySummary#init() took " + (endTime - startTime) + "ms");
	}

	public static EntrySummary parse(String id) {
		return cache.get(id);
	}

	public static int find(String id) {
		return Collections.binarySearch(cacheList, parse(id));
	}

	public static List<EntrySummary> asList() {
		return cacheList;
	}

	public String getId() {
		return id;
	}

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
		for (Day day : Day.asList()) {
			String seek = String.format("[%s]", day);

			int p = text.toString().indexOf(seek);
			while (p > -1) {
				final SpannableString ss = new SpannableString(day.getLocalString());
				ss.setSpan(android.graphics.Typeface.BOLD, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new ForegroundColorSpan(day.getColor()), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.replace(p, p + seek.length(), ss);

				p = text.toString().indexOf(seek, p + ss.length());
			}
		}
		return text;
	}

	public static List<String> getEntryByKeyword(String query, Map<Category, Boolean> filter) {
		List<String> match = new ArrayList<String>();

		// id match
		if (cacheList.contains(query))
			match.add(query);

		// keyword match
		for (EntrySummary es : cacheList) {
			if (query.length() == 0) {
				match.add(es.id);
				continue;
			}

			for (String word : new String[]{ es.getTitle(), es.getSponsor() }) {
				if (word != null && word.contains(query)) {
					match.add(es.id);
					break;
				}
			}
		}
		return match;
	}

	public static void clear() {
		cache = null;
		cacheList = null;
	}

	public int compareTo(EntrySummary another) {
		return this.id.compareTo(another.id);
	}

	@Override
	public String toString() {
		return id + ":" + title + "(" + category + ")";
	}
}
