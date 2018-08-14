package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;

/**
 * Fragment to allow users to change their preferences.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private SwitchPreferenceCompat receiveReminders;
	private ListPreference notifyMe;
	private Preference orientationPamphlet;
	private Preference campusMap;
	private Preference newStudentsWebpage;
	private Preference dti;
	private static final String TAG = SettingsFragment.class.getSimpleName();

    /**
     * Sets up preferences for the user with listeners.
     * @param savedInstanceState Ignored.
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preferences);

        receiveReminders = (SwitchPreferenceCompat) findPreference(R.string.key_receive_reminders);
        notifyMe = (ListPreference) findPreference(R.string.key_notify_me);
        receiveReminders.setOnPreferenceChangeListener(this);
        notifyMe.setOnPreferenceChangeListener(this);
        orientationPamphlet = findPreference(R.string.settings_orientation_pamphlet);
        campusMap = findPreference(R.string.settings_campus_map);
        newStudentsWebpage = findPreference(R.string.settings_new_students_webpage);
        dti = findPreference(R.string.settings_dti);
        orientationPamphlet.setOnPreferenceClickListener(this);
        campusMap.setOnPreferenceClickListener(this);
        newStudentsWebpage.setOnPreferenceClickListener(this);
        dti.setOnPreferenceClickListener(this);
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
		if (preference.equals(orientationPamphlet))
			Internet.openToPage(Internet.ORIENTATION_PAMPHLET, getActivity());
		else if (preference.equals(campusMap))
			Internet.openToPage(Internet.CAMPUS_MAP, getActivity());
		else if (preference.equals(newStudentsWebpage))
			Internet.openToPage(Internet.NEW_STUDENTS_WEBPAGE, getActivity());
		else if (preference.equals(dti))
			Internet.openToPage(Internet.DTI, getActivity());
		else
			Log.e(TAG, "onPreferenceClick: unrecognized preference");
		return true;
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference.equals(receiveReminders))
			receiveRemindersChanged();
		else if (preference.equals(notifyMe))   //resend all notifications as necessary
			Notifications.scheduleForEvents(Settings.getNotifyMe(newValue.toString(), getActivity()), getActivity());
		return true;
	}

	/**
	 * Creates/destroys notifications based on new value of {@link #receiveReminders}.
	 */
	private void receiveRemindersChanged()
	{
		//new value must be opposite of old value
		boolean remindersOn = !receiveReminders.isChecked();

		if (remindersOn)
			Notifications.scheduleForEvents(getActivity());
		else
			Notifications.unscheduleForEvents(getActivity());
	}
}
