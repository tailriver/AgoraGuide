package net.tailriver.agoraguide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.tailriver.agoraguide.TimeFrame.Days;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;


public class Entry {
	public enum Tag {
		TitleJa(String.class),
		TitleEn(String.class),
		Sponsor(String.class),
		CoSponsor(String.class),
		Abstract(String.class),
		Location(String.class),
		Schedule(String.class),
		Content(String.class),
		Guest(String.class),
		Note(String.class),
		Reservation(String.class),
		Image(URL.class),
		Website(URL.class),
		ProgramURL(URL.class),
		ColoredSchedule(CharSequence.class),
		;

		private final Class<?> c;

		Tag(Class<?> theClass) {
			this.c = theClass;
		}

		public boolean equalsClass(Class<?> theClass) {
			return c.equals(theClass);
		}
	}

	public enum Category {
		Booth,
		Poster,
		DisplayAndCraft,
		SymposiumAndTalkSession,
		WorkshopAndScienceCafe,
		ScienceShowAndDisplay,
		Other,
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
	
	private static final Map<Days, Integer> bgColor;

	static {
		bgColor = new EnumMap<Days, Integer>(Days.class);
		bgColor.put(Days.Fri, 0xFF666666);
		bgColor.put(Days.Sat, 0xFF0000CC);
		bgColor.put(Days.Sun, 0xFFCC0000);
	}

	// member variables

	private final String id;
	private final Category category;
	private final Set<Target> target;
	private final Map<Tag, CharSequence> data;

	// constructor and methods

	public Entry(String id, String category, String target) {
		this.id			= id;
		this.category	= Category.valueOf(category);
		this.target		= EnumSet.noneOf(Target.class);
		this.data		= new EnumMap<Tag, CharSequence>(Tag.class);

		if (target != null) {
			for (String t : target.split(","))
				this.target.add(Target.valueOf(t));
		}
	}

	public String getId() {
		return id;
	}

	public Category getCategory() {
		return category;
	}

	/** @return {@code target} or {@code null} */
	public Set<Target> getTarget() {
		return target;
	}

	public CharSequence getColoredSchedule() {
		if (data.get(Tag.ColoredSchedule) != null)
			return data.get(Tag.ColoredSchedule);

		final SpannableStringBuilder schedule = new SpannableStringBuilder(data.get(Tag.Schedule));
		for (Map.Entry<Days, Integer> e : bgColor.entrySet()) {
			final String day = e.getKey().toString();
			final int color = e.getValue();

			final String seek = String.format("[%s]", day);
			for (int p = schedule.toString().indexOf(seek); p > -1; p = schedule.toString().indexOf(seek, p + seek.length())) {
				final SpannableString ss = new SpannableString(day);
				ss.setSpan(android.graphics.Typeface.BOLD, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new ForegroundColorSpan(color), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				schedule.replace(p, p + seek.length(), ss);
			}
		}
		data.put(Tag.ColoredSchedule, schedule);
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
		final String ja = getString(Tag.TitleJa);
		final String en = getString(Tag.TitleEn);
		return (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage()) || en == null) ? ja : en;
	}

	public void set(Tag tag, String value) {
		if (value != null && value.length() > 0)
			data.put(tag, value);
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + getId();
	}
}
