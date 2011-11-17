package net.tailriver.agoraguide;

import java.io.ByteArrayOutputStream;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AgoruActivity extends Activity implements android.view.View.OnClickListener, OnCheckedChangeListener, android.content.DialogInterface.OnClickListener {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agoru);

		AgoraData.setApplicationContext(getApplicationContext());
		final String entryId = getIntent().getStringExtra("entryId");

		final TextView entryIdView = (TextView) findViewById(R.id.agoru_id);
		entryIdView.setText(entryId);

		final TextView titleView = (TextView) findViewById(R.id.agoru_title);
		titleView.setText(AgoraData.getEntry(entryId).getLocaleTitle());

		final CheckBox tweet = (CheckBox) findViewById(R.id.agoru_tweet);
		tweet.setOnCheckedChangeListener(this);

		final Button submit = (Button) findViewById(R.id.agoru_submit);
		submit.setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked == false)
			return;

		new AlertDialog.Builder(AgoruActivity.this)
		.setTitle("Twitterへの投稿")
		.setMessage("投稿内容がTwitterアカウント @agorink に転載されます。よろしいですか？")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.ok, null)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final CheckBox tweet = (CheckBox) findViewById(R.id.agoru_tweet);
				tweet.setChecked(false);
			}
		})
		.create()
		.show();
	}

	@Override
	public void onClick(View v) {
		new AlertDialog.Builder(AgoruActivity.this)
		.setTitle("あごりんく！への投稿")
		.setMessage("投稿内容が世界中に公開されます。よろしいですか？")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.ok, this)
		.setNegativeButton(android.R.string.cancel, null)
		.create()
		.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		final String program_id = getIntent().getStringExtra("entryId");
		final String name = ((EditText) findViewById(R.id.agoru_name)).getText().toString();
		final String comment = ((EditText) findViewById(R.id.agoru_comment)).getText().toString();
		final boolean isTweet = ((CheckBox) findViewById(R.id.agoru_tweet)).isChecked();

		// post
		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair("program_id", program_id));
		pair.add(new BasicNameValuePair("name", name));
		pair.add(new BasicNameValuePair("comment", comment));
		pair.add(new BasicNameValuePair("tweet", isTweet ? "1" : "0"));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.tailriver.net/scienceagora/2011/agori/post");
		try {
			httppost.setEntity(new UrlEncodedFormEntity(pair, "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			response.getEntity().writeTo(byteArrayOutputStream);

			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final String jsonText = byteArrayOutputStream.toString();
				try {
					final JSONArray json = new JSONArray(jsonText);
					@SuppressWarnings("unused")
					final Agori agori = new Agori(json.getJSONObject(0));

					((EditText) findViewById(R.id.agoru_comment)).setText("");
					((CheckBox) findViewById(R.id.agoru_tweet)).setChecked(false);

					finish();
				}
				catch (JSONException e) {
					try {
						final JSONObject json = new JSONObject(jsonText);
						Toast.makeText(AgoruActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
					}
					catch (JSONException ee) { ee.toString(); }
				}				
			}
			else {
				Toast.makeText(this, response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
