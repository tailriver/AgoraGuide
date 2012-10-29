package net.tailriver.agoraguide;

import net.tailriver.agoraguide.SearchActivity.SearchType;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AreaSearchIndexActivity extends AgoraActivity
implements OnItemClickListener
{
	@Override
	public void onPreInitialize() {
		setContentView(R.layout.area_search_index);
		setTitle(R.string.searchArea);
	}

	@Override
	public void onPostInitialize(Bundle savedInstanceState) {
		boolean isLandscape = isLandscape();
		ArrayAdapter<String> arrayAdapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (Area area : Area.values()) {
			arrayAdapter.add(isLandscape ? area.toString() : area.getShortName());
		}

		ListView view = (ListView) findViewById(R.id.area_search_index);
		view.setAdapter(arrayAdapter);
		view.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
		intent.putExtra(IntentExtra.SEARCH_TYPE, SearchType.Area);
		intent.putExtra(IntentExtra.AREA_ID, Area.values().get(position).getId());
		startActivity(intent);
	}
}
