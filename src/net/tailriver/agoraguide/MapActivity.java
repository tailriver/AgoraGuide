package net.tailriver.agoraguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class MapActivity extends AgoraActivity {
	private static final String BUNDLE_HIYOKO = "hiyokoResource";
	private static final int[] HIYOKO_ICONS = {
		R.drawable.here_blue,
		R.drawable.here_green,
		R.drawable.here_yellow
	};

	private int hiyokoResource;

	@Override
	public void onPreInitialize() {
	}

	@Override
	public void onPostInitialize(Bundle savedInstanceState) {
		EntrySummary summary = EntrySummary.get(getIntent().getStringExtra(IntentExtra.ENTRY_ID));
		EntryDetail detail = new EntryDetail(summary);
		Location location = detail.getLocation();
		Area area = location.getArea();

		Bitmap areaBitmap = area.getBitmap();
		if (areaBitmap == null) {
			Log.w(getClass().getSimpleName(), "No image");
			finish();
			return;
		}
		int w = areaBitmap.getWidth();
		int h = areaBitmap.getHeight();
		PointF p = new PointF(location.getX() * w, location.getY() * h);

		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_HIYOKO)) {
			hiyokoResource = savedInstanceState.getInt(BUNDLE_HIYOKO);
		} else {
			hiyokoResource = HIYOKO_ICONS[(int)(Math.random() * HIYOKO_ICONS.length)];
		}

		if (!Float.isNaN(p.x) && !Float.isNaN(p.y)) {
			Bitmap hiyoko = BitmapFactory.decodeResource(getResources(), hiyokoResource);
			p.offset(- hiyoko.getWidth(), - 0.5f * hiyoko.getHeight());
			Canvas mapCanvas = new Canvas(areaBitmap);
			mapCanvas.drawBitmap(hiyoko, p.x, p.y, null);
		}

		BitmapDrawable mapDrawable = new BitmapDrawable(getResources(), areaBitmap);

		int padding = getResources().getDimensionPixelSize(R.dimen.contentPadding);
		ImageView map = new ImageView(this);
		map.setPadding(padding, padding, padding, padding);
		map.setScaleType(ScaleType.CENTER_INSIDE);
		map.setImageDrawable(mapDrawable);

		setTitle(summary.toString());
		setContentView(map);

		Toast.makeText(this, area.toString(), Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_HIYOKO, hiyokoResource);
	}
}
