package net.tailriver.agoraguide;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleAlarm extends BroadcastReceiver {
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		AgoraGuideActivity.initDatabase(context);
		Context applicationContext = context.getApplicationContext();

		EntrySummary summary = getSummary(intent);

		int id = (int) (Math.random() * Integer.MAX_VALUE);
		CharSequence contentTitle = context.getString(R.string.app_name);
		CharSequence contentText  = summary.toString();
		Intent detailActivity = new Intent(applicationContext, EntryDetailActivity.class);
		detailActivity.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		detailActivity.putExtra(EntryDetailActivity.INTENT_NOTIFICATION_ID, id);
		PendingIntent contentIntent = PendingIntent.getActivity(
				applicationContext, 0, detailActivity, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification n = new Notification(R.drawable.icon, contentTitle, System.currentTimeMillis());
		n.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		getNotificationManager().notify(id, n);
	}

	public static void setAlerm(EntrySummary summary, long when) {
		Context context = AgoraGuideActivity.getContext();
		int requestCode = (int) (Integer.MAX_VALUE * Math.random());
		Intent intent = new Intent(context, ScheduleAlarm.class);
		intent.putExtra(EntryDetailActivity.INTENT_ENTRY, summary.getId());
		PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, when, pending);
	}

	public static final void cancelNotification(int id) {
		getNotificationManager().cancel(id);		
	}

	public static final void cancelAllNotifications(Context context) {
		getNotificationManager().cancelAll();
	}

	private static final NotificationManager getNotificationManager() {
		Context context = AgoraGuideActivity.getContext();
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private synchronized final EntrySummary getSummary(Intent intent) {
		try {
			while (!AgoraGuideActivity.isInitFinished()) {
				wait(200);
			}
		} catch (InterruptedException e1) {}
		return EntrySummary.get(intent.getStringExtra(EntryDetailActivity.INTENT_ENTRY));
	}
}
