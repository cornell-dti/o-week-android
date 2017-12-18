package com.cornellsatech.o_week.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cornellsatech.o_week.DetailsActivity;
import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Event;

import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Handles creation & destruction of all local notifications.
 */
public final class Notifications
{
	private static final String TAG = Notifications.class.getSimpleName();

	//suppress constructor
	private Notifications(){}

	/**
	 * {@link #scheduleForEvent(Event, int, Context)} if you're certain hoursBefore is saved and the extra
	 * processing power spent retrieving it is necessary. For example, if you're calling this in a loop,
	 * it'd be better to retrieve hoursBefore once instead of calling this method and retreiving it multiple times.
	 *
	 * @param event Event to schedule notifications for.
	 * @param context
	 */
	public static void scheduleForEvent(Event event, Context context)
	{
		scheduleForEvent(event, Settings.getNotifyMe(context), context);
	}
	/**
	 * Schedules a notification for the given event, to be triggered a number of hours before the event
	 * actually starts.
	 *
	 * @param event Event to schedule notifications for.
	 * @param hoursBefore # of hours before the event starts that the notification will be sent.
	 * @param context
	 * @see AlarmReceiver
	 */
	public static void scheduleForEvent(Event event, int hoursBefore, Context context)
	{
		PendingIntent pendingIntent = intentForEvent(event, context);
		//start time
		LocalDateTime alarmTime = event.startDateTime().minusHours(hoursBefore);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.toDateTime().getMillis(), pendingIntent);
	}
	/**
	 * Creates and sends a notification for the given event.
	 *
	 * @param event Event to create notifications for.
	 * @param context
	 */
	public static void createForEvent(Event event, Context context)
	{
		//intent to open DetailsActivity on click
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.putExtra(DetailsActivity.EVENT_KEY, event.toString());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, event.pk, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification.Builder builder = new Notification.Builder(context);
		builder.setVisibility(Notification.VISIBILITY_PUBLIC)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(event.title)
				.setContentText(event.caption)
				.setAutoCancel(true);        //notification will disappear on click
		builder.setDefaults(Notification.DEFAULT_ALL);  //default sound, light, vibration
		builder.setContentIntent(pendingIntent);

		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(event.pk, builder.build());
	}
	/**
	 * Destroys any notification that was prepared for the given event.
	 *
	 * @param event Event to remove notifications for.
	 * @param context
	 */
	public static void unscheduleForEvent(Event event, Context context)
	{
		PendingIntent pendingIntent = intentForEvent(event, context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}
	/**
	 * Returns an Intent to open to {@link AlarmReceiver}.
	 * Note: {@link Event#pk} is used as {@link PendingIntent}'s requestCode, a unique identifier for
	 * when the alarm has to be updated/cancelled.
	 *
	 * @param event The event to open in {@link DetailsActivity}
	 * @param context
	 * @return PendingIntent (Pending = system will trigger it later)
	 */
	private static PendingIntent intentForEvent(Event event, Context context)
	{
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.EVENT_PK_KEY, event.pk);
		//if a new notification is created with the same intent, destroy the old one (cancel current)
		return PendingIntent.getBroadcast(context, event.pk, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	/**
	 * Helper function for {@link #scheduleForEvents(int, Context)}. Use this when you're certain that the
	 * new hoursBefore value is saved.
	 * @param context
	 */
	public static void scheduleForEvents(Context context)
	{
		scheduleForEvents(Settings.getNotifyMe(context), context);
	}
	/**
	 * Schedule notifications for all selected events
	 *
	 * @param hoursBefore # of hours before the event starts that the notification will be sent.
	 * @param context
	 */
	public static void scheduleForEvents(int hoursBefore, Context context)
	{
		for (List<Event> eventsOfDay : UserData.selectedEvents.values())
			for (Event event : eventsOfDay)
				scheduleForEvent(event, hoursBefore, context);
	}
	/**
	 * Destroys scheduled notifications for all selected events
	 *
	 * @param context
	 */
	public static void unscheduleForEvents(Context context)
	{
		for (List<Event> eventsOfDay : UserData.selectedEvents.values())
			for (Event event : eventsOfDay)
				unscheduleForEvent(event, context);
	}
}
