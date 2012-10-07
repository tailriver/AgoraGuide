package net.tailriver.agoraguide;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleAlarm extends BroadcastReceiver {
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		AgoraGuideActivity.initDatabase(context);

		EntrySummary summary = getSummary(intent);

		Log.i("onReceive", summary + " " + summary.getTitle() + "@" + System.currentTimeMillis());

		int id = (int) (Math.random() * Integer.MAX_VALUE);
		CharSequence contentTitle = context.getString(R.string.app_name);
		CharSequence contentText  = summary.getTitle();
		Intent detailActivity = new Intent(context, EntryDetailActivity.class);
		detailActivity.putExtra(EntryDetailActivity.INTENT_ENTRY, summary);
		detailActivity.putExtra(EntryDetailActivity.INTENT_NOTIFICATION_ID, id);
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 0, detailActivity, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification n = new Notification(R.drawable.icon, contentTitle, System.currentTimeMillis());
		n.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		getNotificationManager(context).notify(id, n);
	}

	public static void setAlerm(Context context, EntrySummary summary, long when) {
		int requestCode = (int) (Integer.MAX_VALUE * Math.random());
		Intent intent = new Intent(context, ScheduleAlarm.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary);
		PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, when, pending);
	}

	public static final void cancelNotification(Context context, int id) {
		getNotificationManager(context).cancel(id);		
	}

	public static final void cancelAllNotifications(Context context) {
		getNotificationManager(context).cancelAll();
	}

	private static final NotificationManager getNotificationManager(Context context) {
		return ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
	}

	private synchronized final EntrySummary getSummary(Intent intent) {
		try {
			while (!AgoraGuideActivity.isInitFinished()) {
				wait(200);
			}
		} catch (InterruptedException e1) {}
		return intent.getParcelableExtra(EntryDetailActivity.INTENT_ENTRY);
	}
}
