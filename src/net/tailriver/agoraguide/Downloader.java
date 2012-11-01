package net.tailriver.agoraguide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;

public class Downloader extends AsyncTask<Void, Integer, Void> {
	private static final String CLASS_NAME = Downloader.class.getSimpleName();
	private static final int HTTP_TIME_OUT = 20000; // msec
	private static final int PROGRESS_MAX = 100;

	private FragmentActivity activity;
	private ProgressDialogFrag dialog;
	private List<Pair<URL, File>> task;
	private boolean showProgressDialog;

	static {
		// patch
		if (!AgoraActivity.hasFroyo()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public Downloader(FragmentActivity activity) throws StandAloneException {
		// connectivity check
		Object cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = ((ConnectivityManager)cm).getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			if (ni != null) {
				Log.i(CLASS_NAME, "State: " + ni.getState());
			}
			throw new StandAloneException();
		}
		this.activity = activity;
		task = new ArrayList<Pair<URL, File>>();
	}

	public void addTask(String url, File dist, long expire) {
		if (url == null || dist == null) {
			throw new IllegalArgumentException("argument contains null");
		}
		if (System.currentTimeMillis() - dist.lastModified() > expire) {
			try {
				task.add(Pair.create(new URL(url), dist));
			} catch (MalformedURLException e) {
				Log.w(CLASS_NAME, "invalid url", e);
			}
		}
	}

	public void setShowProgressDialog(boolean showProgressDialog) {
		this.showProgressDialog = showProgressDialog;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (dialog != null) {
			if (values[0] == null) {
				values[0] = dialog.getProgress() / PROGRESS_MAX;
			}
			dialog.setProgress(values[0] * PROGRESS_MAX + values[1]);
		}
	}

	@Override
	protected void onPreExecute() {
		if (showProgressDialog) {
			FragmentManager manager = activity.getSupportFragmentManager();
			dialog = new ProgressDialogFrag(activity);
			dialog.show(manager, "dialog");
			dialog.setMax(task.size());
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		publishProgress(0, 0);
		for (int i = 0, max = task.size(); i < max; i++) {
			Pair<URL, File> p = task.get(i);
			try {
				boolean isUpdated = download(p.first, p.second);
				if (p.second.equals(AgoraActivity.getDatabaseFile()) && isUpdated) {
					AgoraActivity.invalidateInit();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			publishProgress(i+1, 0);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (dialog != null) {
			dialog.onDismiss(dialog.getDialog());
		}
	}

	@Override
	protected void onCancelled(Void result) {
		Log.w(CLASS_NAME, "cancelled");
		if (dialog != null) {
			dialog.onDismiss(dialog.getDialog());
		}
	}

	private boolean download(URL url, File file) throws IOException {
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		File tempFile = null;
		try {
			http.setRequestProperty("Accept-Encoding", "gzip");
			http.setRequestProperty("User-Agent", "AgoraGuide/2012 (Android)");		
			http.setIfModifiedSince(file.lastModified());
			http.setReadTimeout(HTTP_TIME_OUT);
			http.setDoInput(true);
			http.connect();

			int code = http.getResponseCode();
			Log.d(CLASS_NAME, http.getURL().toString());
			Log.d(CLASS_NAME, code + " " + http.getResponseMessage());

			if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
				file.setLastModified(System.currentTimeMillis());
				return false;
			}
			if (code != HttpURLConnection.HTTP_OK) {
				throw new IOException("fail to download");
			}

			String contentEncoding = http.getContentEncoding();
			String contentType     = http.getContentType();
			InputStream is = http.getInputStream();
			if ((contentEncoding != null && contentEncoding.equals("gzip")) ||
					(contentType != null && contentType.contains("gzip"))) {
				is = new GZIPInputStream(is);
			}
			is = new BufferedInputStream(is);

			tempFile = File.createTempFile("downloading", null, activity.getFilesDir());
			OutputStream os = new FileOutputStream(tempFile);
			os = new BufferedOutputStream(os);

			int contentLength = http.getContentLength();
			byte[] buffer = new byte[8192];
			int totalRead = 0;
			while (true) {
				int byteRead = is.read(buffer);
				if (byteRead == -1)
					break;
				os.write(buffer, 0, byteRead);
				totalRead += byteRead;
				publishProgress(null, (int)(100d * totalRead / contentLength));
			}
			is.close();
			os.flush();
			os.close();

			if (!tempFile.renameTo(file)) {
				throw new IOException("download successed but cannot write specific file");
			}
			Log.i(CLASS_NAME, totalRead + " bytes downloaded");
			return true;
		} catch (UnknownHostException e) {
			Log.w(CLASS_NAME, e.getMessage());
			cancel(true);
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

	private final class ProgressDialogFrag extends DialogFragment {
		private ProgressDialog pd;

		public ProgressDialogFrag(Context context) {
			pd = new ProgressDialog(context);
		}

		@SuppressLint("NewApi")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			pd.setTitle("Updating");
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(false);
			if (AgoraActivity.hasHoneycomb()) {
				pd.setProgressNumberFormat(null);
			}
			pd.show();
			return pd;
		}

		public void setMax(int max) {
			pd.setMax(max * PROGRESS_MAX);
		}

		public int getProgress() {
			return pd.getProgress();
		}

		public void setProgress(int value) {
			pd.setProgress(value);
			if (value > pd.getMax()) {
				pd.setIndeterminate(true);
			}
		}
	}
}
