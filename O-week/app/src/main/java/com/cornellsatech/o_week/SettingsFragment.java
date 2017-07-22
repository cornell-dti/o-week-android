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

import java.util.List;

/**
 * Fragment to allow users to change their preferences.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
{
	Preference addRequiredEvents;
	Preference removeAllEvents;
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

		final ListPreference receiveReminders = (ListPreference) findPreference(R.string.settings_receive_reminders);
		receiveReminders.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int index = receiveReminders.findIndexOfValue(newValue.toString());
				return false;
			}
		});
		final ListPreference notifyMe = (ListPreference) findPreference(R.string.settings_notify_me);
		notifyMe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int index = notifyMe.findIndexOfValue(newValue.toString());
				return false;
			}
		});
		addRequiredEvents = findPreference(R.string.settings_add_required_events);
		removeAllEvents = findPreference(R.string.settings_remove_events);
		addRequiredEvents.setOnPreferenceClickListener(this);
		removeAllEvents.setOnPreferenceClickListener(this);
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
	/**
	 * Shows a dialog asking the user if they'd like to select all required events, doing so should they
	 * click OK.
	 */
	private void addRequiredEvents()
	{
		showDialog(R.string.dialog_title_add_required_events, R.string.dialog_message_add_required_events,
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						for (List<Event> eventsOfDay : UserData.allEvents.values())
							for (Event event : eventsOfDay)
								if (event.required)
									UserData.insertToSelectedEvents(event);
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
							eventsOfDay.clear();
						NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
					}
				});
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
}
