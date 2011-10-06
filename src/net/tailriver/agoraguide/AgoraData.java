package net.tailriver.agoraguide;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import net.tailriver.agoraguide.Entry.*;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

public class AgoraData {
	private static final EnumSet<Tag> searchByKeywordTags;

	private static Map<String, Entry> entryMap;
	private static Map<String, TimeFrame> timeFrameMap;
	private static List<String> favoriteList;
	private static boolean isParseFinished = false;

	static {
		searchByKeywordTags = EnumSet.of(
				Tag.TitleJa, Tag.TitleEn, Tag.Sponsor, Tag.CoSponsor,
				Tag.Abstract, Tag.Content, Tag.Guest, Tag.Note
				);
	}

	private final Context context;
	private final SharedPreferences pref;

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
						entry.set(Tag.TitleJa,		xpp.getAttributeValue(null, "title_ja"));
						entry.set(Tag.TitleEn,		xpp.getAttributeValue(null, "title_en"));
						entry.set(Tag.Sponsor,		xpp.getAttributeValue(null, "sponsor"));
						entry.set(Tag.CoSponsor,	xpp.getAttributeValue(null, "cosponsor"));
						entry.set(Tag.Image,		xpp.getAttributeValue(null, "image"));
						entry.set(Tag.Location,		xpp.getAttributeValue(null, "location"));
						entry.set(Tag.Schedule,		xpp.getAttributeValue(null, "schedule"));
						entry.set(Tag.Guest,		xpp.getAttributeValue(null, "guest"));
						entry.set(Tag.Website,		xpp.getAttributeValue(null, "website"));
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
						entry.set(Tag.Abstract,		xpp.nextText());
					else if ("content".equals(startTag))
						entry.set(Tag.Content,		xpp.nextText());
					else if ("note".equals(startTag))
						entry.set(Tag.Note,			xpp.nextText());
					else if ("reservation".equals(startTag)) {
						entry.set(Tag.ProgramURL,	xpp.getAttributeValue(null, "url"));
						entry.set(Tag.Reservation,	xpp.nextText());
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
				final String newFile = context.getString(R.string.path_local_data_new);
				final String oldFile = context.getString(R.string.path_local_data);
				context.getFileStreamPath(newFile).renameTo(context.getFileStreamPath(oldFile));
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
	private static void sendProgressMessage(Handler handler, int progress, int max) {
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
		assert !isParseFinished;

		final List<String> match = new ArrayList<String>();
		for (Entry entry : entryMap.values()) {
			if (entry.getId().equals(query)) {
				match.add(entry.getId());
				continue;
			}
			for (Tag key : searchByKeywordTags) {
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
		assert !isParseFinished;

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
		return String.format("AgoraData has %d (Entry) and %d (TimeFrame) data", entryMap.size(), timeFrameMap.size());
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
