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

public class AgoraGuideActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		(new Thread(runnable)).start();
	}

	/** You must not try to change UI here, go to {@link Handler}.  */
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				final AgoraData ad = new AgoraData(getApplicationContext());
				ad.updateData(false, handler);
				ad.parseData(handler);
			}
			catch (UpdateDataAbortException e) {
				Log.e("AGActivity", e.toString());
			}
			catch (ParseDataAbortException e) {
				Log.e("AGActivity", e.toString());
			}

			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putBoolean("quit", true);
			message.setData(bundle);
			handler.sendMessage(message);
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final Bundle b = msg.getData();
			final ProgressBar pb = (ProgressBar) findViewById(R.id.main_progress);

			if (b.containsKey("progress"))
				pb.setProgress(b.getInt("progress", 0));

			else if (b.containsKey("max"))
				pb.setMax(b.getInt("max"));

			else if (b.containsKey("parse"))
				Toast.makeText(AgoraGuideActivity.this, b.getBoolean("parse") ? "Parse finished" : "Parse failed", Toast.LENGTH_LONG).show();

			else if (b.containsKey("quit")) {
				pb.setVisibility(View.GONE);
				findViewById(R.id.main_text).invalidate();
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = new MenuInflater(AgoraGuideActivity.this);
		mi.inflate(R.menu.main, menu);

		menu.findItem(R.id.menu_sbk).setIntent(new Intent(AgoraGuideActivity.this, SearchByKeywordActivity.class));
		menu.findItem(R.id.menu_sbs).setEnabled(false);
		menu.findItem(R.id.menu_sbm).setEnabled(false);
		menu.findItem(R.id.menu_favorite).setIntent(new Intent(AgoraGuideActivity.this, FavoritesActivity.class));
		//menu.findItem(R.id.menu_agora).setEnabled(false);
		menu.findItem(R.id.menu_configuration).setEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getIntent() != null) {
			startActivity(item.getIntent());
			return true;
		}
		return false;
	}
}
