package com.cornellsatech.o_week.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Event;

/**
 * Receives messages sent from {@link android.app.AlarmManager}.
 * @see Notifications#scheduleForEvent(Event, int, Context)
 */
public class AlarmReceiver extends BroadcastReceiver
{
	private static final String TAG = AlarmReceiver.class.getSimpleName();
	public static final String EVENT_PK_KEY = "eventPk";

	/**
	 * Send a notification for the event whose pk we received.
	 * @param context
	 * @param intent Contains event's pk.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String eventPk = intent.getStringExtra(EVENT_PK_KEY);
		if (eventPk == null)
		{
			Log.e(TAG, "onReceive: No pk received");
			return;
		}

		//if the notification was created, then the event was deleted in an update, don't produce a notification
		Event event = UserData.eventForPk(eventPk);
		if (event != null)
			Notifications.createForEvent(event, context);
	}
}
