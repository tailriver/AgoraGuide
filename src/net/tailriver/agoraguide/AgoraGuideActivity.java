package net.tailriver.agoraguide;

import java.io.IOException;

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

		findViewById(R.id.main_progressLayout).setVisibility(View.INVISIBLE);

		AgoraData.setApplicationContext(getApplicationContext());
		try {
			AgoraData.parseData();
		}
		catch (ParseDataAbortException e) { }
		new Thread(AgoraGuideActivity.this).start();
	}

	/** You must not try to change UI here, go to {@link Handler}. */
	public synchronized void run() {
		final Message message = new Message();
		message.what = R.layout.main;
		try {
			AgoraData.updateData(handler);
			AgoraData.parseData();
		}
		catch (IOException e) {
			message.arg1 = R.string.error_fail_update;
			message.arg2 = Toast.LENGTH_LONG;
			handler.sendMessage(message);
			LogError(e.toString());
		}
		catch (ParseDataAbortException e) {
			message.arg1 = R.string.error_fail_parse;
			message.arg2 = Toast.LENGTH_LONG;
			handler.sendMessage(message);
			LogError(e.toString());
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
					findViewById(R.id.main_progressLayout)
					.setVisibility(msg.arg2 > 0 ? View.VISIBLE : View.INVISIBLE);
				}
				break;

			case R.layout.main:
				Toast.makeText(AgoraGuideActivity.this, msg.arg1, msg.arg2).show();
				break;

			default:
				LogError("unknown message received: " + msg.toString());
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Class<?> nextActivity = null;
		switch (item.getItemId()) {
		case R.id.menu_sbk:
			nextActivity = SearchByKeywordActivity.class;
			break;

		case R.id.menu_sbs:
			nextActivity = SearchByScheduleActivity.class;
			break;

		case R.id.menu_favorites:
			nextActivity = FavoritesActivity.class;
			break;

		case R.id.menu_credits:
/*
			AgoraData.removeData();
			Toast.makeText(AgoraGuideActivity.this, "Data removed", Toast.LENGTH_SHORT).show();
			new Thread(AgoraGuideActivity.this).start();
			return true;
 */
			nextActivity = CreditsActivity.class;
			break;

		default:
			break;
		}

		if (nextActivity != null) {
			startActivity(new Intent(AgoraGuideActivity.this, nextActivity));
			return true;
		}
		else
			return false;
	}

	private void LogError(String s) {
		Log.e(getApplicationContext().getString(R.string.app_name), s);
	}
}
