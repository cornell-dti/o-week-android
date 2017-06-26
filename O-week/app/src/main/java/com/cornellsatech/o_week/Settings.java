package com.cornellsatech.o_week;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Settings
{
	private static final String KEY_ALL_EVENTS = "allEvents";
	private static final String KEY_SELECTED_EVENTS = "selectedEvents";
	private static final String KEY_CATEGORIES = "categories";
	private static final String KEY_VERSION = "version";

	//suppress default constructor
	private Settings(){}

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
	public static Set<Event> getAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> eventStrings = preferences.getStringSet(KEY_ALL_EVENTS, new HashSet<String>());
		Set<Event> allEvents = new HashSet<>(eventStrings.size());

		for (String eventString : eventStrings)
			allEvents.add(Event.fromString(eventString));
		return allEvents;
	}

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
	public static Set<Event> getSelectedEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> selectedEventPks = preferences.getStringSet(KEY_SELECTED_EVENTS, new HashSet<String>());
		Set<Event> allEvents = getAllEvents(context);
		Set<Event> selectedEvents = new HashSet<>(selectedEventPks.size());
		for (String selectedEventPk : selectedEventPks)
			for (Event event : allEvents)
				if (selectedEventPks.contains(String.valueOf(event.pk)))
					selectedEvents.add(event);
		return selectedEvents;
	}

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
	public static Set<Category> getCategories(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> categoryStrings = preferences.getStringSet(KEY_CATEGORIES, new HashSet<String>());
		Set<Category> categories = new HashSet<>(categoryStrings.size());

		for (String categoryString : categoryStrings)
			categories.add(Category.fromString(categoryString));
		return categories;
	}

	public static void setVersion(int version, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_VERSION, version);
		editor.apply();
	}
	public static int getVersion(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(KEY_VERSION, 0);  //default version value = 0
	}
}
