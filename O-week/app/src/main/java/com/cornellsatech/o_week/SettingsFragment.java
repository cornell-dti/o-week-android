package com.cornellsatech.o_week;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;

import java.util.List;

/**
 * Fragment to allow users to change their preferences.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private ListPreference receiveReminders;
	private ListPreference notifyMe;
	private Preference addRequiredEvents;
	private Preference removeAllEvents;
	private static final String TAG = SettingsFragment.class.getSimpleName();

	/**
	 * Sets up preferences for the user with listeners.
	 * @param savedInstanceState Ignored.
	 */
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		receiveReminders = (ListPreference) findPreference(R.string.key_receive_reminders);
		notifyMe = (ListPreference) findPreference(R.string.key_notify_me);
		receiveReminders.setOnPreferenceChangeListener(this);
		notifyMe.setOnPreferenceChangeListener(this);
		addRequiredEvents = findPreference(R.string.settings_add_required_events);
		removeAllEvents = findPreference(R.string.settings_remove_events);
		addRequiredEvents.setOnPreferenceClickListener(this);
		removeAllEvents.setOnPreferenceClickListener(this);

		//set notify me to disabled if necessary
		toggleEnableNotifyMe(receiveReminders.getValue());
	}
	/**
	 * Helper method for finding the Preference for the given key using the key's resId as opposed
	 * to the key String itself.
	 *
	 * @param keyId ResId for the key String
	 * @return Preference
	 */
	private Preference findPreference(@StringRes int keyId)
	{
		return findPreference(getString(keyId));
	}
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference.equals(addRequiredEvents))
			addRequiredEvents();
		else if (preference.equals(removeAllEvents))
			removeAllEvents();
		else
			Log.e(TAG, "onPreferenceClick: unrecognized preference");
		return true;
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference.equals(receiveReminders))
			receiveRemindersChanged(newValue.toString());
		else if (preference.equals(notifyMe))   //resend all notifications as necessary
			Notifications.scheduleForEvents(Settings.getNotifyMe(newValue.toString(), getActivity()), getActivity());
		return true;
	}

	/**
	 * Shows a dialog asking the user if they'd like to select all required events, doing so should they
	 * click OK.
	 *
	 * NOTE: Update <code>shouldNotify</code> if the ordering of {@link R.array#settings_receive_reminders_titles} changes.
	 */
	private void addRequiredEvents()
	{
		showDialog(R.string.dialog_title_add_required_events, R.string.dialog_message_add_required_events,
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						//notifications should be sent unless the user selected that they wouldn't want it to be sent.
						boolean shouldNotify = Settings.getReceiveReminders(getActivity()) != 2;

						for (List<Event> eventsOfDay : UserData.allEvents.values())
							for (Event event : eventsOfDay)
								if (event.required)
									//insert into selected events. If insertion worked (event wasn't previously selected) and we should notify, schedule a notification
									if (UserData.insertToSelectedEvents(event) && shouldNotify)
										Notifications.scheduleForEvent(event, getActivity());

						NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
					}
				});
	}
	/**
	 * Shows a dialog asking the user if they'd like to deselect all events, doing so should they
	 * click OK.
	 */
	private void removeAllEvents()
	{
		showDialog(R.string.dialog_title_remove_events, R.string.dialog_message_remove_events,
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						for (List<Event> eventsOfDay : UserData.selectedEvents.values())
						{
							for (Event event : eventsOfDay)
								Notifications.unscheduleForEvent(event, getActivity());
							eventsOfDay.clear();
						}
						NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
					}
				});
	}
	/**
	 * Creates/destroys notifications based on old and new values of {@link #receiveReminders}.
	 * Updating {@link R.array#settings_receive_reminders_titles} requires an update to this method.
	 *
	 * @param newValue The new String value for {@link #receiveReminders}
	 */
	private void receiveRemindersChanged(String newValue)
	{
		//don't update if values never changed
		String oldValue = receiveReminders.getValue();
		if (oldValue.equals(newValue))
			return;

		String[] choices = getResources().getStringArray(R.array.settings_receive_reminders_titles);
		String notifyAll = choices[0];
		String notifyRequired = choices[1];

		//------------------------------------------------- different actions depending on old value

		int hoursBefore = Settings.getNotifyMe(notifyMe.getValue(), getActivity());

		//we had notifications set up for all events
		if (oldValue.equals(notifyAll))
		{
			//remove notifications for all non-required events
			if (newValue.equals(notifyRequired))
				Notifications.unscheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return !event.required;
					}
				}, getActivity());
			else    //remove notification for all events
				Notifications.unscheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return true;
					}
				}, getActivity());
		}

		//we had notifications set up for required events only
		else if (oldValue.equals(notifyRequired))
		{
			//add notifications for all non-required events
			if (newValue.equals(notifyAll))
				Notifications.scheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return !event.required;
					}
				}, hoursBefore, getActivity());
			else    //remove notification for all required events
				Notifications.unscheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return event.required;
					}
				}, getActivity());
		}

		//we had no notifications in the past
		else
		{
			//add notifications for all events
			if (newValue.equals(notifyAll))
				Notifications.scheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return true;
					}
				}, hoursBefore, getActivity());
			//add notifications for required events
			else
				Notifications.scheduleForEventsWithFilter(new Notifications.Filter()
				{
					@Override
					public boolean shouldActUpon(Event event)
					{
						return event.required;
					}
				}, hoursBefore, getActivity());
		}

		//notify me could be disabled/enabled by the new change. Check.
		toggleEnableNotifyMe(newValue);
	}
	/**
	 * Helper method for creating an AlertDialog with the given title, message, and inner class.
	 *
	 * @param title String resId for the title
	 * @param message String resId for the message
	 * @param onConfirm Method that will execute should the user click the positive button
	 */
	private void showDialog(@StringRes int title, @StringRes int message, DialogInterface.OnClickListener onConfirm)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder.setTitle(title)
				.setMessage(message)
				.setCancelable(true)
				.setPositiveButton(R.string.dialog_positive_button, onConfirm)
				.setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				})
				.show();
	}
	/**
	 * Disables {@link #notifyMe} if {@link #receiveReminders} is set to "No events".
	 * @param receiveRemindersValue The new value for {@link #receiveReminders}. Doesn't directly pull
	 *                            the value from the variable since this method may be called from
	 *                            {@link #onPreferenceChange(Preference, Object)}, where the variable
	 *                            has yet to be updated.
	 */
	private void toggleEnableNotifyMe(String receiveRemindersValue)
	{
		String noReminders = getString(R.string.receive_reminders_no_events);
		notifyMe.setEnabled(!receiveRemindersValue.equals(noReminders));
	}
}
