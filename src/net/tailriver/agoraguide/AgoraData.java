package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Xml;


class AgoraData {
	private Context context;
	private boolean isLocaleJapanese;

	private static ArrayList<String> entryOrder;
	private static HashMap<String, Entry> entryMap;
	private static HashMap<String, TimeFrame> timeFrameMap;
	private static int localVersion, serverVersion;
	private static final String dataURL, versionURL, XMLFilename;

	static {
		entryOrder		= new ArrayList<String>();
		entryMap		= new HashMap<String, Entry>();
		timeFrameMap	= new HashMap<String, TimeFrame>();
		dataURL			= "http://www.tailriver.net/scienceagora/2010/data.xml";
		versionURL		= "http://www.tailriver.net/scienceagora/2010/version.txt";
		XMLFilename		= "data.xml";
	}

	public AgoraData() {
		this(null);
	}

	public AgoraData(Context context) {
		super();
		this.context = context;
		isLocaleJapanese = Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage());
	}

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
	}

	public boolean isXMLUpdated() {
		if (!isConnected())
			return false;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(versionURL).openStream()), 16);
			serverVersion = Integer.parseInt(br.readLine());

			SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
			localVersion = pref.getInt("localVersion", 0);

			Log.i("XMLUpdateNotifierr", "server: " + String.valueOf(serverVersion) + ", local: " + String.valueOf(localVersion));
			return serverVersion > localVersion;
		}
		catch (IOException e) {
			Log.w("AgoraData.XMLUpdateNotifier", e.toString());
			return false;
		}
	}

	public synchronized void XMLUpdater() {
		if (!isConnected())
			return;

		// TODO write to temporally file and rename? 
		try {
			final int BUFFER_SIZE = 1024;
			BufferedInputStream bis = new BufferedInputStream(new URL(dataURL).openStream(), BUFFER_SIZE);
			BufferedOutputStream bos = new BufferedOutputStream(context.openFileOutput(XMLFilename, Context.MODE_PRIVATE), BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				int byteRead = bis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);
			}
			bis.close();
			bos.flush();
			bos.close();

			if (serverVersion != localVersion) {
				SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
				SharedPreferences.Editor spe = pref.edit();
				spe.putInt("localVersion", serverVersion);
				spe.commit();
				Log.i("AgoraData.XMLUpdater", "XML update successed");
				localVersion = serverVersion;
			}
		}
		catch (IOException e) {
			// TODO rerun?
			Log.w("AgoraData.XMLUpdater", "XML update failed: " + e);
		}
	}

	public synchronized void XMLParser() throws XMLParserAbortException {
		XmlPullParser xpp = Xml.newPullParser();

		try {
			xpp.setInput(context.openFileInput(XMLFilename), null);
		}
		catch (Exception e) {
			Log.e("AgoraData.XMLParser", "Cannot read datafile: " + e);
			throw new XMLParserAbortException();
		}

		clearCache();

		// Loop over XML input stream and process events
		try {
			Entry entry = null;
			boolean inSchedule = false;
			for (int e = xpp.getEventType(); e != XmlPullParser.END_DOCUMENT; e = xpp.next()) {
				String tag;
				switch (e) {
				case XmlPullParser.START_TAG:
					tag = xpp.getName();
					if ("act".equals(tag)) {
						String id = xpp.getAttributeValue(null, "id");
						if (id == null)
							throw new XMLParserAbortException();

						entry = new Entry(id);
						entry.setData(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title.ja"));
						entry.setData(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title.en"));
						entry.setData(EntryKey.ExhibitorJa,	xpp.getAttributeValue(null, "exhibitor.ja"));
						entry.setData(EntryKey.ExhibitorEn,	xpp.getAttributeValue(null, "exhibitor.en"));
						entry.setData(EntryKey.Image,		xpp.getAttributeValue(null, "image"));
						entry.setData(EntryKey.Location,	xpp.getAttributeValue(null, "location"));
						entry.setData(EntryKey.FixedNumber,	xpp.getAttributeValue(null, "fixedNumber"));
						entry.setData(EntryKey.Website,		xpp.getAttributeValue(null, "website"));
						entry.setData(EntryKey.Genre,		xpp.getAttributeValue(null, "genre"));
						entry.setData(EntryKey.Target,		xpp.getAttributeValue(null, "target"));
						entry.setData(EntryKey.Reservation, xpp.getAttributeValue(null, "reservation"));
						entryMap.put(id, entry);
						entryOrder.add(id);
						break;
					}
					if (entry == null)
						break;

					else if ("schedule".equals(tag)) {
						inSchedule = true;
						entry.setData(EntryKey.Schedule,	xpp.getAttributeValue(null, "string"));
						break;
					}
					if (inSchedule && "timeframe".equals(tag)) {
						String id	= xpp.getAttributeValue(null, "id");
						String day	= xpp.getAttributeValue(null, "day");
						int start	= Integer.parseInt(xpp.getAttributeValue(null, "start"));
						int end		= Integer.parseInt(xpp.getAttributeValue(null, "end"));
						timeFrameMap.put(id, new TimeFrame(id, day, start, end));
						entry.addScheduleId(id);
						break;
					}

					if ("abstract".equals(tag))
						entry.setData(EntryKey.Abstract,	xpp.nextText());
					else if ("content".equals(tag))
						entry.setData(EntryKey.Content,		xpp.nextText());
					else if ("note".equals(tag))
						entry.setData(EntryKey.Note,		xpp.nextText());
					else
						Log.i("AgoraData.XMLParser", entry + ": " + tag + " is not implemented");
					break;

				case XmlPullParser.END_TAG:
					tag = xpp.getName();
					if ("schedule".equals(tag)) {
						inSchedule = false;
					}
					else if ("act".equals(tag)) {
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

	public void clearCache() {
		entryOrder.clear();
		entryMap.clear();
		timeFrameMap.clear();
	}

	// TODO
	public void removeDataFile() {
		SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
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

	public static ArrayList<Entry> getAllEntry() {
		return new ArrayList<Entry>(entryMap.values());
	}

	public static ArrayList<Entry> getEntryByKeyword(String query) {
		final EnumSet<EntryKey> searchKeys = EnumSet.of(
				EntryKey.TitleJa, EntryKey.TitleEn, EntryKey.ExhibitorJa, EntryKey.ExhibitorEn,
				EntryKey.Abstract, EntryKey.Content, EntryKey.Note);

		ArrayList<Entry> matched = new ArrayList<Entry>();
		for (Entry entry : entryMap.values()) {
			for (EntryKey key : searchKeys) {
				// TODO use regular expressions for query!
				if (entry.getDataString(key).contains(query)) {
					matched.add(entry);
					break;
				}
			}
		}
		return matched;
	}

	public static ArrayList<Entry> getEntryByTimeFrame(String day, int hour, int minute) {
		return new ArrayList<Entry>();
	}

	@Override
	public String toString() {
		return getClass().getName() + "@Context: " + ((context == null) ? "static" : context.getClass().getName());
	}

	public enum EntryKey {
		TitleJa, TitleEn, ExhibitorJa, ExhibitorEn, Abstract, Location, Schedule, Content, Note,	// String
		Genre, Target, ScheduleSet,		// Collection (they have their own variables) 
		Image, Website,					// URL
		FixedNumber,					// Integer
		Reservation,					// Boolean
	};
	public enum EntryGenre { NULL, SymposiumAndTalkSession, ScienceShow, WorkshopAndCafe, PlayAndManufacture, Booth, Poster, Other }
	public enum EntryTarget { Child, Student, Teacher, Professional, Adult, Politics, SCer, NonJapanese }

	class Entry {
		private final String id;
		private EnumMap<EntryKey, Object> data;
		private EntryGenre dataGenre;
		private EnumSet<EntryTarget> dataTarget;
		private HashSet<String> dataScheduleSet;

		public Entry(String id) {
			this.id = id;
			data = new EnumMap<EntryKey, Object>(EntryKey.class);
			data.put(EntryKey.Image,		null);
			data.put(EntryKey.Website,		null);
			data.put(EntryKey.FixedNumber,	-1);
			data.put(EntryKey.Reservation,	false);
			dataGenre = EntryGenre.NULL;
			dataTarget = EnumSet.noneOf(EntryTarget.class);
			dataScheduleSet = new HashSet<String>();
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			final String ja = (String) getDataString(EntryKey.TitleJa);
			final String en = (String) getDataString(EntryKey.TitleEn);
			return (isLocaleJapanese || en.length() == 0) ? ja : en;
		}

		public String getExhibitor() {
			final String ja = (String) getDataString(EntryKey.ExhibitorJa);
			final String en = (String) getDataString(EntryKey.ExhibitorEn);
			return (isLocaleJapanese || en.length() == 0) ? ja : en;
		}

		public EntryGenre getGenre() {
			return dataGenre;
		}

		public EnumSet<EntryTarget> getTarget() {
			return dataTarget;
		}

		public HashSet<String> getScheduleId() {
			return dataScheduleSet;
		}

		private Class<?> getDataClass(EntryKey key) {
			switch (key) {
			case Genre:
				return dataGenre.getClass();
			case Target:
				return dataTarget.getClass();
			case ScheduleSet:
				return dataScheduleSet.getClass();
			case Image:
			case Website:
				return URL.class;
			case FixedNumber:
				return Integer.class;
			case Reservation:
				return Boolean.class;
			default:
				return String.class;
			}
		}

		private void checkClass(EntryKey key, Class<?> theClass) {
			if (!getDataClass(key).equals(theClass))
				throw new IllegalArgumentException(key + " is a class of " + getDataClass(key));
		}

		public URL getDataURL(EntryKey key) {
			checkClass(key, URL.class);
			return (URL) data.get(key);
		}

		public Integer getDataInteger(EntryKey key) {
			checkClass(key, Integer.class);
			return (Integer) data.get(key);
		}

		public Boolean getDataBoolean(EntryKey key) {
			checkClass(key, Boolean.class);
			return (Boolean) data.get(key);
		}

		public String getDataString(EntryKey key) {
			checkClass(key, String.class);
			final String s = (String) data.get(key);
			return s == null ? "" : s;
		}

		public void setData(EntryKey key, String value) {
			switch (key) {
			case Genre:
				dataGenre = EntryGenre.NULL;
				break;

			case Target:
				dataTarget.add(EntryTarget.Student);
				break;

			case ScheduleSet:
				dataScheduleSet.add(value);
				break;

			case Image:
			case Website:
				try {
					data.put(key, new URL((String) value));
				} catch (MalformedURLException e) {
					data.put(key, null);
				}
				break;

			case FixedNumber:
				try {
					data.put(key, Integer.parseInt(value));
				} catch (NumberFormatException e) {
					data.put(key, -1);
				}
				break;

			case Reservation:
				data.put(key, (value == null) ? false : value.equals("required"));
				break;

			default:
				data.put(key, value);
				break;
			}
		}

		public void addScheduleId(String scheduleId) {
			setData(EntryKey.ScheduleSet, scheduleId);
		}

		@Deprecated
		public void setGenre(String genre) {
			setData(EntryKey.Genre, genre);
//			this.genre = (genre == null) ? EntryGenre.NULL : EntryGenre.valueOf(genre);
		}

		@Deprecated
		public void setTarget(String target) {
			setData(EntryKey.Target, target);
//			if (target == null)
//				return;
//			String[] targets = target.split(",");
//			for (int i = 0; i < targets.length; i++) {
//				this.target.add(EntryTarget.values()[Integer.parseInt(targets[i])]);
//			}
		}

		@Override
		public String toString() {
			return getClass().getName() + "@" + id;
		}

	}

	class TimeFrame {
		final String id;
		final String day;
		final int start;
		final int end;

		public TimeFrame(String id, String day, int start, int end) {
			this.id = id;
			this.day = day;
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
