package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class Downloader {
	private static final int BUFFER_SIZE = 4096;
	private static final int HTTP_TIME_OUT = 20000; // msec

	private File localDirectory;

	static {
		// patch
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public Downloader() throws StandAloneException {
		Context context = AgoraInitializer.getApplicationContext();
		// connectivity check
		Object cm = context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = ((ConnectivityManager)cm).getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			throw new StandAloneException();
		}
		Log.i("NI", "Available: " + ni.isAvailable());
		Log.i("NI", "Connected: " + ni.isConnected());
		Log.i("NI", "ConnectedOr...: " + ni.isConnectedOrConnecting());
		Log.i("NI", "State: " + ni.getState());
		Log.i("NI", "DetailedState: " + ni.getDetailedState().toString());
		localDirectory = context.getFilesDir();
	}

	public void download(String urlString, File file) throws IOException {
		if (urlString == null || file == null) {
			throw new IllegalArgumentException("argument contains null");
		}

		URL url = new URL(urlString);
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		File tempFile = null;
		InputStream  is = null;
		OutputStream os = null;
		try {
			http.setRequestProperty("Accept-Encoding", "gzip");
			http.setRequestProperty("User-Agent", "AgoraGuide/2012 (Android)");		
			http.setIfModifiedSince(file.lastModified());
			http.setReadTimeout(HTTP_TIME_OUT);
			http.setDoInput(true);
			http.connect();

			int code = http.getResponseCode();

			Log.v("Downloader", code + " " + http.getResponseMessage());
			if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
				file.setLastModified(System.currentTimeMillis());
				return;
			}

			if (code != HttpURLConnection.HTTP_OK) {
				throw new IOException("fail to download");
			}

			String contentEncoding = http.getContentEncoding();
			is = http.getInputStream();
			if (contentEncoding != null && contentEncoding.equals("gzip")) {
				is = new GZIPInputStream(is);
			}
			is = new BufferedInputStream(is, BUFFER_SIZE);

			tempFile = File.createTempFile("downloading", null, localDirectory);
			os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os, BUFFER_SIZE);

			byte[] buffer = new byte[BUFFER_SIZE];
			int totalRead = 0;
			while (true) {
				int byteRead = is.read(buffer, 0, BUFFER_SIZE);
				if (byteRead == -1)
					break;
				os.write(buffer, 0, byteRead);
				totalRead += byteRead;
			}
			is.close();
			os.flush();
			os.close();

			if (!tempFile.renameTo(file)) {
				throw new IOException("download successed but cannot write specific file");
			}
			Log.i("Downloader ", totalRead + " bytes downloaded");
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
			if (http != null) {
				http.disconnect();
			}
		}
	}
}
