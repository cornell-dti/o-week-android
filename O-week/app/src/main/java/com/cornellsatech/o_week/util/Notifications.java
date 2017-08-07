package com.cornellsatech.o_week.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.cornellsatech.o_week.DetailsActivity;
import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Event;
import com.google.common.base.Joiner;

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
	 * Creates a notification to tell the user that the app's events have been updated. Does nothing if
	 * the given list of changed events is empty.
	 *
	 * @param changedEventsTitles Titles of changed events.
	 * @param context
	 */
	public static void createForChangedEvents(List<String> changedEventsTitles, Context context)
	{
		if (changedEventsTitles.isEmpty())
			return;

		String notificationCaption = context.getString(R.string.notification_events_changed_caption,
					Joiner.on(", ").join(changedEventsTitles));

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setVisibility(Notification.VISIBILITY_PUBLIC)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(context.getString(R.string.notification_events_changed_title))
				.setContentText(notificationCaption)
				.setAutoCancel(true);        //notification will disappear on click
		builder.setDefaults(Notification.DEFAULT_ALL);  //default sound, light, vibration

		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());
	}
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

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
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
	 * Based on {@link Settings#getReceiveReminders(Context)}, determine if the selected event should
	 * have a notification scheduled. Call this after the user selects and event and before
	 * {@link #scheduleForEvent(Event, int, Context)}.
	 *
	 * @param event Event that has been selected.
	 * @param context
	 * @return True if the event should have a notification.
	 * @see #scheduleForEvents(int, Context)
	 */
	public static boolean shouldScheduleForEvent(Event event, Context context)
	{
		switch (Settings.getReceiveReminders(context))
		{
			//notify for all events
			case 0:
				return true;
			//notify for required events
			case 1:
				return event.required;
			//don't notify
			case 2:
				return false;
			default:
				Log.e(TAG, "shouldScheduleForEvent: Unexpected receiveReminders option selected");
				return false;
		}
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
	 * Schedule notifications for any events that
	 * 1. Are selected by the user
	 * 2. Should be scheduled based on {@link Settings#getReceiveReminders(Context)}.
	 *
	 * @param hoursBefore # of hours before the event starts that the notification will be sent.
	 * @param context
	 */
	public static void scheduleForEvents(int hoursBefore, Context context)
	{
		switch (Settings.getReceiveReminders(context))
		{
			//notify for all events
			case 0:
				scheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return true;
					}
				}, hoursBefore, context);
			//notify for required events
			case 1:
				scheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return event.required;
					}
				}, hoursBefore, context);
			//don't notify
			case 2:
				return;
			default:
				Log.e(TAG, "scheduleForEvents: Unexpected receiveReminders option selected");
		}
	}
	/**
	 * Schedule notifications for any events that
	 * 1. Are selected by the user
	 * 2. Pass the given filter
	 *
	 * @param filter Contains function to filter events.
	 * @param hoursBefore # of hours before the event starts that the notification will be sent.
	 * @param context
	 */
	public static void scheduleForEventsWithFilter(Filter filter, int hoursBefore, Context context)
	{
		for (List<Event> eventsOfDay : UserData.selectedEvents.values())
			for (Event event : eventsOfDay)
				if (filter.shouldActUpon(event))
					scheduleForEvent(event, hoursBefore, context);
	}
	/**
	 * Destroys scheduled notifications for any events that
	 * 1. Are selected by the user
	 * 2. Pass the given filter
	 *
	 * @param filter Contains function to filter events.
	 * @param context
	 */
	public static void unscheduleForEventsWithFilter(Filter filter, Context context)
	{
		for (List<Event> eventsOfDay : UserData.selectedEvents.values())
			for (Event event : eventsOfDay)
				if (filter.shouldActUpon(event))
					unscheduleForEvent(event, context);
	}

	/**
	 * An interface for filtering events.
	 *
	 * @see #scheduleForEventsWithFilter(Filter, int, Context)
	 * @see #unscheduleForEventsWithFilter(Filter, Context)
	 */
	public interface Filter
	{
		/**
		 * Returns whether or not this event was shouldActUpon (accepted).
		 * @param event Event to filter.
		 * @return True if event shouldActUpon
		 */
		boolean shouldActUpon(Event event);
	}
}
