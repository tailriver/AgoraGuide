package net.tailriver.agoraguide;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import net.tailriver.agoraguide.AgoraEntry.*;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

public class AgoraData {
	private static final Tag[] searchByKeywordTags = {
		Tag.TITLE, Tag.SPONSOR, Tag.CO_SPONSOR, Tag.ABSTRACT, Tag.CONTENT, Tag.GUEST, Tag.NOTE
	};

	private static Context context;
	private static Map<String, AgoraEntry> agoraEntry;
	private static List<TimeFrame> timeFrame;
	private static List<String> favorites;

	public static void setApplicationContext(Context context) {
		if (AgoraData.context != null)
			return;

		AgoraData.context = context.getApplicationContext();

		final Resources res = AgoraData.context.getResources();
		AgoraEntry.setResources(res);
		Day.setResources(res);

		final SharedPreferences pref = getSharedPreferences();
		agoraEntry	= new LinkedHashMap<String, AgoraEntry>(pref.getInt("initialCapacityOfEntry",	50));
		timeFrame	= new ArrayList<TimeFrame>(  pref.getInt("initialCapacityOfTimeFrame",		50));
		favorites	= new ArrayList<String>(Arrays.asList(pref.getString("favorites", "").split(";")));
	}

	private static SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(context.getString(R.string.path_pref), Context.MODE_PRIVATE);
	}

	private static boolean isParseFinished() {
		return agoraEntry.size() > 0;
	}

	public static void updateData(Handler handler) throws IOException {
		String urlString = context.getString(R.string.path_data_xml_gz);
		try {
			URL  url  = new URL(urlString);
			File file = context.getFileStreamPath(context.getString(R.string.path_local_data));
			AgoraHttpClient ahc = new AgoraHttpClient(context, url);
			ahc.download(file);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed url: " + urlString);
		} catch (StandAloneException e) {
			// noop
		}
	}

	/** @throws ParseDataAbortException */
	public static void parseData() throws ParseDataAbortException {
		clear();

		final SharedPreferences.Editor ee = getSharedPreferences().edit();
		try {
			final XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(context.openFileInput(context.getString(R.string.path_local_data)), null);

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
						entry.set(Tag.TITLE,		xpp.getAttributeValue(null, "title"));
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
			Collections.sort(timeFrame);

			ee.putInt("initialCapacityOfEntryMap", (int) (agoraEntry.size() * 1.5));
			ee.putInt("initialCapacityOfTimeFrameMap", (int) (timeFrame.size() * 1.5));
			ee.commit();
		}
		catch (ParseDataAbortException e) {
			clear();
			throw e;
		}
		catch (Exception e) {
			clear();
			throw new ParseDataAbortException("Parse error: " + e);
		}
	}

	public static void removeData() {
		context.deleteFile(context.getString(R.string.path_local_data));
		final SharedPreferences.Editor ee = getSharedPreferences().edit();
		ee.remove("lovalVersionNew");
		ee.remove("localVersion");
		ee.commit();
	}

	public static void clear() {
		agoraEntry.clear();
		timeFrame.clear();
	}

	/**
	 * @param handler Handler.
	 * @param progress int. argument of setProgress()
	 * @param max int. argument of setMax()
	 */
	@SuppressWarnings("unused")
	private static void sendProgressMessage(Handler handler, int progress, int max) {
		final Message message = new Message();
		message.what = R.id.main_progress;
		message.arg1 = progress;
		message.arg2 = max;
		handler.sendMessage(message);
	}

	/**
	 *  @param id entry id
	 */
	public static AgoraEntry getEntry(String id) {
		assert !isParseFinished();
		assert !agoraEntry.containsKey(id) : "Request id does not exist";
		return agoraEntry.get(id);
	}

	/**
	 * @return list of entry {@code id}(s)
	 * @throws AssertionError called before finishing parse
	 */
	public static List<String> getAllEntryId() {
		assert !isParseFinished();
		return new ArrayList<String>(agoraEntry.keySet());
	}

	public static List<TimeFrame> getAllTimeFrame() {
		assert !isParseFinished();
		return timeFrame;
	}

	/**
	 * @return list of favorite entry {@code id}(s)
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
	public static List<String> getEntryByKeyword(String query, Map<Category, Boolean> filter) {
		assert !isParseFinished();

		final List<String> match = new ArrayList<String>();

		// id match
		if (agoraEntry.containsKey(query))
			match.add(query);

		// keyword match
		for (Map.Entry<String, AgoraEntry> e : agoraEntry.entrySet()) {
			final String id = e.getKey();
			final AgoraEntry entry = e.getValue();

//			if (!filter.get(entry.getCategory()))
//				continue;

			if (query.length() == 0) {
				match.add(id);
				continue;
			}

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

	public static void setFavorite(String id, boolean state) {
		if (state && !favorites.contains(id)) {
			favorites.add(id);
			updateFavoriteList();
		}
		else if (!state && favorites.remove(id)) {
			updateFavoriteList();
		}
	}

	public static void clearFavorite() {
		favorites.clear();
		updateFavoriteList();
	}

	private static void updateFavoriteList() {
		final StringBuilder sb = new StringBuilder();
		for (String favoriteId : favorites)
			sb.append(favoriteId).append(';');

		final SharedPreferences.Editor ee = getSharedPreferences().edit();
		ee.putString("favorites", sb.toString());
		ee.commit();
	}
}
