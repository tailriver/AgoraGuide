package net.tailriver.agoraguide;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;

public class Favorite {
	private static final String PREFERENCE_NAME = "agora2012";

	public static final boolean isFavorite(EntrySummary summary) {
		SharedPreferences pref = getSharedPreferences();
		return pref.contains(summary.getId());
	}

	public static void setFavorite(EntrySummary summary, boolean newState) {
		SharedPreferences pref = getSharedPreferences();
		if (newState) {
			pref.edit().putBoolean(summary.getId(), true).commit();
		} else {
			pref.edit().remove(summary.getId()).commit();
		}
	}

	public static Collection<EntrySummary> values() {
		Collection<EntrySummary> collection = new HashSet<EntrySummary>();
		for (String key : getSharedPreferences().getAll().keySet()) {
			collection.add(EntrySummary.get(key));
		}
		return collection;
	}

	public static void clear() {
		SharedPreferences pref = getSharedPreferences();
		SharedPreferences.Editor editor = pref.edit();
		for (String key : pref.getAll().keySet()) {
			editor.remove(key);
		}
		editor.commit();
	}

	private static final SharedPreferences getSharedPreferences() {
		Context context = AgoraInitializer.getApplicationContext();
		return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
	}
}
