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

	public AgoraData(Context context) {
		super();
		this.context = context;
		this.pref	 = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		entryMap	 = new LinkedHashMap<String, Entry>(pref.getInt("initialCapacityOfEntry",	  50));
		timeFrameMap = new HashMap<String, TimeFrame>(  pref.getInt("initialCapacityOfTimeFrame", 50));
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
		final XmlPullParser xpp = Xml.newPullParser();

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
				switch (e) {
				case XmlPullParser.START_TAG:
					final String startTag = xpp.getName();
					if ("entry".equals(startTag)) {
						final String id			= xpp.getAttributeValue(null, "id");
						final String category	= xpp.getAttributeValue(null, "category");
						if (id == null || category == null)
							throw new XMLParserAbortException();

						entry = new Entry(id, category);
						entry.set(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title.ja"));
						entry.set(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title.en"));
						entry.set(EntryKey.Sponsor,		xpp.getAttributeValue(null, "sponsor"));
						entry.set(EntryKey.CoSponsor,	xpp.getAttributeValue(null, "cosponsor"));
						entry.set(EntryKey.Image,		xpp.getAttributeValue(null, "image"));
						entry.set(EntryKey.Target,		xpp.getAttributeValue(null, "target"));
						entry.set(EntryKey.Location,	xpp.getAttributeValue(null, "location"));
						entry.set(EntryKey.Schedule,	xpp.getAttributeValue(null, "schedule"));
						entry.set(EntryKey.Guest,		xpp.getAttributeValue(null, "guest"));
						entry.set(EntryKey.Website,		xpp.getAttributeValue(null, "url"));
						entryMap.put(id, entry);
						break;
					}
					if (entry == null)
						break;

					if ("timeframe".equals(startTag)) {
						final String eid	= entry.getId();
						final String tfid	= xpp.getAttributeValue(null, "id");
						final String day	= xpp.getAttributeValue(null, "day");
						final int start		= Integer.parseInt(xpp.getAttributeValue(null, "start"));
						final int end		= Integer.parseInt(xpp.getAttributeValue(null, "end"));
						timeFrameMap.put(tfid, new TimeFrame(tfid, eid, day, start, end));
						break;
					}

					if ("abstract".equals(startTag))
						entry.set(EntryKey.Abstract,	xpp.nextText());
					else if ("content".equals(startTag))
						entry.set(EntryKey.Content,		xpp.nextText());
					else if ("reservation".equals(startTag))
						entry.set(EntryKey.Reservation,	xpp.nextText());
					else if ("note".equals(startTag))
						entry.set(EntryKey.Note,		xpp.nextText());
					else
						Log.i("AgoraData.XMLParser", entry + ": " + startTag + " is not implemented");
					break;

				case XmlPullParser.END_TAG:
					final String closeTag = xpp.getName();
					if ("entry".equals(closeTag))
						entry = null;
					break;
				}
			}
		}
		catch (Exception e) {
			clearCache();
			Log.w("AgoraData.XMLParser", "parse aborted: " + e);
			throw new XMLParserAbortException();
		}

		SharedPreferences.Editor ee = pref.edit();
		ee.putInt("initialCapacityOfEntryMap", (int) (entryMap.size() * 1.5));
		ee.putInt("initialCapacityOfTimeFrameMap", (int) (timeFrameMap.size() * 1.5));
		ee.commit();
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
		Abstract(String.class),
		Location(String.class),
		Schedule(String.class),
		Content(String.class),
		Guest(String.class),
		Note(String.class),
		Reservation(String.class),
		Image(URL.class),
		Website(URL.class),
		Target(HashSet.class),
		;

		private final Class<?> dataClass;

		EntryKey(Class<?> dataClass) {
			this.dataClass = dataClass;
		}

		public Class<?> getDataClass() {
			return dataClass;
		}
	};
	public enum EntryCategory { SymposiumAndTalkSession, WorkshopAndScienceCafe, ScienceShowAndDisplay, Other }
	public enum EntryTarget { Child, Student, Teacher, Professional, Adult, Politics, SCer, NonJapanese }

	class Entry {
		private final String id;
		private final EntryCategory category;
		private final Map<EntryKey, CharSequence> stringData;
		private final Map<EntryKey, URL> urlData;
		private final Set<EntryTarget> target;

		public Entry(String id, String category) {
			this.id			= id;
			this.category	= EntryCategory.valueOf(category);
			stringData		= new EnumMap<EntryKey, CharSequence>(EntryKey.class);
			urlData			= new EnumMap<EntryKey, URL>(EntryKey.class);
			target			= EnumSet.noneOf(EntryTarget.class);
		}

		public String getId() {
			return id;
		}

		public EntryCategory getCategory() {
			return category;
		}

		public Set<EntryTarget> getTarget() {
			return target;
		}

		public URL getURL(EntryKey key) {
			if (!key.getDataClass().equals(URL.class))
				throw new IllegalArgumentException();
			return urlData.get(key);
		}

		public String getString(EntryKey key) {
			if (!key.getDataClass().equals(String.class))
				throw new IllegalArgumentException();
			final CharSequence cs = stringData.get(key);
			return (cs == null) ? "" : cs.toString();
		}

		public String getLocaleTitle() {
			final String ja = (String) getString(EntryKey.TitleJa);
			final String en = (String) getString(EntryKey.TitleEn);
			return (getAppLocale().equals(Locale.JAPANESE.getLanguage()) || en.length() == 0) ? ja : en;
		}

		public void set(EntryKey key, String value) {
			switch (key) {
			case Target:
				if (value == null)
					break;
				//				String[] targets = target.split(",");
				//				for (int i = 0; i < targets.length; i++) {
				//					this.target.add(EntryTarget.values()[Integer.parseInt(targets[i])]);
				//				}				EnumSet<EntryTarget> et = (EnumSet<EntryTarget>) data.get(key);
				target.add(EntryTarget.valueOf(value));
				break;

			case Image:
			case Website:
				try {
					URL url = new URL(value);
					urlData.put(key, url);
				} catch (MalformedURLException e) {
					urlData.put(key, null);
				}
				break;

			default:
				stringData.put(key, value);
				break;
			}
		}

		@Override
		public String toString() {
			return getClass().getName() + "@" + getId();
		}
	}

	class TimeFrame {
//		private final String tfid;	// schedule id
		private final String eid;	// entry id
		private final String day;
		private final int start;
		private final int end;

		public TimeFrame(String tfid, String eid, String day, int start, int end) {
//			this.tfid	= tfid;
			this.eid	= eid;
			this.day	= day;
			this.start	= start;
			this.end	= end;
		}

		public Entry getEntry() {
			return AgoraData.getEntry(eid.toString());
		}

		public boolean isInSession(String day, int time) {
			return this.day.equals(day) && start <= time && time < end;
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
