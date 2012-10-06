package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class HttpClient {
	private static final String thisClass = HttpClient.class.getSimpleName();
	static final int BUFFER_SIZE = 4096;

	private static Context context;

	URL url;
	HttpURLConnection http;

	static {
		// patch
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public HttpClient(URL url) throws StandAloneException, IOException {
		this.url = url;
		openConnection();
	}

	public HttpClient(String urlString) throws StandAloneException, IOException {
		try {
			url = new URL(urlString);
			openConnection();
		} catch (MalformedURLException e) {
			throw new IOException(e);
		}
	}

	public static void init(Context context) {
		HttpClient.context = context;
	}

	private void openConnection() throws StandAloneException, IOException {
		// connectivity check
		Object cm = context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = ((ConnectivityManager)cm).getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			throw new StandAloneException();
		}

		http = (HttpURLConnection) url.openConnection();
		http.setRequestProperty("User-Agent", "AgoraGuide/2012 (Android)");		
	}

	public void download(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("file is null");
		}

		http.setIfModifiedSince(file.lastModified());
		http.setRequestProperty("Accept-Encoding", "gzip");
		http.setDoInput(true);

		File tempFile = File.createTempFile("downloading", null, context.getFilesDir());
		try {
			http.connect();
			int code = http.getResponseCode();

			Log.i(thisClass, "Downloading: " + url);
			Log.i(thisClass, code + " " + http.getResponseMessage());

			if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
				file.setLastModified(System.currentTimeMillis());
				return;
			}

			if (code != HttpURLConnection.HTTP_OK) {
				throw new IOException("fail to download");
			}

			String contentEncoding = http.getContentEncoding();
			InputStream is = http.getInputStream();
			BufferedInputStream bis;
			if (contentEncoding != null && contentEncoding.equals("gzip")) {
				bis = new BufferedInputStream(new GZIPInputStream(is), BUFFER_SIZE);
			} else {
				bis = new BufferedInputStream(is, BUFFER_SIZE);
			}

			FileOutputStream fos = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);

			byte[] buffer = new byte[BUFFER_SIZE];
			int totalRead = 0;
			while (true) {
				int byteRead = bis.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				bos.write(buffer, 0, byteRead);
				totalRead += byteRead;
			}
			bis.close();
			bos.flush();
			bos.close();

			if (!tempFile.renameTo(file)) {
				throw new IOException("download successed but cannot write specific file");
			}
			Log.i(thisClass, "Downloaded " + String.valueOf(totalRead) + " bytes");
		} finally {
			if (tempFile.exists()) {
				tempFile.delete();
			}
			disconnect();
		}
	}

	public Bitmap getBitmap() throws IOException {
		http.setDoInput(true);
		try {
			http.connect();
			InputStream is = http.getInputStream();
			return BitmapFactory.decodeStream(is);
		} finally {
			disconnect();
		}
	}

	private final void disconnect() {
		if (http != null) {
			http.disconnect();
		}
	}
}
