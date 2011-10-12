package net.tailriver.agoraguide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.tailriver.agoraguide.TimeFrame.Days;

import android.content.res.Resources;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;


public class AgoraEntry {
	public enum Tag {
		TITLE_JA(String.class,		R.string.tag_title),
		TITLE_EN(String.class,		R.string.tag_title),
		SPONSOR(String.class,		R.string.tag_sponsor),
		CO_SPONSOR(String.class),
		ABSTRACT(String.class,		R.string.tag_abstract),
		LOCATION(String.class),
		CONTENT(String.class,		R.string.tag_content),
		GUEST(String.class,			R.string.tag_guest),
		NOTE(String.class,			R.string.tag_note),
		RESERVATION(String.class,	R.string.tag_reservation),
		IMAGE(URL.class),
		WEBSITE(URL.class),
		RESERVE_ADDRESS(URL.class),
		;

		private final Class<?> c;
		private final int resId;

		Tag(Class<?> theClass) {
			this(theClass, -1);
		}

		Tag(Class<?> theClass, int resId) {
			this.c = theClass;
			this.resId = resId;
		}

		public boolean equalsClass(Class<?> theClass) {
			return c.equals(theClass);
		}

		public int getResourceId() {
			assert resId == -1;
			return resId;
		}
	}

	public enum Category {
		Booth(R.string.category_A1),
		Poster(R.string.category_A2),
		DisplayAndCraft(R.string.category_A3),
		//Other(R.string.category_A4),
		SymposiumAndTalkSession(R.string.category_B1),
		WorkshopAndScienceCafe(R.string.category_B2),
		ScienceShowAndDisplay(R.string.category_B3),
		Other(R.string.category_B4),
		;

		private final int resId;

		Category(int resId) {
			this.resId = resId;
		}

		public int getResourceId() {
			return resId;
		}
	}

	public enum Target {
		Child,
		Student,
		Teacher,
		Professional,
		Adult,
		Politics,
		SCer,
		NonJapanese,
	}

	// static variables

	private static Resources mRes;
	private static final Map<Days, Integer> bgColor;

	static {
		bgColor = new EnumMap<Days, Integer>(Days.class);
		bgColor.put(Days.FRI, 0xFF666666);
		bgColor.put(Days.SAT, 0xFF0000CC);
		bgColor.put(Days.SUN, 0xFFCC0000);
	}

	// member variables

	private final Category category;
	private final Set<Target> target;
	private final Map<Tag, CharSequence> data;
	private CharSequence schedule;

	// constructor and methods

	public AgoraEntry(String category, String target, String schedule) {
		this.category	= Category.valueOf(category);
		this.target		= EnumSet.noneOf(Target.class);
		this.data		= new EnumMap<Tag, CharSequence>(Tag.class);
		this.schedule	= schedule;

		if (target != null) {
			for (String t : target.split(","))
				this.target.add(Target.valueOf(t));
		}
	}

	public static void setResources(Resources res) {
		mRes = res;
	}

	public Category getCategory() {
		return category;
	}

	/** @return {@code target} or {@code null} */
	public Set<Target> getTarget() {
		return target;
	}

	public CharSequence getSchedule() {
		if (schedule instanceof String)
			schedule = enhanceSchedule(schedule);
		return schedule;
	}

	/** @return value of {@code key} or {@code null} */
	public URL getURL(Tag tag) {
		assert !tag.equalsClass(URL.class);

		final CharSequence s = data.get(tag);
		if (s != null) {
			try {
				return new URL(s.toString());
			}
			catch (MalformedURLException e) {
				return null;
			}
		}
		return null;
	}

	/** @return value of {@code key} or {@code null} */
	public String getString(Tag tag) {
		assert !tag.equalsClass(String.class);
		final CharSequence s = data.get(tag);
		return s != null ? s.toString().replace("&#xA;", "\n") : null;
	}

	public String getLocaleTitle() {
		final String ja = getString(Tag.TITLE_JA);
		final String en = getString(Tag.TITLE_EN);
		return (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage()) || en == null) ? ja : en;
	}

	public void set(Tag tag, String value) {
		if (value != null && value.length() > 0)
			data.put(tag, value);
	}

	private CharSequence enhanceSchedule(CharSequence schedule) {
		final SpannableStringBuilder text = new SpannableStringBuilder(schedule);
		for (Map.Entry<Days, Integer> e : bgColor.entrySet()) {
			final Days day		= e.getKey();
			final int color		= e.getValue();
			final String seek	= String.format("[%s]", day.toString());

			int p = text.toString().indexOf(seek);
			while (p > -1) {
				final SpannableString ss = new SpannableString(mRes.getText(day.getResourceId()));
				ss.setSpan(android.graphics.Typeface.BOLD, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new ForegroundColorSpan(color), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.replace(p, p + seek.length(), ss);

				p = text.toString().indexOf(seek, p + ss.length());
			}
		}
		return text;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + getLocaleTitle();
	}
}
