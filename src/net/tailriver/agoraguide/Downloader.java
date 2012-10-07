package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class Downloader extends AsyncTask<Downloader.Pair, Void, Void> {
	private static final String thisClass = Downloader.class.getSimpleName();
	private static final int BUFFER_SIZE = 4096;
	private static final int MAX_WORKER = 3;

	private File localDirectory;

	static {
		// patch
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public Downloader(Context context) throws StandAloneException {
		// connectivity check
		Object cm = context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = ((ConnectivityManager)cm).getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			throw new StandAloneException();
		}

		localDirectory = context.getFilesDir();
	}

	private boolean download(Pair p) {
		if (p == null) {
			return true;
		}
		if (p.url == null || p.file == null) {
			throw new IllegalArgumentException("pair is null");
		}

		HttpURLConnection http;
		try {
			URL url = new URL(p.url);
			http = (HttpURLConnection) url.openConnection();
			http.setRequestProperty("Accept-Encoding", "gzip");
			http.setRequestProperty("User-Agent", "AgoraGuide/2012 (Android)");		
			http.setIfModifiedSince(p.file.lastModified());
			http.setDoInput(true);
			http.connect();
		} catch (IOException e) {
			Log.w("HttpURLConnection", e.getMessage(), e);
			return false;
		}

		File tempFile = null;
		try {
			int code = http.getResponseCode();

			Log.v(thisClass, code + " " + http.getResponseMessage());

			if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
				p.file.setLastModified(System.currentTimeMillis());
				return true;
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

			tempFile = File.createTempFile("downloading", null, localDirectory);
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

			if (!tempFile.renameTo(p.file)) {
				throw new IOException("download successed but cannot write specific file");
			}
			Log.i(thisClass, "Downloaded " + String.valueOf(totalRead) + " bytes");
			return true;
		} catch (Exception e) {
			Log.e(thisClass, "Exception", e);
			return false;
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
			if (http != null) {
				http.disconnect();
			}
		}
	}

	@Override
	protected Void doInBackground(Pair... params) {
		try {
			int worker = 0;
			for (Pair p : params) {
				if (worker < MAX_WORKER) {
					worker++;
					download(p);
					worker--;
				} else {
					wait(200);
				}
			}
		} catch (InterruptedException e) {
			Log.e("Downloader", "interrupted", e);
		}
		return null;
	}

	public final AsyncTask<Pair, Void, Void> execute(List<Pair> list) {
		return execute(list.toArray(new Pair[list.size()]));
	}

	static class Pair {
		final String url;
		final File   file;
		Pair(String url, File file) {
			this.url  = url;
			this.file = file;
		}
	}
}
