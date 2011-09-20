package net.tailriver.agoraguide;

import java.net.HttpURLConnection;

import net.tailriver.agoraguide.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
		String id = intent.getStringExtra("id");
		AgoraData.Entry entry = AgoraData.getEntry(id);

		TextView titleText = (TextView) this.findViewById(R.id.actdetail_title);
		titleText.setText(entry.getTitle());
		titleText.setTextSize(21.0f);
		titleText.setTextColor(Color.WHITE);

		TextView exhibitorText = (TextView) this.findViewById(R.id.actdetail_exhibitor);
		exhibitorText.setText(entry.getExhibitor());
		exhibitorText.setTextColor(Color.GRAY);
		exhibitorText.setTextSize(14.0f);

		TextView abstractText = (TextView) this.findViewById(R.id.actdetail_abstract);
		abstractText.setText(entry.getAbstract());
		abstractText.setTextColor(Color.WHITE);
		abstractText.setTextSize(14.0f);


		ImageView thumbnail = (ImageView) this.findViewById(R.id.actdetail_thumbnail);
		if (entry.getImage() != null) {
			try {
				HttpURLConnection huc = ((HttpURLConnection) entry.getImage().openConnection());
				huc.setDoInput(true);
				huc.connect();
				thumbnail.setImageBitmap(BitmapFactory.decodeStream(huc.getInputStream()));
			}
			catch (Exception e) {
				Log.e("ADActivity", e.toString());
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
