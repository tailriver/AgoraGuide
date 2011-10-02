package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
	private static List<String> favoriteList;

	public AgoraData(Context context) {
		this.context = context.getApplicationContext();
		this.pref	 = this.context.getSharedPreferences("pref", Context.MODE_PRIVATE);

		if (entryMap == null) {
			entryMap	 = new LinkedHashMap<String, Entry>(pref.getInt("initialCapacityOfEntry",	  50));
			timeFrameMap = new HashMap<String, TimeFrame>(  pref.getInt("initialCapacityOfTimeFrame", 50));
			favoriteList = new ArrayList<String>(Arrays.asList(pref.getString("favorites", "").split(";")));
		}
	}

	public boolean isConnected() {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
			final BufferedReader br = new BufferedReader(new InputStreamReader(new URL(context.getString(R.string.versionTextURL)).openStream()), 16);
			final String[] versionTexts = br.readLine().split(";");
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
			// TODO want to use GZIPINPUTSTREAM
//			final BufferedInputStream  bis = new BufferedInputStream(new GZIPInputStream(new URL(context.getString(R.string.XMLDataURL)).openStream()), BUFFER_SIZE);
			final BufferedInputStream  bis = new BufferedInputStream(new URL(context.getString(R.string.XMLDataURL)).openStream(), BUFFER_SIZE);
			final BufferedOutputStream bos = new BufferedOutputStream(context.openFileOutput(context.getString(R.string.XMLDataFilename), Context.MODE_PRIVATE), BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				final int byteRead = bis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);
			}
			bis.close();
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
						final String target		= xpp.getAttributeValue(null, "target");

						if (id == null || category == null)
							throw new XMLParserAbortException();

						entry = new Entry(id, category, target);
						entry.set(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title.ja"));
						entry.set(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title.en"));
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

		final SharedPreferences.Editor ee = pref.edit();
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
		final SharedPreferences.Editor ee = pref.edit();
		ee.remove("localVersion");
		ee.commit();
		Log.w("AgoraData.removeXML", "XMLFile removed");
	}

	public static Entry getEntry(String id) {
		if (entryMap.containsKey(id))
			return entryMap.get(id);
		throw new IllegalArgumentException("Requiest id does not exist");
	}

	public static List<String> getAllEntryId() {
		return new ArrayList<String>(entryMap.keySet());
	}

	public static List<String> getFavoriteEntryId() {
		// normalize entry
		favoriteList.remove("");
		Collections.sort(favoriteList);

		return favoriteList;
	}

	public static List<String> getEntryByKeyword(String query) {
		final EnumSet<EntryKey> searchKeys = EnumSet.of(
				EntryKey.TitleJa, EntryKey.TitleEn, EntryKey.Sponsor, EntryKey.CoSponsor,
				EntryKey.Abstract, EntryKey.Content, EntryKey.Guest, EntryKey.Note);

		final List<String> matched = new ArrayList<String>();
		for (Entry entry : entryMap.values()) {
			if (entry.getId().equals(query)) {
				matched.add(entry.getId());
				continue;
			}
			for (EntryKey key : searchKeys) {
				final String s = entry.getString(key);
				// TODO use regular expressions for query!
				if (s != null && s.contains(query)) {
					matched.add(entry.getId());
					break;
				}
			}
		}
		return matched;
	}

	// TODO not implemented
	public static List<String> getEntryByTimeFrame(String day, int hour, int minute) {
		return new ArrayList<String>();
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
		Log.i("AgoraData", "Favorites: " + sb.toString());

		final SharedPreferences.Editor ee = pref.edit();
		ee.putString("favorites", sb.toString());
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

		public Set<EntryTarget> getTarget() {
			return target;
		}

		public URL getURL(EntryKey key) {
			if (!key.equalsClass(URL.class))
				throw new IllegalArgumentException();

			final String s = data.get(key);
			if (s != null) {
				try {
					return new URL(s);
				}
				catch (MalformedURLException e) { /* ignore */ }
			}

			return null;
		}

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
