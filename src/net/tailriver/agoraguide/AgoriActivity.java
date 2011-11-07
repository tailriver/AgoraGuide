package net.tailriver.agoraguide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

public class AgoriActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agori);

		AgoraData.setApplicationContext(getApplicationContext());

		final ListView view = (ListView) findViewById(R.id.agori_list);
		view.setAdapter(new AgoriAdapter(AgoriActivity.this, Agori.get()));
		view.setOnItemClickListener(theAdapter());
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		theAdapter().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private AgoriAdapter theAdapter() {
		return (AgoriAdapter) ((ListView) findViewById(R.id.agori_list)).getAdapter();
	}
}
