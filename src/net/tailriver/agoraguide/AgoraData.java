package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Xml;


class AgoraData {
	private final Context context;
	private final SharedPreferences pref;

	private static Map<String, Entry> entryMap;
	private static Map<String, TimeFrame> timeFrameMap;

	static {
		entryMap	 = new LinkedHashMap<String, Entry>();
		timeFrameMap = new HashMap<String, TimeFrame>();
	}

	public AgoraData(Context context) {
		super();
		this.context = context;
		this.pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
	}

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
	}

	public synchronized void XMLUpdater() {
		if (!isConnected())
			return;

		final int localVersion = pref.getInt("localVersion", 0);
		final int serverVersion;
		final int fileSize; 
		try {
			// format of version.txt
			//		version ; file size of data.xml ; file size of data.xml.gz
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(context.getString(R.string.versionTextURL)).openStream()), 16);
			String[] versionTexts = br.readLine().split(";");
			br.close();
			serverVersion = Integer.parseInt(versionTexts[0]);
			fileSize = Integer.parseInt(versionTexts[2]);
			Log.i("AgoraData.XMLUpdater", String.format("server: %d, local: %d (%d bytes)", serverVersion, localVersion, fileSize));
		}
		catch (IOException e) {
			Log.w("AgoraData.XMLUpdater", "Fail to check the version of data: " + e);
			return;
		}

		if (serverVersion == localVersion || !isConnected())
			return;

		try {
			final int BUFFER_SIZE = 1024;
			GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(new URL(context.getString(R.string.XMLDataURL)).openStream(), BUFFER_SIZE));
			BufferedOutputStream bos = new BufferedOutputStream(context.openFileOutput(context.getString(R.string.XMLDataFilename), Context.MODE_PRIVATE), BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				int byteRead = gis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);
			}
			gis.close();
			bos.flush();
			bos.close();

			SharedPreferences.Editor ee = pref.edit();
			ee.putInt("localVersion", serverVersion);
			ee.commit();
			Log.i("AgoraData.XMLUpdater", "XML update successed");
		}
		catch (IOException e) {
			// TODO rerun?
			Log.w("AgoraData.XMLUpdater", "XML update failed: " + e);
		}
	}

	public synchronized void XMLParser() throws XMLParserAbortException {
		XmlPullParser xpp = Xml.newPullParser();

		try {
			xpp.setInput(context.openFileInput(context.getString(R.string.XMLDataFilename)), null);
		}
		catch (Exception e) {
			Log.e("AgoraData.XMLParser", "Cannot read XMLFile: " + e);
			throw new XMLParserAbortException();
		}

		clearCache();

		// Loop over XML input stream and process events
		try {
			Entry entry = null;
			for (int e = xpp.getEventType(); e != XmlPullParser.END_DOCUMENT; e = xpp.next()) {
				String tag;
				switch (e) {
				case XmlPullParser.START_TAG:
					tag = xpp.getName();
					if ("entry".equals(tag)) {
						String id = xpp.getAttributeValue(null, "id");
						if (id == null)
							throw new XMLParserAbortException();

						entry = new Entry(id);
						entry.set(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title.ja"));
						entry.set(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title.en"));
						entry.set(EntryKey.Sponsor,		xpp.getAttributeValue(null, "sponsor"));
						entry.set(EntryKey.CoSponsor,	xpp.getAttributeValue(null, "cosponsor"));
						entry.set(EntryKey.Image,		xpp.getAttributeValue(null, "image"));
						entry.set(EntryKey.Location,	xpp.getAttributeValue(null, "location"));
						entry.set(EntryKey.Website,		xpp.getAttributeValue(null, "url"));
						entry.set(EntryKey.Category,	xpp.getAttributeValue(null, "category"));
						entry.set(EntryKey.Target,		xpp.getAttributeValue(null, "target"));
						entry.set(EntryKey.Schedule,	xpp.getAttributeValue(null, "schedule"));
						entryMap.put(id, entry);
						break;
					}
					if (entry == null)
						break;

					if ("timeframe".equals(tag)) {
						String sid	= xpp.getAttributeValue(null, "id");
						String id	= entry.getId();
						String day	= xpp.getAttributeValue(null, "day");
						int start	= Integer.parseInt(xpp.getAttributeValue(null, "start"));
						int end		= Integer.parseInt(xpp.getAttributeValue(null, "end"));
						timeFrameMap.put(sid, new TimeFrame(sid, id, day, start, end));
						entry.addScheduleId(sid);
						break;
					}

					if ("abstract".equals(tag))
						entry.set(EntryKey.Abstract,	xpp.nextText());
					else if ("content".equals(tag))
						entry.set(EntryKey.Content,		xpp.nextText());
					else if ("reservation".equals(tag))
						entry.set(EntryKey.Reservation,	xpp.nextText());
					else if ("note".equals(tag))
						entry.set(EntryKey.Note,		xpp.nextText());
					else
						Log.i("AgoraData.XMLParser", entry + ": " + tag + " is not implemented");
					break;

				case XmlPullParser.END_TAG:
					tag = xpp.getName();
					if ("entry".equals(tag)) {
						entry = null;
					}
					break;
				}
			}
		}
		catch (XmlPullParserException e) {
			clearCache();
			Log.w("AgoraData.XMLParser", "parse aborted: " + e);
			throw new XMLParserAbortException();
		}
		catch (IOException e) {
			clearCache();
			removeDataFile();
			Log.w("AgoraData.XMLParser", "parse aborted: " + e);
			throw new XMLParserAbortException();
		}
	}

	public static void clearCache() {
		entryMap.clear();
		timeFrameMap.clear();
	}

	// TODO
	public void removeDataFile() {
		SharedPreferences.Editor ee = pref.edit();
		ee.putInt("localVersion", 0);
		ee.commit();
		Log.w("AgoraData.removeXML", "XMLFile removed");
	}

	public static Entry getEntry(String id) {
		if (entryMap.containsKey(id))
			return entryMap.get(id);
		throw new IllegalArgumentException();
	}

	public static List<Entry> getAllEntry() {
		return new ArrayList<Entry>(entryMap.values());
	}

	public static List<Entry> getEntryByKeyword(String query) {
		final EnumSet<EntryKey> searchKeys = EnumSet.of(
				EntryKey.TitleJa, EntryKey.TitleEn, EntryKey.Sponsor, EntryKey.CoSponsor,
				EntryKey.Abstract, EntryKey.Content, EntryKey.Note);

		List<Entry> matched = new ArrayList<Entry>();
		for (Entry entry : entryMap.values()) {
			for (EntryKey key : searchKeys) {
				// TODO use regular expressions for query!
				if (entry.getString(key).contains(query)) {
					matched.add(entry);
					break;
				}
			}
		}
		return matched;
	}

	// TODO not implemented
	public static List<Entry> getEntryByTimeFrame(String day, int hour, int minute) {
		return new ArrayList<Entry>();
	}

	public Locale getAppLocale() {
		if (pref.contains("appLocale"))
			return new Locale(pref.getString("appLocale", Locale.getDefault().getLanguage()));
		else
			return Locale.getDefault();
	}

	public void setAppLocale(Locale appLocale) {
		SharedPreferences.Editor ee = pref.edit();
		if (appLocale == null)
			ee.remove("appLocale");
		else
			ee.putString("appLocale", appLocale.getLanguage());
		ee.commit();
	}

	@Override
	public String toString() {
		return getClass().getName() + "@Context: " + ((context == null) ? "static" : context.getClass().getName());
	}

	public enum EntryKey {
		TitleJa(String.class),
		TitleEn(String.class),
		Sponsor(String.class),
		CoSponsor(String.class),
		Image(URL.class),
		Category(EntryCategory.class),
		Target(HashSet.class),
		Abstract(String.class),
		Location(String.class),
		Schedule(String.class),
		ScheduleSet(HashMap.class),
		Content(String.class),
		Reservation(String.class),
		Website(URL.class),
		Note(String.class),
		;

		private final Class<?> dataClass;

		EntryKey(Class<?> dataClass) {
			this.dataClass = dataClass;
		}

		public Class<?> getDataClass() {
			return dataClass;
		}

		@Override
		public String toString() {
			return super.toString();
		}
	};
	public enum EntryCategory { NULL, SymposiumAndTalkSession, ScienceShow, WorkshopAndCafe, PlayAndManufacture, Booth, Poster, Other }
	public enum EntryTarget { Child, Student, Teacher, Professional, Adult, Politics, SCer, NonJapanese }

	class Entry {
		private final String id;
		private EnumMap<EntryKey, Object> data;

		public Entry(String id) {
			this.id = id;
			data = new EnumMap<EntryKey, Object>(EntryKey.class);
			data.put(EntryKey.Category,		EntryCategory.NULL);
			data.put(EntryKey.Target,		EnumSet.noneOf(EntryTarget.class));
			data.put(EntryKey.ScheduleSet,	new HashSet<String>());
		}

		public String getId() {
			return id;
		}

		public Object get(EntryKey key) {
			return data.get(key);
		}

		public String getString(EntryKey key) {
			if (!key.getDataClass().equals(String.class))
				throw new IllegalArgumentException();
			final String s = (String) data.get(key);
			return (s == null) ? "" : s;
		}

		public String getLocaleTitle() {
			final String ja = (String) getString(EntryKey.TitleJa);
			final String en = (String) getString(EntryKey.TitleEn);
			return (getAppLocale().equals(Locale.JAPANESE.getLanguage()) || en.length() == 0) ? ja : en;
		}

		@Deprecated
		public String getLocaleString(EntryKey key) {
			final EntryKey keyJa, keyEn;
			if (EnumSet.of(EntryKey.TitleJa, EntryKey.TitleEn).contains(key)) {
				keyJa = EntryKey.TitleJa;
				keyEn = EntryKey.TitleEn;
			}
			else
				throw new IllegalArgumentException();

			final String ja = (String) getString(keyJa);
			final String en = (String) getString(keyEn);
			return (getAppLocale().equals(Locale.JAPANESE.getLanguage()) || en.length() == 0) ? ja : en;
		}

		public void set(EntryKey key, String value) {
			switch (key) {
			case Category:
				//				this.genre = (genre == null) ? EntryGenre.NULL : EntryGenre.valueOf(genre);
				EntryCategory ec = (EntryCategory) data.get(key);
				ec = EntryCategory.NULL;
				data.put(key, ec);
				break;

			case Target:
				if (key == null)
					break;
				@SuppressWarnings("unchecked")
				EnumSet<EntryTarget> et = (EnumSet<EntryTarget>) data.get(key);
				//				String[] targets = target.split(",");
				//				for (int i = 0; i < targets.length; i++) {
				//					this.target.add(EntryTarget.values()[Integer.parseInt(targets[i])]);
				//				}				EnumSet<EntryTarget> et = (EnumSet<EntryTarget>) data.get(key);
				et.add(EntryTarget.SCer);
				data.put(key, et);
				break;

			case ScheduleSet:
				@SuppressWarnings("unchecked")
				Set<String> ss = (Set<String>) data.get(key);
				ss.add(value);
				data.put(key, ss);
				break;

			case Image:
			case Website:
				try {
					data.put(key, new URL(value));
				} catch (MalformedURLException e) {
					data.put(key, null);
				}
				break;

			default:
				data.put(key, value);
				break;
			}
		}

		public void addScheduleId(String scheduleId) {
			set(EntryKey.ScheduleSet, scheduleId);
		}

		@Override
		public String toString() {
			return getClass().getName() + "@" + getId();
		}
	}

	class TimeFrame {
		final char[] sid;	// schedule id
		final char[] id;	// entry id
		final char[] day;
		final int start;
		final int end;

		public TimeFrame(String sid, String id, String day, int start, int end) {
			this.sid = sid.toCharArray();
			this.id = id.toCharArray();
			this.day = day.toCharArray();
			this.start = start;
			this.end = end;
		}
	}

	@SuppressWarnings("serial")
	class XMLParserAbortException extends Exception {
		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}
}
