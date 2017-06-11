package com.cornellsatech.o_week;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class Settings
{
	private static final String KEY_ALL_EVENTS = "alLEvents";
	private static final String KEY_SELECTED_EVENTS = "selectedEvents";

	//suppress default constructor
	private Settings(){}

	/**
	 * Saves the given event to SharedPreferences, using its pk as the key
	 * @param event Event to save
	 * @param context Context for SharedPreferences
	 */
	public static void setEvent(Event event, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(String.valueOf(event.pk), event.toString());

		//add pk to all events
		Set<String> allEventsPks = preferences.getStringSet(KEY_ALL_EVENTS, new HashSet<String>());
		allEventsPks.add(String.valueOf(event.pk));
		editor.putStringSet(KEY_ALL_EVENTS, allEventsPks);

		editor.apply();
	}

	public static void setEventSelected(Event event, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> selectedEventsPks = preferences.getStringSet(KEY_SELECTED_EVENTS, new HashSet<String>());
		selectedEventsPks.add(String.valueOf(event.pk));
		editor.putStringSet(KEY_SELECTED_EVENTS, selectedEventsPks);
		editor.apply();
	}

	public static Set<Event> getAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> allEventsPks = preferences.getStringSet(KEY_ALL_EVENTS, new HashSet<String>());
		Set<Event> allEvents = new HashSet<>(allEventsPks.size());

		for (String pkString : allEventsPks)
			allEvents.add(Event.fromString(preferences.getString(pkString, null)));
		return allEvents;
	}

	public static Set<String> getAllSelectedEventsPks(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getStringSet(KEY_SELECTED_EVENTS, new HashSet<String>());
	}
}
