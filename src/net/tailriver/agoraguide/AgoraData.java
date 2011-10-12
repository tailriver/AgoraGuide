package net.tailriver.agoraguide;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import net.tailriver.agoraguide.AgoraEntry.*;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

public class AgoraData {
	private static final Tag[] searchByKeywordTags = {
		Tag.TITLE_JA, Tag.TITLE_EN, Tag.SPONSOR, Tag.CO_SPONSOR, Tag.ABSTRACT, Tag.CONTENT, Tag.GUEST, Tag.NOTE
	};

	private static Map<String, AgoraEntry> agoraEntry;
	private static List<TimeFrame> timeFrame;
	private static List<String> favorites;
	private static boolean isParseFinished = false;

	private final Context context;
	private final SharedPreferences pref;

	/** @param context	It should be {@code getApplicationContext()} */
	public AgoraData(Context context) {
		this.context = context.getApplicationContext();
		this.pref	 = this.context.getSharedPreferences("pref", Context.MODE_PRIVATE);

		if (agoraEntry == null) {
			agoraEntry	= new LinkedHashMap<String, AgoraEntry>(pref.getInt("initialCapacityOfEntry",	50));
			timeFrame	= new ArrayList<TimeFrame>(  pref.getInt("initialCapacityOfTimeFrame",		50));
			favorites	= new ArrayList<String>(Arrays.asList(pref.getString("favorites", "").split(";")));
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
		final NetworkInfo ni = cm.getActiveNetworkInfo();

		return ni.isAvailable() && ni.isConnected();
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

			AgoraEntry entry = null;
			String entryId	 = null;
			for (int e = xpp.getEventType(); e != XmlPullParser.END_DOCUMENT; e = xpp.next()) {
				switch (e) {
				case XmlPullParser.START_TAG:
					final String startTag = xpp.getName();
					if ("entry".equals(startTag)) {
						entryId					= xpp.getAttributeValue(null, "id");
						final String category	= xpp.getAttributeValue(null, "category");
						final String target		= xpp.getAttributeValue(null, "target");
						final String schedule	= xpp.getAttributeValue(null, "schedule");

						if (entryId == null || category == null)
							throw new ParseDataAbortException("Parse error: it does not have required content");

						entry = new AgoraEntry(category, target, schedule);
						entry.set(Tag.TITLE_JA,		xpp.getAttributeValue(null, "title_ja"));
						entry.set(Tag.TITLE_EN,		xpp.getAttributeValue(null, "title_en"));
						entry.set(Tag.SPONSOR,		xpp.getAttributeValue(null, "sponsor"));
						entry.set(Tag.CO_SPONSOR,	xpp.getAttributeValue(null, "cosponsor"));
						entry.set(Tag.IMAGE,		xpp.getAttributeValue(null, "image"));
						entry.set(Tag.LOCATION,		xpp.getAttributeValue(null, "location"));
						entry.set(Tag.GUEST,		xpp.getAttributeValue(null, "guest"));
						entry.set(Tag.WEBSITE,		xpp.getAttributeValue(null, "website"));
						agoraEntry.put(entryId, entry);
						break;
					}
					if (entry == null)
						break;

					if ("timeframe".equals(startTag)) {
						final String day	= xpp.getAttributeValue(null, "day");
						final int start		= Integer.parseInt(xpp.getAttributeValue(null, "start"));
						final int end		= Integer.parseInt(xpp.getAttributeValue(null, "end"));
						timeFrame.add(new TimeFrame(entryId, day, start, end));
						break;
					}

					if ("abstract".equals(startTag))
						entry.set(Tag.ABSTRACT,		xpp.nextText());
					else if ("content".equals(startTag))
						entry.set(Tag.CONTENT,		xpp.nextText());
					else if ("note".equals(startTag))
						entry.set(Tag.NOTE,			xpp.nextText());
					else if ("reservation".equals(startTag)) {
						entry.set(Tag.RESERVE_ADDRESS,	xpp.getAttributeValue(null, "url"));
						entry.set(Tag.RESERVATION,		xpp.nextText());
					}
					else
						throw new ParseDataAbortException("Parse error: unknown tag found");
					break;

				case XmlPullParser.END_TAG:
					final String closeTag = xpp.getName();
					if ("entry".equals(closeTag)) {
						entry	= null;
						entryId	= null;
					}
					break;
				}
			}
			isParseFinished = true;

			Collections.sort(timeFrame);

			ee.putInt("initialCapacityOfEntryMap", (int) (agoraEntry.size() * 1.5));
			ee.putInt("initialCapacityOfTimeFrameMap", (int) (timeFrame.size() * 1.5));

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
		agoraEntry.clear();
		timeFrame.clear();
	}

	/**
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

	protected Context getContext() {
		return context;
	}

	/**
	 *  @param id entry id
	 *  @throws IllegalArgumentException when {@code id} is invalid
	 *  @throws IllegalStateException called before finishing parse
	 */
	public static AgoraEntry getEntry(String id) {
		if (!isParseFinished)
			throw new IllegalStateException();
		if (agoraEntry.containsKey(id))
			return agoraEntry.get(id);
		throw new IllegalArgumentException("Requiest id does not exist");
	}

	/**
	 * @return list of entry {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getAllEntryId() {
		assert !isParseFinished;
		return new ArrayList<String>(agoraEntry.keySet());
	}

	public static List<TimeFrame> getAllTimeFrame() {
		assert !isParseFinished;
		return timeFrame;
	}

	/**
	 * @return list of favorite entry {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getFavoriteEntryId() {
		// normalize
		favorites.remove("");
		Collections.sort(favorites);

		return favorites;
	}

	/**
	 * @param query
	 * @return search result, list of {@code id}(s)
	 * @throws IllegalStateException called before finishing parse
	 */
	public static List<String> getEntryByKeyword(String query) {
		assert !isParseFinished;

		final List<String> match = new ArrayList<String>();

		// id match
		if (agoraEntry.containsKey(query))
			match.add(query);

		// keyword match
		for (Map.Entry<String, AgoraEntry> e : agoraEntry.entrySet()) {
			final String id = e.getKey();
			final AgoraEntry entry = e.getValue();
			for (Tag key : searchByKeywordTags) {
				final String s = entry.getString(key);
				if (s != null && s.contains(query)) {
					match.add(id);
					break;
				}
			}
		}
		return match;
	}

	public static boolean isFavorite(String id) {
		return favorites.contains(id);
	}

	public void setFavorite(String id, boolean state) {
		if (state && !favorites.contains(id)) {
			favorites.add(id);
			updateFavoriteList();
		}
		else if (!state && favorites.remove(id)) {
			updateFavoriteList();
		}
	}

	public void clearFavorite() {
		favorites.clear();
		updateFavoriteList();
	}

	private void updateFavoriteList() {
		final StringBuilder sb = new StringBuilder();
		for (String favoriteId : favorites)
			sb.append(favoriteId).append(';');

		final SharedPreferences.Editor ee = pref.edit();
		ee.putString("favorites", sb.toString());
		ee.commit();
	}

	@Override
	public String toString() {
		return String.format("AgoraData has %d (Entry) and %d (TimeFrame) data", agoraEntry.size(), timeFrame.size());
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
