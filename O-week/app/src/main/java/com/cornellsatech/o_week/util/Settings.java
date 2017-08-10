package com.cornellsatech.o_week.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles all operations involving saving and reading from disk. Every method requires {@link Context}
 * to create a {@link SharedPreferences}. Some of these methods are costly CPU-wise, so use sparingly.
 */
public final class Settings
{
	//Keys to saved data.
	private static final String KEY_ALL_EVENTS = "allEvents";
	private static final String KEY_SELECTED_EVENTS = "selectedEvents";
	private static final String KEY_CATEGORIES = "categories";
	private static final String KEY_VERSION = "version";

	private static final String TAG = Settings.class.getSimpleName();

	//suppress default constructor
	private Settings(){}

	/**
	 * Saves {@link UserData#allEvents} to disk.
	 * @param context
	 */
	public static void setAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> eventStrings = new HashSet<>();
		for (List<Event> eventsForDay : UserData.allEvents.values())
			for (Event event : eventsForDay)
				eventStrings.add(event.toString());
		editor.putStringSet(KEY_ALL_EVENTS, eventStrings);
		editor.apply();
	}
	/**
	 * Returns all saved events.
	 * @param context
	 * @return A set of saved events.
	 */
	public static Set<Event> getAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> eventStrings = preferences.getStringSet(KEY_ALL_EVENTS, new HashSet<String>());
		Set<Event> allEvents = new HashSet<>(eventStrings.size());

		for (String eventString : eventStrings)
			allEvents.add(Event.fromString(eventString));
		return allEvents;
	}
	/**
	 * Saves the {@link Event#pk} of all events in {@link UserData#selectedEvents}.
	 * @param context
	 */
	public static void setSelectedEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> selectedEventsPks = new HashSet<>();
		for (List<Event> selectedEventsForDay : UserData.selectedEvents.values())
			for (Event selectedEvent : selectedEventsForDay)
				selectedEventsPks.add(String.valueOf(selectedEvent.pk));
		editor.putStringSet(KEY_SELECTED_EVENTS, selectedEventsPks);
		editor.apply();
	}
	/**
	 * Returns all selected events based on the saved {@link Event#pk} and events retrieved using
	 * {@link #getAllEvents(Context)}.
	 *
	 * Behavior:
	 * 1. Update - if a selected event is updated (the {@link Event#pk} is unchanged), it remains selected.
	 * 2. Deletion - selected events are "unselected" after deletion. We simply won't find the event
	 *               in {@link #getAllEvents(Context)}.
	 * @param context
	 * @return Set of all selected events.
	 */
	public static Set<Event> getSelectedEvents(Context context)
	{
		Set<String> selectedEventPks = getSelectedEventsPks(context);
		Set<Event> allEvents = getAllEvents(context);
		Set<Event> selectedEvents = new HashSet<>(selectedEventPks.size());
		for (Event event : allEvents)
			if (selectedEventPks.contains(String.valueOf(event.pk)))
				selectedEvents.add(event);
		return selectedEvents;
	}
	/**
	 * Returns all selected events' pks (as strings, since the only data structure we can store are sets of strings).
	 *
	 * @param context
	 * @return Set of pks of all selected events.
	 */
	public static Set<String> getSelectedEventsPks(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getStringSet(KEY_SELECTED_EVENTS, new HashSet<String>());
	}
	/**
	 * Saves {@link UserData#categories} to disk.
	 * @param context
	 */
	public static void setCategories(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> categoryStrings = new HashSet<>(UserData.categories.size());
		for (Category category : UserData.categories)
			categoryStrings.add(category.toString());
		editor.putStringSet(KEY_CATEGORIES, categoryStrings);
		editor.apply();
	}
	/**
	 * Returns all saved categories.
	 * @param context
	 * @return Set of saved categories.
	 */
	public static Set<Category> getCategories(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> categoryStrings = preferences.getStringSet(KEY_CATEGORIES, new HashSet<String>());
		Set<Category> categories = new HashSet<>(categoryStrings.size());

		for (String categoryString : categoryStrings)
			categories.add(Category.fromString(categoryString));
		return categories;
	}
	/**
	 * Saves the current event database version. If the saved version does not match the database's,
	 * an update is in store.
	 *
	 * @param version The current version of events saved on disk. The version number should increase as
	 *                changes are made on the database.
	 * @param context
	 */
	public static void setVersion(int version, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_VERSION, version);
		editor.apply();
	}
	/**
	 * Returns the database version of saved events.
	 * @param context
	 * @return The version of events downloaded from the database, or 0 by default. This version will
	 *         be provided to the database so it can tell us what to update.
	 */
	public static int getVersion(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(KEY_VERSION, 0);  //default version value = 0
	}

	/**
	 * Returns the index of the current selection in {@link R.array#settings_receive_reminders_titles}.
	 * Change default value of <code>receiveRemindersText</code> along with {@link R.xml#preferences}.
	 *
	 * @param context
	 * @return See method description.
	 */
	public static int getReceiveReminders(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String receiveRemindersText = preferences.getString(context.getString(R.string.key_receive_reminders),
				context.getString(R.string.receive_reminders_all_events));
		String[] choices = context.getResources().getStringArray(R.array.settings_receive_reminders_titles);
		return linearSearch(choices, receiveRemindersText);
	}
	/**
	 * Returns the number of hours before an event starts to notify the user.
	 * Change default value of <code>notifyMeText</code> along with {@link R.xml#preferences}.
	 *
	 * @param context
	 * @return See method description.
	 */
	public static int getNotifyMe(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String notifyMeText = preferences.getString(context.getString(R.string.key_notify_me),
				context.getString(R.string.notify_me_2_hours));
		return getNotifyMe(notifyMeText, context);
	}
	/**
	 * Returns the number of hours before an event starts to notify the user.
	 * Use this method instead of {@link #getNotifyMe(Context)} if the new value might not have been
	 * saved yet.
	 *
	 * This should change should {@link R.array#settings_notify_me_titles} change.
	 *
	 * @param notifyMeValue The current string value of {@link com.cornellsatech.o_week.SettingsFragment#notifyMe}
	 * @param context
	 * @return See method description.
	 */
	public static int getNotifyMe(String notifyMeValue, Context context)
	{
		String choices[] = context.getResources().getStringArray(R.array.settings_notify_me_titles);
		int index = linearSearch(choices, notifyMeValue);
		switch (index)
		{
			case 0: //notify at time of event
				return 0;
			case 1: //notify 1 hour before event
				return 1;
			case 2: //notify 2 hours before event
				return 2;
			case 3: //notify 5 hours before event
				return 5;
			case 4: //notify 1 day before event
				return 24;
			default:
				Log.e(TAG, "getNotifyMe: Unexpected index selected in list");
				return 0;
		}
	}

	/**
	 * Conducts a linear search for a generic object in a generic array.
	 * @param array Array (does not have to be sorted)
	 * @param object Object to find
	 * @param <T> Generic type. Object of this type should extend {@link Object#equals(Object)}.
	 * @return Index of object in array, or -1 if failed.
	 */
	private static <T> int linearSearch(T[] array, T object)
	{
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(object))
				return i;
		return -1;
	}
}
