package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AgoraHttpClient {
	static final int BUFFER_SIZE = 4096;

	final URL url;
	HttpURLConnection http;

	static {
		// patch (before FROYO)
		System.setProperty("http.keepAlive", "false");		
	}

	public AgoraHttpClient(Context context, URL url) throws StandAloneException, IOException {
		if (url == null) {
			throw new IllegalArgumentException("url is null");
		}
		this.url = url;

		isConnected(context);
		http = (HttpURLConnection) url.openConnection();
	}

	public void download(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("file is null");
		}

		if (file.exists()) {
			http.setIfModifiedSince(file.lastModified());
		}
		http.setRequestProperty("Accept-Encoding", "gzip");
		http.setDoInput(true);

		try {
			http.connect();
			int code = http.getResponseCode();

			Log.i(getClass().getSimpleName(), "Downloading: " + url);
			Log.i(getClass().getSimpleName(), code + " " + http.getResponseMessage());

			if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
				return;
			}

			if (code != HttpURLConnection.HTTP_OK) {
				throw new IOException("fail to download");
			}

			String contentEncoding = http.getContentEncoding();
			final InputStream is = http.getInputStream();
			final BufferedInputStream bis;
			if (contentEncoding != null && contentEncoding.equals("gzip")) {
				Log.d(this.getClass().toString(), "gzip compressed");
				bis = new BufferedInputStream(new GZIPInputStream(is), BUFFER_SIZE);
			} else {
				Log.d(this.getClass().toString(), "not compressed");
				bis = new BufferedInputStream(is, BUFFER_SIZE);
			}

			final FileOutputStream fos = new FileOutputStream(file);
			final BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);

			byte[] buffer = new byte[BUFFER_SIZE];
			int totalRead = 0;
			while (true) {
				final int byteRead = bis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);
				totalRead += byteRead;
			}
			bis.close();
			bos.flush();
			bos.close();

			Log.i(getClass().getSimpleName(), "Downloaded " + String.valueOf(totalRead) + " bytes");
		} finally {
			cleanup();
		}
	}

	public void downloadForce(File file) throws IOException {
		if (file != null && file.exists()) {
			file.delete();
		}
		download(file);
	}

	public Bitmap getBitmap() throws IOException {
		http.setDoInput(true);
		try {
			http.connect();
			InputStream is = http.getInputStream();
			return BitmapFactory.decodeStream(is);
		} finally {
			cleanup();
		}
	}

	private void cleanup() {
		if (http != null) {
			http.disconnect();
		}
	}

	private void isConnected(Context context) throws StandAloneException {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			throw new StandAloneException();
		}
	}
}
