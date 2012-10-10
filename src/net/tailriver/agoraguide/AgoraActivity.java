package net.tailriver.agoraguide;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public abstract class AgoraActivity extends Activity {
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new InitializeTask(this).execute();
	}

	abstract public void onPreInitialize();

	abstract public void onPostInitialize();

	private final class InitializeTask extends AsyncTask<Void, Void, Void> {
		private AgoraActivity agora;

		InitializeTask(AgoraActivity agora) {
			this.agora = agora;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			agora.onPreInitialize();
		}

		@Override
		protected Void doInBackground(Void... params) {
			AgoraInitializer.init(agora.getApplicationContext());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			agora.onPostInitialize();
		}
	}
}
