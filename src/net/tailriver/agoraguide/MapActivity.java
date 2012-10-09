package net.tailriver.agoraguide;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AgoraGuideActivity.initDatabase(getApplicationContext());

		EntrySummary summary = EntrySummary.get(getIntent().getStringExtra(EntryDetailActivity.INTENT_ENTRY));
		EntryDetail detail = new EntryDetail(summary);
		Location location = detail.getLocation();
		Area area = location.getArea();

		Bitmap areaBitmap = area.getBitmap();
		if (areaBitmap == null) {
			Log.w(getClass().getSimpleName(), "No image");
			finish();
		}
		int w = areaBitmap.getWidth();
		int h = areaBitmap.getHeight();
		float cx = location.getX() * w;
		float cy = location.getY() * h;
		float radius = 0.03f * h;
		Paint paint = new Paint();
		paint.setARGB(32, 255, 0, 0);
		Canvas mapCanvas = new Canvas(areaBitmap);
		mapCanvas.drawCircle(cx, cy, radius, paint);

		BitmapDrawable mapDrawable = new BitmapDrawable(getResources(), areaBitmap);
		mapDrawable.setBounds(0, 0, w, h);

		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		TextView map = new TextView(this);
		map.setGravity(Gravity.CENTER);
		map.setText(area.toString());
		map.setCompoundDrawables(null, null, null, mapDrawable);

		root.addView(map);
		setTitle(summary.toString());
		setContentView(root);
	}
}
