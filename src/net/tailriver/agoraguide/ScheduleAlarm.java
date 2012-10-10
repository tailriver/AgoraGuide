package net.tailriver.agoraguide;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class ScheduleAlarm extends BroadcastReceiver {
	private static NotificationManager nm;

	@Override
	public void onReceive(Context context, Intent intent) {
		new InitializeTask(this, context.getApplicationContext(), intent);
	}

	public void onPreInitialize(Context context, Intent intent) {
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@SuppressWarnings("deprecation")
	public void onPostInitialize(Context context, Intent intent) {
		String entryId = intent.getStringExtra(EntryDetailActivity.INTENT_ENTRY);

		int id = (int) (Math.random() * Integer.MAX_VALUE);

		Intent detailActivity = new Intent(context, EntryDetailActivity.class);
		detailActivity.putExtra(EntryDetailActivity.INTENT_ENTRY, entryId);
		detailActivity.putExtra(EntryDetailActivity.INTENT_NOTIFICATION_ID, id);

		CharSequence contentTitle = context.getString(R.string.app_name);
		CharSequence contentText  = EntrySummary.get(entryId).toString();
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 0, detailActivity, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification n = new Notification(R.drawable.icon, contentTitle, System.currentTimeMillis());
		n.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		nm.notify(id, n);		
	}

	public static void setAlerm(EntrySummary summary, long when) {
		Context context = AgoraInitializer.getApplicationContext();
		int requestCode = (int) (Integer.MAX_VALUE * Math.random());
		Intent intent = new Intent(context, ScheduleAlarm.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, when, pending);
	}

	public static final void cancelNotification(int id) {
		nm.cancel(id);		
	}

	public static final void cancelAllNotifications(Context context) {
		nm.cancelAll();
	}

	private static class InitializeTask extends AsyncTask<Void, Void, Void> {
		private ScheduleAlarm alarm;
		private Context context;
		private Intent intent;

		InitializeTask(ScheduleAlarm alarm, Context context, Intent intent) {
			this.alarm   = alarm;
			this.context = context;
			this.intent  = intent;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			alarm.onPreInitialize(context, intent);
		}

		@Override
		protected Void doInBackground(Void... params) {
			AgoraInitializer.init(context);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			alarm.onPostInitialize(context, intent);
		}
	}
}
