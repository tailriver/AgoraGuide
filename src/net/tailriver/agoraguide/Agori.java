package net.tailriver.agoraguide;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

	public static List<Agori> get(String entryId) {
		final List<Agori> list = new ArrayList<Agori>();
		if (!AgoraData.isConnected())
			return list;

		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair("program_id", entryId));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.tailriver.net/scienceagora/2011/agori/get");
		try {
			httppost.setEntity(new UrlEncodedFormEntity(pair, "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			response.getEntity().writeTo(byteArrayOutputStream);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new Exception();

			final String jsonText = byteArrayOutputStream.toString();
			try {
				final JSONArray json = new JSONArray(jsonText);
				for (int i = 0; i < json.length(); i++) {
					try {
						final Agori agori = new Agori(json.getJSONObject(i));
						list.add(agori);
					}
					catch (JSONException e) { }
				}
			}
			catch (JSONException e) { }				
		}
		catch (Exception e) { }

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
