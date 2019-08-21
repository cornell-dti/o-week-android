package com.cornellsatech.o_week.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.models.StudentType;
import com.google.gson.JsonSyntaxException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles all operations involving saving and reading from disk. Every method requires {@link Context}
 * to create a {@link SharedPreferences}. Some of these methods are costly CPU-wise, so use sparingly.
 */
public final class Settings
{
	//Keys to saved data.
	private static final String KEY_CLEARED_VERSION = "clear";
	private static final String KEY_ALL_EVENTS = "allEvents";
	private static final String KEY_SELECTED_EVENTS = "selectedEvents";
	private static final String KEY_CATEGORIES = "categories";
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_STUDENT_TYPE = "studentType";
	private static final String KEY_COLLEGE_TYPE = "collegeType";
	private static final String KEY_RESOURCES = "resources";

	private static final String TAG = Settings.class.getSimpleName();

	//suppress default constructor
	private Settings(){}

	/**
	 * Saves the student information from app initial settings to disk.
	 * @param studentType whether a student is a transfer or freshman
	 * @param collegeType the name of the college the student is in.
	 */
	public static void setStudentInfo(Context context, StudentType studentType, CollegeType collegeType){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_STUDENT_TYPE, studentType.toString());
		editor.putString(KEY_COLLEGE_TYPE, collegeType.toString());
		editor.apply();
	}

	/**
	 * Gets the saved type of the student.
	 * @return a StudentType Enum indicating the student's saved type status
	 */
	public static StudentType getStudentSavedType(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String studentTypeStringRepresentation = preferences.getString(KEY_STUDENT_TYPE,
				StudentType.NOTSET.toString());
		return StudentType.valueOf(studentTypeStringRepresentation);
	}

	/**
	 * Gets the saved college type of the student
	 * @return a CollegeType Enum indicating the student's saved CollegeType Status
	 */
	public static CollegeType getStudentSavedCollegeType(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String collegeTypeStringRepresentation = preferences.getString(KEY_COLLEGE_TYPE,
				CollegeType.NOTSET.toString());
		return CollegeType.valueOf(collegeTypeStringRepresentation);
	}

	/**
	 * Saves {@link UserData#allEvents} to disk.
	 */
	public static void setAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> eventStrings = new HashSet<>();
		for (Event event : UserData.allEvents)
			eventStrings.add(event.toString());
		editor.putStringSet(KEY_ALL_EVENTS, eventStrings);
		editor.apply();
	}
	/**
	 * Returns all saved events.
	 * @return A set of saved events.
	 */
	public static Set<Event> getAllEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> eventStrings = preferences.getStringSet(KEY_ALL_EVENTS, new HashSet<String>());
		Set<Event> allEvents = new HashSet<>(eventStrings.size());
		try
		{
			for (String eventString : eventStrings)
				allEvents.add(Event.fromJSON(eventString));
		}
		catch (JsonSyntaxException e)
		{
			Log.e(TAG, "Could not load events from settings.", e);
		}
		return allEvents;
	}

	/**
	 * Saves the {@link Event#getPk()} of all events in {@link UserData#selectedEvents}.
	 */
	public static void setSelectedEvents(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> selectedEventsPks = new HashSet<>(UserData.selectedEvents.size());
		for (Event event : UserData.selectedEvents)
			selectedEventsPks.add(event.getPk());
		editor.putStringSet(KEY_SELECTED_EVENTS, selectedEventsPks);
		editor.apply();
	}
	/**
	 * Returns all selected events' pks (as strings, since the only data structure we can store are
	 * sets of strings).
	 *
	 * @return Set of pks of all selected events.
	 */
	public static Set<String> getSelectedEventsPks(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getStringSet(KEY_SELECTED_EVENTS, new HashSet<String>());
	}
	/**
	 * Saves {@link UserData#categories} to disk.
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
	 *
	 * @return Set of saved categories.
	 */
	public static Set<Category> getCategories(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> categoryStrings = preferences.getStringSet(KEY_CATEGORIES, new HashSet<String>());
		Set<Category> categories = new HashSet<>(categoryStrings.size());
		try
		{
			for (String categoryString : categoryStrings)
				categories.add(Category.fromJSON(categoryString));
		}
		catch (JsonSyntaxException e)
		{
			Log.e(TAG, "Could not load catgories from settings.", e);
		}
		return categories;
	}
	/**
	 * Saves the latest timestamp of events' retrieval.
	 *
	 * @param timestamp The time the current version of the database was retrieved.
	 */
	public static void setTimestamp(long timestamp, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(KEY_TIMESTAMP, timestamp);
		editor.apply();
	}
	/**
	 * Returns the time events were last retrieved.
	 * @return The timestamp downloaded from the database, or 0 by default. This timestamp will
	 *         be provided to the database so it can tell us what to update.
	 */
	public static long getTimestamp(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(KEY_TIMESTAMP, 0);  //default version value = 0
	}

	public static void setResources(Map<String, String> resources, Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> resourceNameLinkStrings = new HashSet<>(resources.size());
		for (Map.Entry<String, String> entry : resources.entrySet())
		{
			String name = entry.getKey();
			String link = entry.getValue();
			resourceNameLinkStrings.add(name + "|" + link);
		}
		editor.putStringSet(KEY_RESOURCES, resourceNameLinkStrings);
		editor.apply();
	}

	public static Map<String, String> getResources(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> resourceNameLinkStrings = preferences.getStringSet(KEY_RESOURCES, Collections.<String>emptySet());
		Map<String, String> resourceNameLinks = new HashMap<>(resourceNameLinkStrings.size());
		for (String resourceNameLinkString : resourceNameLinkStrings)
		{
			String[] parts = resourceNameLinkString.split("\\|");
			String name = parts[0];
			String link = parts[1];
			resourceNameLinks.put(name, link);
		}
		return resourceNameLinks;
	}

	/**
	 * Returns whether or not the user wants to receive reminders.
	 * Change default value of along with {@link R.xml#preferences}.
	 */
	public static boolean getReceiveReminders(Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getString(R.string.key_receive_reminders), true);
	}
	/**
	 * Returns the number of hours before an event starts to notify the user.
	 * Change default value of <code>notifyMeText</code> along with {@link R.xml#preferences}.
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
	 */
	public static int getNotifyMe(String notifyMeValue, Context context)
	{
		String[] choices = context.getResources().getStringArray(R.array.settings_notify_me_titles);
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
	 * Deletes all stored data if version number does not match saved version. Prevents old data from
	 * crashing app if it isn't backwards compatible.
	 */
	public static void clearAllForNewVersion(Context context)
	{
		try
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			String clearedVersion = preferences.getString(KEY_CLEARED_VERSION, "");
			if (clearedVersion.equals(versionName))
				return; // already cleared for this version

			preferences.edit()
					.clear()
					.putString(KEY_CLEARED_VERSION, versionName)
					.apply();
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.e(TAG, "Could not find app version name");
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
