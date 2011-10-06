package net.tailriver.agoraguide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
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
	
	private static final String[] days;
	private static final int[] fgColors, bgColors;

	static {
		days  = new String[]{ "Fri",		"Sat",		"Sun"		};
		fgColors = new int[]{ 0xFF666666,	0xFF00FFFF,	0xFFFF00FF	};
		bgColors = new int[]{ 0xFF000000,	0xFFFFFFFF,	0xFFFFFFFF	};
	}

	// member variables

	private final String id;
	private final Category category;
	private final Set<Target> target;
	private final Map<Tag, String> data;

	// constructor and methods

	public Entry(String id, String category, String target) {
		this.id			= id;
		this.category	= Category.valueOf(category);
		this.target		= EnumSet.noneOf(Target.class);
		this.data		= new EnumMap<Tag, String>(Tag.class);

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

	// TODO
	public CharSequence getColoredSchedule() {
		final SpannableStringBuilder schedule = new SpannableStringBuilder(data.get(Tag.Schedule));
		for (int i = 0; i < days.length; i++) {
			final String seek = String.format("[%s]", days[i]);
			for (int p = schedule.toString().indexOf(seek); p > -1; p = schedule.toString().indexOf(seek, p + seek.length())) {
				final SpannableString ss = new SpannableString(days[i]);
				ss.setSpan(new ForegroundColorSpan(fgColors[i]), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new BackgroundColorSpan(bgColors[i]), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				schedule.replace(p, p + seek.length(), ss);
			}
		}
		return schedule;
	}

	/** @return value of {@code key} or {@code null} */
	public URL getURL(Tag tag) {
		assert !tag.equalsClass(URL.class);

		final String s = data.get(tag);
		if (s != null) {
			try {
				return new URL(s);
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
		return data.get(tag);
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
