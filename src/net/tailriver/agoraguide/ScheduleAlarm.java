package net.tailriver.agoraguide;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ScheduleAlarm extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		int id = intent.getIntExtra(IntentExtra.NOTIFICATION_ID, 0);
		CharSequence contentTitle = context.getString(R.string.app_name);
		CharSequence contentText  = intent.getStringExtra(IntentExtra.NOTIFICATION_TEXT);
		long when = intent.getLongExtra(IntentExtra.NOTIFICATION_WHEN, System.currentTimeMillis());

		intent.setClass(context, ProgramActivity.class);
		intent.removeExtra(IntentExtra.NOTIFICATION_TEXT);
		intent.removeExtra(IntentExtra.NOTIFICATION_WHEN);
		Log.i("SA", intent.getExtras().keySet().toString());

		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		// TODO multiple notifications does not work appropriately.
		// it works last one only
		Notification n = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.icon)
		.setTicker(contentText)
		.setContentTitle(contentTitle)
		.setContentText(contentText)
		.setContentIntent(contentIntent)
		.setWhen(when)
		.build();
		getNotificationManager(context).notify(id, n);		
	}

	/** You have to check whether the set of hash code of id is identical or not (2012 ok) */
	public static int setAlarm(EntrySummary summary, long alarmWhen, long notificationWhen) {
		Context context = AgoraActivity.getStaticApplicationContext();
		PendingIntent operation = getPendingIntent(context, summary, notificationWhen);
		getAlarmManager(context).set(AlarmManager.RTC_WAKEUP, alarmWhen, operation);
		return summary.getId().hashCode();
	}

	public static final void cancelAlarm(EntrySummary summary, long notificationWhen) {
		Context context = AgoraActivity.getStaticApplicationContext();
		PendingIntent operation = getPendingIntent(context, summary, notificationWhen);
		getAlarmManager(context).cancel(operation);
	}

	public static final void cancelNotification(Context context, int id) {
		getNotificationManager(context).cancel(id);
	}

	private static final AlarmManager getAlarmManager(Context context) {
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	private static final NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private static PendingIntent getPendingIntent(Context context,
			EntrySummary summary, long notificationWhen) {
		int requestCode = summary.getId().hashCode();
		Intent intent = new Intent(context, ScheduleAlarm.class);
		intent.putExtra(IntentExtra.ENTRY_ID, summary.getId());
		intent.putExtra(IntentExtra.NOTIFICATION_ID, requestCode);
		intent.putExtra(IntentExtra.NOTIFICATION_TEXT, summary.toString());
		intent.putExtra(IntentExtra.NOTIFICATION_WHEN, notificationWhen);
		for (String s : intent.getExtras().keySet()) {
			Log.i("SA", "Intent " + s + ": " + intent.getExtras().get(s));
		}
		return PendingIntent.getBroadcast(context, requestCode, intent, 0);
	}
}
