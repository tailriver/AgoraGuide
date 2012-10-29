package net.tailriver.agoraguide;

import android.os.Bundle;

public class CreditsActivity extends AgoraActivity {
	@Override
	public void onPreInitialize() {
		setContentView(R.layout.credits);		
	}

	// FIXME updates credits
	@Override
	public void onPostInitialize(Bundle savedInstanceState) {
	}
}
