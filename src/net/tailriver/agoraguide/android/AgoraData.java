package net.tailriver.agoraguide.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Xml;


public class AgoraData {
	private Context context;
	private boolean isLocaleJapanese;

	private static ArrayList<String> entryIdList;
	private static HashMap<String, Entry> entryMap;
	private static HashMap<String, TimeFrame> timeFrameMap;
	private static int localVersion, serverVersion;
	private static URL dataURL, versionURL;
	private static String XMLFilename;

	static {
		entryIdList		= new ArrayList<String>();
		entryMap		= new HashMap<String, Entry>();
		timeFrameMap	= new HashMap<String, TimeFrame>();
		XMLFilename		= "data.xml";
		try {
			dataURL		= new URL("http://www.tailriver.net/scienceagora/2010/data.xml");
			versionURL	= new URL("http://www.tailriver.net/scienceagora/2010/version.txt");
		}
		catch (MalformedURLException e) {
			Log.e("AgoraData", e.toString());
		}
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

	public boolean XMLUpdateNotifier() {
		if (!isConnected())
			return false;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(versionURL.openStream()), 16);
			serverVersion = Integer.parseInt(br.readLine());

			SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
			localVersion = pref.getInt("localVersion", 0);

			Log.i("XMLUpdateNotifierr", "server: " + String.valueOf(serverVersion) + ", local: " + String.valueOf(localVersion));
			return serverVersion > localVersion;
		}
		catch (Exception e) {
			Log.w("AgoraData.XMLUpdateNotifier", e.toString());
			return false;
		}
	}

	public synchronized void XMLUpdater() {
		if (!isConnected())
			return;

		try {
			final int BUFFER_SIZE = 1024;
			BufferedInputStream bis = new BufferedInputStream(dataURL.openStream(), BUFFER_SIZE);
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
		catch (Exception e) {
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

		entryIdList.clear();
		entryMap.clear();
		timeFrameMap.clear();

		Entry entry = null;
		boolean inAct = false;
		boolean inSchedule = false;

		// Loop over XML input stream and process events
		try {
			for (int e = xpp.getEventType(); e != XmlPullParser.END_DOCUMENT; e = xpp.next()) {
				String tag;
				switch (e) {
				case XmlPullParser.START_TAG:
					tag = xpp.getName();
					if ("act".equals(tag)) {
						inAct = true;
						String id = xpp.getAttributeValue(null, "id");
						entryMap.put(id, new Entry(id));
						entryIdList.add(id);
						entry = entryMap.get(id);
						entry.setInternal(EntryKey.TitleJa,		xpp.getAttributeValue(null, "title.ja"));
						entry.setInternal(EntryKey.TitleEn,		xpp.getAttributeValue(null, "title.en"));
						entry.setInternal(EntryKey.ExhibitorJa,	xpp.getAttributeValue(null, "exhibitor.ja"));
						entry.setInternal(EntryKey.ExhibitorEn,	xpp.getAttributeValue(null, "exhibitor.en"));
						entry.setInternal(EntryKey.Image,		xpp.getAttributeValue(null, "image"));
						entry.setInternal(EntryKey.Location,	xpp.getAttributeValue(null, "location"));
						entry.setInternal(EntryKey.FixedNumber,	xpp.getAttributeValue(null, "fixedNumber"));
						entry.setInternal(EntryKey.Website,		xpp.getAttributeValue(null, "website"));
						entry.setGenre(xpp.getAttributeValue(null, "genre"));
						entry.setTarget(xpp.getAttributeValue(null, "target"));
						entry.setReservation(xpp.getAttributeValue(null, "reservation"));
						break;
					}
					if (!inAct)
						break;

					if ("schedule".equals(tag)) {
						inSchedule = true;
						entry.setInternal(EntryKey.Schedule,	xpp.getAttributeValue(null, "string"));
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
						entry.setInternal(EntryKey.Abstract,	xpp.getAttributeValue(null, "abstract"));
					else if ("content".equals(tag))
						entry.setInternal(EntryKey.Content,		xpp.getAttributeValue(null, "content"));
					else if ("note".equals(tag))
						entry.setInternal(EntryKey.Note,		xpp.getAttributeValue(null, "note"));
					else
						Log.i("AgoraData.XMLParser", entry.getId() + ": " + tag.toString() + " is not implemented");
					break;

				case XmlPullParser.END_TAG:
					tag = xpp.getName();
					if ("schedule".equals(tag)) {
						inSchedule = false;
					}
					else if ("act".equals(tag)) {
						inAct = false;
					}
					break;
				}
			}
		}
		catch (Exception e) {
			Log.w("AgoraData.XMLParser", "parse aborted: " + e);
			throw new XMLParserAbortException();
		}
	}

	public static ArrayList<String> getEntryIdList() {
		return entryIdList;
	}

	public static Entry getEntry(String id) {
		return entryMap.containsKey(id) ? entryMap.get(id) : null;
	}

	private enum EntryKey { Id, TitleJa, TitleEn, ExhibitorJa, ExhibitorEn, Image, Abstract, Location, Schedule, FixedNumber, Content, Website, Note }
	public enum EntryGenre { NULL, SymposiumAndTalkSession, ScienceShow, WorkshopAndCafe, PlayAndManufacture, Booth, Poster, Other }
	public enum EntryTarget { Child, Student, Teacher, Professional, Adult, Politics, SCer, NonJapanese }
	public class Entry {
		private EnumMap<EntryKey, String> data;
		private EntryGenre genre;
		private EnumSet<EntryTarget> target;
		private HashSet<String> scheduleSet;
		private boolean reservation;

		public Entry(String id) {
			data = new EnumMap<EntryKey, String>(EntryKey.class);
			genre = EntryGenre.NULL;
			target = EnumSet.noneOf(EntryTarget.class);
			scheduleSet = new HashSet<String>();
			reservation = false;
			data.put(EntryKey.Id, id);
		}

		void setInternal(EntryKey key, String value) {
			if (value == null)
				value = "";
			data.put(key, value);
		}

		public void setGenre(String genre) {
			this.genre = EntryGenre.NULL;
			//			this.genre = (genre == null) ? EntryGenre.NULL : EntryGenre.valueOf(genre);
		}

		public void setTarget(String target) {
			this.target.add(EntryTarget.Student);
			//			if (target == null)
			//				return;
			//			String[] targets = target.split(",");
			//			for (int i = 0; i < targets.length; i++) {
			//				this.target.add(EntryTarget.values()[Integer.parseInt(targets[i])]);
			//			}
		}

		public void addScheduleId(String scheduleId) {
			this.scheduleSet.add(scheduleId);
		}

		public void setReservation(String reservation) {
			this.reservation = (reservation == null) ? false : reservation.equals("required");
		}

		private URL getInternalURL(EntryKey key) {
			try {
				final String s = data.get(key);
				return (s.length() != 0) ? new URL(s) : null;
			}
			catch (Exception e) {
				return null;
			}
		}

		private int getInternalInteger(EntryKey key) {
			try {
				return Integer.parseInt(data.get(key));
			}
			catch (Exception e) {
				return -1;
			}
		}

		public String getId() {
			return data.get(EntryKey.Id);
		}

		public String getTitle() {
			final String ja = data.get(EntryKey.TitleJa);
			final String en = data.get(EntryKey.TitleEn);
			return (isLocaleJapanese || en.length() == 0) ? ja : en;
		}

		public String getExhibitor() {
			final String ja = data.get(EntryKey.ExhibitorJa);
			final String en = data.get(EntryKey.ExhibitorEn);
			return (isLocaleJapanese || en.length() == 0) ? ja : en;
		}

		public EntryGenre getGenre() {
			return genre;
		}

		public EnumSet<EntryTarget> getTarget() {
			return target;
		}

		public URL getImage() {
			return getInternalURL(EntryKey.Image);
		}

		public String getLocation() {
			return data.get(EntryKey.Location);
		}

		public String getScheduleString() {
			return data.get(EntryKey.Schedule);
		}

		public HashSet<String> getScheduleId() {
			return scheduleSet;
		}

		public int getFixedNumber() {
			return getInternalInteger(EntryKey.FixedNumber);
		}


		public boolean isReservation() {
			return reservation;
		}

		public URL getWebsite() {
			return getInternalURL(EntryKey.Website);
		}

		public String getAbstract() {
			return data.get(EntryKey.Abstract);
		}

		public String getContent() {
			return data.get(EntryKey.Content);
		}

		public String getNote() {
			return data.get(EntryKey.Note);
		}
	}

	public class TimeFrame {
		String id;
		String day;
		int start;
		int end;

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
