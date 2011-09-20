package net.tailriver.agoraguide.android;

import net.tailriver.agoraguide.android.AgoraData;
import net.tailriver.agoraguide.android.AgoraData.XMLParserAbortException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class AgoraGuideActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		AgoraData ad = new AgoraData(this.getApplicationContext());
		boolean isAvailableLatestVersion = ad.XMLUpdateNotifier();
		Log.d("AGActivity", "isAvailableLatestVersion: " + isAvailableLatestVersion);
		if (isAvailableLatestVersion) {
			ad.XMLUpdater();
		}
		try {
			ad.XMLParser();
		}
		catch (XMLParserAbortException e) {
			Log.e("AGActivity", e.toString());

			SharedPreferences pref = this.getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
			SharedPreferences.Editor ee = pref.edit();
			ee.putInt("localVersion", 0);
			ee.commit();
			Log.w("AGActivity", "localVersion reseted");
		}
		ListView actList = (ListView) this.findViewById(R.id.main_actlist);

		ActArrayAdapter adapter = new ActArrayAdapter(this, R.layout.actlist_item, AgoraData.getEntryIdList());
		actList.setAdapter(adapter);

		actList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				Intent intent = new Intent(AgoraGuideActivity.this, ActDetailActivity.class);
				intent.putExtra("id", (String) listView.getItemAtPosition(position));
				try {
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(AgoraGuideActivity.this, "Fail to move to detail view" + e.toString(), Toast.LENGTH_LONG).show();
					Log.w("AGActivity", e.toString());
				}
			}
		});

/*
		actList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				AgoraAct act = (AgoraAct) listView.getItemAtPosition(position);
				Toast.makeText(AgoraGuideActivity.this, act.getId(), Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
*/
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("MainActivity", "onPause() called");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i("MainActivity", "onResume() called");
		//		ap.parse();
	}
}
