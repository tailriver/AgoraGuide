package net.tailriver.agoraguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

public class Agori {
	private static Resources res;

	public static void setResources(Resources res) {
		Agori.res = res;
	}

	public static List<Agori> get() {
		final List<Agori> list = new ArrayList<Agori>();
		if (!AgoraData.isConnected())
			return list;

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					new URL(res.getString(R.string.path_agori_get)).openStream()));

			final StringBuilder sb = new StringBuilder();
			while (true) {
				String s = br.readLine();
				if (s == null)
					break;
				sb.append(s);
			}
			br.close();

			final JSONArray json = new JSONArray(sb.toString());
			for (int i = 0; i < json.length(); i++) {
				try {
					final Agori agori = new Agori(json.getJSONObject(i));
					list.add(agori);
				}
				catch (JSONException e) { }
			}
		}
		catch (IOException e) { }
		catch (JSONException e) { }
		return list;
	}

	private final String id;
	private final String name;
	private final String comment;

	Agori(JSONObject json) throws JSONException {
		id		= json.getString("program_id");
		name	= json.getString("name");
		comment	= json.getString("comment");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/*
	public SpannableString getNameEnhanced() {
		return name;
	}
	 */

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return String.format("%s: %s (%s)", id, comment, name.length() > 0 ? name : "?");
	}
}
