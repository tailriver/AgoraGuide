package net.tailriver.agoraguide;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Xml;

class AgoraData {
	private final Context context;
	private final SharedPreferences pref;

	private static Map<String, Entry> entryMap;
	private static Map<String, TimeFrame> timeFrameMap;
	private static List<String> favoriteList;
	private static boolean isParseFinished = false;

	/** @param context	It should be {@code getApplicationContext()} */
	public AgoraData(Context context) {
		this.context = context.getApplicationContext();
		this.pref	 = this.context.getSharedPreferences("pref", Context.MODE_PRIVATE);

		if (entryMap == null) {
			entryMap	 = new LinkedHashMap<String, Entry>(pref.getInt("initialCapacityOfEntry",	  50));
			timeFrameMap = new HashMap<String, TimeFrame>(  pref.getInt("initialCapacityOfTimeFrame", 50));
			favoriteList = new ArrayList<String>(Arrays.asList(pref.getString("favorites", "").split(";")));
		}
	}

	public static boolean isParseFinished() {
		return isParseFinished;
	}

	public boolean isConnected() {
		return AgoraData.isConnected(context);
	}

	public static boolean isConnected(Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		// TODO I don't have any confidence for this is right
		for (NetworkInfo ni : cm.getAllNetworkInfo()) {
			if (ni.isAvailable() && ni.getState() == NetworkInfo.State.CONNECTED)
				return true;
		}
		return false;
	}

	/** @throws UpdateDataAbortException */
	public boolean updateData(boolean useGZIP, Handler handler) throws UpdateDataAbortException {
		if (!isConnected())
			return false;

		final String[] versionText;
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(new URL(context.getString(R.string.path_version_check)).openStream()), 64);
			versionText = br.readLine().split(";");
			br.close();
		}
		catch (IOException e) {
			// fail to download versionTextURL; we cannot continue
			throw new UpdateDataAbortException("Fail to check update information: " + e);
		}

		// format of version.txt
		//		version ; file size of data.xml ; file size of data.xml.gz
		final int localVersion	= pref.getInt("localVersion", 0);
		final int serverVersion	= Integer.parseInt(versionText[0]);
		final int size			= Integer.parseInt(versionText[useGZIP ? 2 : 1]);

		if (serverVersion == localVersion)
			return false;

		// show progress bar with start state
		if (handler != null)
			sendProgressMessage(handler, 0, size);

		try {
			final InputStream			is = new URL(context.getString(useGZIP ? R.string.path_data_xml_gz : R.string.path_data_xml)).openStream();
			final FileOutputStream	   fos = context.openFileOutput(context.getString(R.string.path_local_data_new), Context.MODE_PRIVATE);

			final int BUFFER_SIZE = 4096;
			final BufferedInputStream  bis = new BufferedInputStream(useGZIP ? new GZIPInputStream(is) : is, BUFFER_SIZE);
			final BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);

			byte[] buffer = new byte[BUFFER_SIZE];
			int totalRead = 0;
			while (true) {
				final int byteRead = bis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);

				// update progress bar
				if (handler != null) {
					totalRead += byteRead;
					sendProgressMessage(handler, totalRead, size);
				}
			}
			bis.close();
			bos.flush();
			bos.close();

			SharedPreferences.Editor ee = pref.edit();
			ee.putInt("localVersionNew", serverVersion);
			ee.commit();

			// hide progress bar
			sendProgressMessage(handler, 0, 0);

			return true;
		}
		catch (IOException e) {
			// fail to download XMLDataURL; we cannot continue
			throw new UpdateDataAbortException("Fail to update data file: " + e);
		}
	}

	/** @throws ParseDataAbortException */
	public void parseData() throws ParseDataAbortException {
		clear();

		boolean useNewData = pref.contains("localVersionNew");
		final SharedPreferences.Editor ee = pref.edit();

		try {
			final XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(context.openFileInput(context.getString(useNewData ? R.string.path_local_data_new : R.string.path_local_data)), null);

			Entry entry = null;
			for (int e = xpp.getEventType(); e != XmlPullParser.END_DOCUMENT; e = xpp.next()) {
				switch (e) {
				case XmlPullParser.START_TAG:
					final String startTag = xpp.getName();
					if ("entry".equals(startTag)) {
						final String id			= xpp.getAttributeValue(null, "id");
						final String category	= xpp.getAttributeValue(null, "category");
						final String target		= xpp.getAttributeValue(null, "target");

						if (id == null || category == null)
							throw new ParseDataAbortException("Parse error: it does not have required content");

						entry = new Entry(id, category, target);
						entry.set(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title_ja"));
						entry.set(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title_en"));
						entry.set(EntryKey.Sponsor,		xpp.getAttributeValue(null, "sponsor"));
						entry.set(EntryKey.CoSponsor,	xpp.getAttributeValue(null, "cosponsor"));
						entry.set(EntryKey.Image,		xpp.getAttributeValue(null, "image"));
						entry.set(EntryKey.Location,	xpp.getAttributeValue(null, "location"));
						entry.set(EntryKey.Schedule,	xpp.getAttributeValue(null, "schedule"));
						entry.set(EntryKey.Guest,		xpp.getAttributeValue(null, "guest"));
						entry.set(EntryKey.Website,		xpp.getAttributeValue(null, "website"));
						entryMap.put(id, entry);
						break;
					}
					if (entry == null)
						break;

					if ("timeframe".equals(startTag)) {
						final String tfid	= xpp.getAttributeValue(null, "id");
						final String day	= xpp.getAttributeValue(null, "day");
						final int start		= Integer.parseInt(xpp.getAttributeValue(null, "start"));
						final int end		= Integer.parseInt(xpp.getAttributeValue(null, "end"));
						timeFrameMap.put(tfid, new TimeFrame(entry.getId(), day, start, end));
						break;
					}

					if ("abstract".equals(startTag))
						entry.set(EntryKey.Abstract,	xpp.nextText());
					else if ("content".equals(startTag))
						entry.set(EntryKey.Content,		xpp.nextText());
					else if ("note".equals(startTag))
						entry.set(EntryKey.Note,		xpp.nextText());
					else if ("reservation".equals(startTag)) {
						entry.set(EntryKey.ProgramURL, xpp.getAttributeValue(null, "url"));
						entry.set(EntryKey.Reservation,	xpp.nextText());
					}
					else
						throw new ParseDataAbortException("Parse error: unknown tag found");
					break;

				case XmlPullParser.END_TAG:
					final String closeTag = xpp.getName();
					if ("entry".equals(closeTag))
						entry = null;
					break;
				}
			}
			isParseFinished = true;

			ee.putInt("initialCapacityOfEntryMap", (int) (entryMap.size() * 1.5));
			ee.putInt("initialCapacityOfTimeFrameMap", (int) (timeFrameMap.size() * 1.5));

			// the new data file is valid (correctly, well-formed) XML, it's time to replace
			if (useNewData) {
				context.getFileStreamPath(context.getString(R.string.path_local_data_new)).renameTo(context.getFileStreamPath(context.getString(R.string.path_local_data)));
				ee.putInt("localVersion", pref.getInt("localVersionNew", 0));
			}
		}
		catch (ParseDataAbortException e) {
			clear();
			throw e;
		}
		catch (Exception e) {
			clear();
			throw new ParseDataAbortException("Parse error: " + e);
		}
		finally {
			ee.remove("localVersionNew");
			ee.commit();

			// delete invalid new XML file
			// retry. parseData() run under using old data
			if (useNewData && !isParseFinished) {
				context.deleteFile(context.getString(R.string.path_local_data_new));
				parseData();
			}
		}
	}

	public void removeData() {
		context.deleteFile(context.getString(R.string.path_local_data));
		final SharedPreferences.Editor ee = pref.edit();
		ee.remove("lovalVersionNew");
		ee.remove("localVersion");
		ee.commit();
	}

	public static void clear() {
		isParseFinished = false;
		entryMap.clear();
		timeFrameMap.clear();
	}

	/**
	 * 
	 * @param handler Handler.
	 * @param progress int. argument of setProgress()
	 * @param max int. argument of setMax()
	 */
	private void sendProgressMessage(Handler handler, int progress, int max) {
		final Message message = new Message();
		message.what = R.id.main_progress;
		message.arg1 = progress;
		message.arg2 = max;
		handler.sendMessage(message);
	}

	/**
	 *  @param id entry id
	 *  @throws IllegalArgumentException when {@code id} is invalid
	 *  @throws IllegalStateException called before finishing parse
	 */
	public static Entry getEntry(String id) {
		if (!isParseFinished)
			throw new IllegalStateException();
		if (entryMap.containsKey(id))
			return entryMap.get(id);
		throw new IllegalArgumentException("Requiest id does not exist");
	}

	/**
	 * @return list of entry {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getAllEntryId() {
		if (!isParseFinished)
			throw new IllegalStateException();
		return new ArrayList<String>(entryMap.keySet());
	}

	/**
	 * @return list of favorite entry {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getFavoriteEntryId() {
		// normalize
		favoriteList.remove("");
		Collections.sort(favoriteList);

		return favoriteList;
	}

	/**
	 * @param query
	 * @return search result, list of {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getEntryByKeyword(String query) {
		if (!isParseFinished)
			throw new IllegalStateException();

		final EnumSet<EntryKey> searchKeys = EnumSet.of(
				EntryKey.TitleJa, EntryKey.TitleEn, EntryKey.Sponsor, EntryKey.CoSponsor,
				EntryKey.Abstract, EntryKey.Content, EntryKey.Guest, EntryKey.Note);

		final List<String> match = new ArrayList<String>();
		for (Entry entry : entryMap.values()) {
			if (entry.getId().equals(query)) {
				match.add(entry.getId());
				continue;
			}
			for (EntryKey key : searchKeys) {
				final String s = entry.getString(key);
				if (s != null && s.contains(query)) {
					match.add(entry.getId());
					break;
				}
			}
		}
		return match;
	}

	/**
	 * @param day
	 * @param startBegin
	 * @param startEnd
	 * @return search result, list of {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getEntryByTimeFrame(String day, int startBegin, int startEnd) {
		if (!isParseFinished)
			throw new IllegalStateException();

		final List<String> match = new ArrayList<String>();
		for (TimeFrame timeFrame : timeFrameMap.values()) {
			final int start = timeFrame.getStart();
			if (timeFrame.getDay() == day && startBegin <= start && start <= startEnd)
				match.add(timeFrame.getId());
		}
		// TODO sort
		return match;
	}

	public static boolean isFavorite(String id) {
		for (String favoriteId : favoriteList) {
			if (id.equals(favoriteId))
				return true;
		}
		return false;
	}

	public void setFavorite(String id, boolean state) {
		if (state && !favoriteList.contains(id)) {
			favoriteList.add(id);
			updateFavoriteList();
		}
		else if (!state && favoriteList.remove(id)) {
			updateFavoriteList();
		}
	}

	public void clearFavorite() {
		favoriteList.clear();
		updateFavoriteList();
	}

	private void updateFavoriteList() {
		final StringBuilder sb = new StringBuilder();
		for (String favoriteId : favoriteList) {
			sb.append(favoriteId).append(';');
		}

		final SharedPreferences.Editor ee = pref.edit();
		ee.putString("favorites", sb.toString());
		ee.commit();
	}

	@Override
	public String toString() {
		return String.format("%s has %d (Entry) and %d (TimeFrame) data", getClass().getName(), entryMap.size(), timeFrameMap.size());
	}

	public enum EntryKey {
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

		EntryKey(Class<?> theClass) {
			this.c = theClass;
		}

		public boolean equalsClass(Class<?> theClass) {
			return c.equals(theClass);
		}
	};
	public enum EntryCategory { SymposiumAndTalkSession, WorkshopAndScienceCafe, ScienceShowAndDisplay, Other }
	public enum EntryTarget { Child, Student, Teacher, Professional, Adult, Politics, SCer, NonJapanese }

	class Entry {
		private final String id;
		private final EntryCategory category;
		private final Set<EntryTarget> target;
		private final Map<EntryKey, String> data;

		public Entry(String id, String category, String target) {
			this.id			= id;
			this.category	= EntryCategory.valueOf(category);
			this.target		= EnumSet.noneOf(EntryTarget.class);
			this.data		= new EnumMap<EntryKey, String>(EntryKey.class);

			if (target != null) {
				for (String t : target.split(","))
					this.target.add(EntryTarget.valueOf(t));
			}
		}

		public String getId() {
			return id;
		}

		public EntryCategory getCategory() {
			return category;
		}

		/** @return {@code target} or {@code null} */
		public Set<EntryTarget> getTarget() {
			return target;
		}

		// TODO
		public CharSequence getColoredSchedule() {
			final String[] days	 = context.getResources().getStringArray(R.array.scheduleDays);
			final int[] fgColors = context.getResources().getIntArray(R.array.scheduleTextColor);
			final int[] bgColors = context.getResources().getIntArray(R.array.scheduleBackgroundColor);

			final SpannableStringBuilder schedule = new SpannableStringBuilder(data.get(EntryKey.Schedule));
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
		public URL getURL(EntryKey key) {
			if (!key.equalsClass(URL.class))
				throw new IllegalArgumentException();

			final String s = data.get(key);
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
		public String getString(EntryKey key) {
			if (!key.equalsClass(String.class))
				throw new IllegalArgumentException();
			return data.get(key);
		}

		public String getLocaleTitle() {
			final String ja = getString(EntryKey.TitleJa);
			final String en = getString(EntryKey.TitleEn);
			return (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage()) || en == null) ? ja : en;
		}

		public void set(EntryKey key, String value) {
			if (value != null && value.length() > 0)
				data.put(key, value);
		}

		@Override
		public String toString() {
			return getClass().getName() + "@" + getId();
		}
	}

	class TimeFrame {
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

	@SuppressWarnings("serial")
	class UpdateDataAbortException extends Exception {
		public UpdateDataAbortException(String s) {
			super(s);
		}
	}

	@SuppressWarnings("serial")
	class ParseDataAbortException extends Exception {
		public ParseDataAbortException(String s) {
			super(s);
		}
	}
}
