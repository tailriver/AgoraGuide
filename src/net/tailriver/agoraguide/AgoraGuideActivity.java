package net.tailriver.agoraguide;

import net.tailriver.agoraguide.AgoraData.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AgoraGuideActivity extends Activity implements Runnable {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.main_progress).setVisibility(View.GONE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		new Thread(AgoraGuideActivity.this).start();
	}

	/** You must not try to change UI here, go to {@link Handler}.  */
	@Override
	public synchronized void run() {
		final Message message = new Message();
		message.what = R.layout.main;
		try {
			final AgoraData ad = new AgoraData(getApplicationContext());
			ad.parseData();
	
			boolean isUpdated = false;
			try {
				isUpdated = ad.updateData(true, handler);
			}
			catch (UpdateDataAbortException e) {
				isUpdated = ad.updateData(false, handler);
			}
			if (isUpdated)
				ad.parseData();
		}
		catch (UpdateDataAbortException e) {
			message.arg1 = R.string.error_fail_update;
			message.arg2 = Toast.LENGTH_LONG;
			handler.sendMessage(message);
			Log.e("AgoraGuide", e.toString());
		}
		catch (ParseDataAbortException e) {
			message.arg1 = R.string.error_fail_parse;
			message.arg2 = Toast.LENGTH_LONG;
			handler.sendMessage(message);
			Log.e("AgoraGuide", e.toString());
		}
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.main_progress:
				final ProgressBar pb = (ProgressBar) findViewById(msg.what);
				pb.setProgress(msg.arg1);

				if (msg.arg1 == 0) {
					pb.setMax(msg.arg2);
					pb.setVisibility(msg.arg2 > 0 ? View.VISIBLE : View.GONE);
					findViewById(R.id.main_text).invalidate();
				}
				break;

			case R.layout.main:
				Toast.makeText(AgoraGuideActivity.this, msg.arg1, msg.arg2).show();
				break;

			default:
				Log.w("AgoraGuide", "unknown message received: " + msg.toString());
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);

		menu.findItem(R.id.menu_sbk).setIntent(new Intent(AgoraGuideActivity.this, SearchByKeywordActivity.class));
		menu.findItem(R.id.menu_sbs).setEnabled(false);
		menu.findItem(R.id.menu_sbm).setEnabled(false);
		menu.findItem(R.id.menu_favorite).setIntent(new Intent(AgoraGuideActivity.this, FavoritesActivity.class));
		//menu.findItem(R.id.menu_agora).setEnabled(false);
		menu.findItem(R.id.menu_preference).setEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getIntent() != null) {
			startActivity(item.getIntent());
			return true;
		}
		if (item.getItemId() == R.id.menu_preference) {
			new AgoraData(getApplicationContext()).removeData();
			Toast.makeText(AgoraGuideActivity.this, "Data removed", Toast.LENGTH_SHORT).show();
			new Thread(AgoraGuideActivity.this).start();
		}
		return false;
	}
}
