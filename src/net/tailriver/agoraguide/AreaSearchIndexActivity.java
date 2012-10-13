package net.tailriver.agoraguide;

import java.util.List;

import net.tailriver.agoraguide.SearchActivity.SearchType;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AreaSearchIndexActivity extends AgoraActivity
implements OnItemClickListener
{
	private List<Area> areas;
	private ListView view;

	@Override
	public void onPreInitialize() {
		setContentView(R.layout.area_search_index);
		setTitle(R.string.searchByMap);
	}

	@Override
	public void onPostInitialize() {
		areas = Area.values();
		view = (ListView) findViewById(R.id.area_search_index);
		ArrayAdapter<String> arrayAdapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (Area area : areas) {
			arrayAdapter.add(area.getShortName());
		}

		view.setAdapter(arrayAdapter);
		view.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
		intent.putExtra(SearchActivity.INTENT_SEARCH_TYPE, SearchType.Area);
		intent.putExtra(SearchActivity.INTENT_AREA_ID, areas.get(position).getId());
		startActivity(intent);
	}
}
