package com.cornellsatech.o_week;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;

import java.util.Map;

/**
 * Fragment to allow users to change their preferences.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private SwitchPreferenceCompat receiveReminders;
	private ListPreference notifyMe;
	private static final String TAG = SettingsFragment.class.getSimpleName();

    /**
     * Sets up preferences for the user with listeners.
     * Dynamically load resources from {@link UserData#resourceNameLink}.
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preferences);

        receiveReminders = (SwitchPreferenceCompat) findPreference(R.string.key_receive_reminders);
        notifyMe = (ListPreference) findPreference(R.string.key_notify_me);
        receiveReminders.setOnPreferenceChangeListener(this);
        notifyMe.setOnPreferenceChangeListener(this);

	    PreferenceCategory resourceCategory = new PreferenceCategory(getContext());
	    resourceCategory.setIconSpaceReserved(false);
	    resourceCategory.setTitle(R.string.settings_category_resources);
	    getPreferenceScreen().addPreference(resourceCategory);

	    for (Map.Entry<String, String> entry : UserData.resourceNameLink.entrySet()) {
		    Preference preference = new Preference(getContext());
		    preference.setTitle(entry.getKey());
		    preference.setKey(entry.getValue());
		    preference.setIconSpaceReserved(false);
		    preference.setOnPreferenceClickListener(this);
		    resourceCategory.addPreference(preference);
	    }
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
		Internet.openToPage(preference.getKey(), getContext());
		return true;
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference.equals(receiveReminders))
			receiveRemindersChanged();
		else if (preference.equals(notifyMe))   //resend all notifications as necessary
			Notifications.scheduleForEvents(Settings.getNotifyMe(newValue.toString(), getContext()), getActivity());
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
