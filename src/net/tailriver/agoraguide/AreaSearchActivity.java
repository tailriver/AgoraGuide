package net.tailriver.agoraguide;

import java.util.Collections;

public class AreaSearchActivity extends SearchActivity {
	public static final String INTENT_AREA_ID = "net.tailriver.agorguide.area_id";

	@Override
	public void onPreInitialize() {
		setContentView(R.layout.search_result);
		findViewById(android.R.id.content).setPadding(5, 5, 5, 5);
	}

	@Override
	public void onPostInitialize() {
		Area area = Area.get(getIntent().getStringExtra(INTENT_AREA_ID));

		searchAdapter.addAll(new EntryFilter().addAllEntry().applyFilter(Collections.singleton(area)).getResult());
	}
}
