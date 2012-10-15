package net.tailriver.agoraguide;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public abstract class AgoraActivity extends Activity {
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new InitializeTask(this).execute(savedInstanceState);
	}

	abstract public void onPreInitialize();

	abstract public void onPostInitialize(Bundle savedInstanceState);

	private final class InitializeTask extends AsyncTask<Bundle, Void, Bundle> {
		private AgoraActivity agora;

		InitializeTask(AgoraActivity agora) {
			this.agora = agora;
		}

		@Override
		protected void onPreExecute() {
			agora.onPreInitialize();
		}

		@Override
		protected Bundle doInBackground(Bundle... params) {
			AgoraInitializer.init(agora.getApplicationContext());
			return params[0];
		}

		@Override
		protected void onPostExecute(Bundle result) {
			agora.onPostInitialize(result);
		}
	}
}
