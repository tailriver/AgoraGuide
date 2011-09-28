package net.tailriver.agoraguide;

import java.net.HttpURLConnection;
import java.net.URL;

import net.tailriver.agoraguide.AgoraData.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ActDetailActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actdetail);

		Intent intent = getIntent();
		Entry entry = AgoraData.getEntry(intent.getStringExtra("id"));

		((TextView) this.findViewById(R.id.actdetail_title)).setText(entry.getLocaleString(EntryKey.TitleJa));
		((TextView) this.findViewById(R.id.actdetail_exhibitor)).setText(entry.getLocaleString(EntryKey.ExhibitorJa));
		((TextView) this.findViewById(R.id.actdetail_abstract)).setText(entry.getString(EntryKey.Abstract));
		((TextView) this.findViewById(R.id.actdetail_content)).setText(entry.getString(EntryKey.Content).replace("&x0A;", "\n"));

		ImageView thumbnail = (ImageView) this.findViewById(R.id.actdetail_thumbnail);
		URL imageURL = (URL) entry.get(EntryKey.Image);
		if (imageURL != null) {
			try {
				HttpURLConnection huc = ((HttpURLConnection) imageURL.openConnection());
				huc.setDoInput(true);
				huc.connect();
				thumbnail.setImageBitmap(BitmapFactory.decodeStream(huc.getInputStream()));
			}
			catch (Exception e) {
				Log.e("ADActivity", e.toString());
				thumbnail.setVisibility(View.GONE);
			}
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}


		Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
