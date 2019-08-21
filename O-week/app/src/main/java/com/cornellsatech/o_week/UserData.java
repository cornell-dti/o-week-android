package com.cornellsatech.o_week;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.models.StudentType;
import com.cornellsatech.o_week.models.VersionUpdate;
import com.cornellsatech.o_week.util.Callback;
import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;
import com.google.common.base.Joiner;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all data shared between classes. Many of these variables have associated {@link NotificationCenter}
 * events that should be fired when they are changed, so do so when changing their values.
 *
 * {@link #allEvents}: All events on disk.
 * {@link #selectedEvents}: All events selected by the user.
 * {@link #categories}: All categories on disk.
 * {@link #selectedDate}: The date to display events for.
 * {@link #selectedFilters}: An integer set that represents all the currently selected filters.
 *                               appear in the feed.
 * {@link #filterRequired}: True if "required events" filter is on.
 * {@link #collegeType}: Which college the student belongs in.
 * {@link #studentType}: What kind of student the user is.
 */
public final class UserData
{
	public static Set<Event> allEvents = new HashSet<>();
	public static final Set<Event> selectedEvents = new HashSet<>();
	public static Set<Category> categories = new HashSet<>();
	public static Map<String, String> resourceNameLink = new HashMap<>();
	public static List<LocalDate> sortedDates = new ArrayList<>();
	public static LocalDate selectedDate;
	public static final Set<String> selectedFilters = new HashSet<>();
	public static boolean filterRequired = false;
	private static CollegeType collegeType = CollegeType.NOTSET;
	private static StudentType studentType = StudentType.NOTSET;
	private static final String TAG = UserData.class.getSimpleName();

	//suppress instantiation
	private UserData(){}

	/**
	 * Linear search for an event given its pk value.
	 * @param pk {@link Event#getPk()}
	 * @return Event. May be null.
	 */
	@Nullable
	public static Event eventForPk(String pk) {
		for (Event event : allEvents)
			if (event.getPk().equals(pk))
				return event;
		Log.e(TAG, "eventForPk: Event not found for given pk");
		return null;
	}

	private static void populateSelectedEvents(Set<String> selectedEventsPks)
	{
		Log.i(TAG, "Cleared selected events: " + selectedEvents.size() + ", new size: " + selectedEventsPks.size());
        Set<String> previousSelectedEventsPks = new HashSet<>();
        for (Event event : selectedEvents)
            previousSelectedEventsPks.add(event.getPk());
        previousSelectedEventsPks.addAll(selectedEventsPks);
		selectedEvents.clear();
		for (Event event : allEvents)
			if (previousSelectedEventsPks.contains(event.getPk()))
				selectedEvents.add(event);
	}

	/**
	 * Loads {@link #allEvents}, {@link #selectedEvents}, {@link #categories}.
	 * 1. Retrieves all events and categories from disk, adding them to {@link #allEvents}, {@link #categories}.
	 * 2. Downloads updates from the database.
	 * 3. Sorts all events and categories. Retrieves selected events. If anything WAS updated from the
	 *    database, save the updates. Note that after events and categories are saved here, the lists
	 *    they belong in should not be mutated any further.
	 *
	 * @param context
	 */
	public static void loadData(final Context context)
	{
		loadStudentCollegeTypes(context);

        resourceNameLink = Settings.getResources(context);
        if (resourceNameLink.isEmpty())
            Internet.getResources(context);

        allEvents = Settings.getAllEvents(context);
		loadDates();
		final Set<String> selectedEventsPks = Settings.getSelectedEventsPks(context);
		populateSelectedEvents(selectedEventsPks);

		categories = Settings.getCategories(context);

		Internet.getUpdatesForVersion(Settings.getTimestamp(context),
                new Callback<VersionUpdate>()
		{
			//timestamp is 0 if failed
			@Override
			public void execute(VersionUpdate update)
			{
				if (update == null || update.getTimestamp() == 0)
                {
                    NotificationCenter.DEFAULT.post(new NotificationCenter.EventInternetUpdate());
                    return;
                }

				Log.i(TAG, "Received timestamp: " + update.getTimestamp());
				Log.i(TAG, "Changed events: " + update.getEvents().getChanged().size());
				//update categories; this works because categories are compared using pk
				categories.removeAll(update.getCategories().getChanged());
				categories.addAll(update.getCategories().getChanged());
				//delete categories
				Set<Category> categoriesToDelete = new HashSet<>(update.getCategories().getDeleted().size());
				for (String pk : update.getCategories().getDeleted())
					categoriesToDelete.add(Category.withPk(pk));
				categories.removeAll(categoriesToDelete);

				//keep track of all changed events to notify the user
				Map<String, String> changedEventsPkName = new HashMap<>();
				//update/remove notifications
				boolean remindersOn = Settings.getReceiveReminders(context);
				//update events
				allEvents.removeAll(update.getEvents().getChanged());
				allEvents.addAll(update.getEvents().getChanged());
				for (Event event : update.getEvents().getChanged())
				{
					changedEventsPkName.put(event.getPk(), event.getName());

					//reschedule the event
					if (selectedEvents.remove(event))
					{
						selectedEvents.add(event);
						if (remindersOn)
							Notifications.scheduleForEvent(event, context);
					}
				}
				//delete events
				Iterator<Event> eventsIterator = allEvents.iterator();
				while (eventsIterator.hasNext())
				{
					Event event = eventsIterator.next();
					if (update.getEvents().getDeleted().contains(event.getPk()))
					{
						changedEventsPkName.put(event.getPk(), event.getName());
						eventsIterator.remove();

						//remove the event from notification
						if (remindersOn)
							Notifications.unscheduleForEvent(event, context);
					}
				}

                loadDates();

				//send a toast to alert the user that their events were updated
				if (changedEventsPkName.size() != 0)
				{
					//show toast only for changed events that you HAD selected
					List<String> selectedChangedEventsTitles = new ArrayList<>();
					for (String pk : selectedEventsPks)
					{
						String selectedChangedEventTitle = changedEventsPkName.get(pk);
						if (selectedChangedEventTitle != null)
							selectedChangedEventsTitles.add(selectedChangedEventTitle);
					}
					if (!selectedChangedEventsTitles.isEmpty())
					{
						String toastText = context.getString(R.string.toast_events_changed, Joiner.on(", ").join(selectedChangedEventsTitles));
						Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
					}
				}

				Settings.setAllEvents(context);
				Settings.setCategories(context);
				Settings.setTimestamp(update.getTimestamp(), context);
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventInternetUpdate());
			}
		});
	}

	/**
	 * Sets {@link #collegeType} and {@link #studentType} based on saved settings.
	 * Necessary for {@link #requiredForUser(Event)} to always return the correct result.
	 *
	 * @param context
	 */
	public static void loadStudentCollegeTypes(Context context)
	{
		try
		{
			collegeType = Settings.getStudentSavedCollegeType(context);
			studentType = Settings.getStudentSavedType(context);
		}
		catch (IllegalArgumentException e)
		{
			Log.e(TAG, "Could not load college/student type.", e);
		}
	}

	private static void loadDates()
	{
		Set<LocalDate> dates = new HashSet<>();
		for (Event event : allEvents)
			dates.add(event.getStartDate());
		sortedDates = new ArrayList<>(dates);
		Collections.sort(sortedDates);
	}

	/**
	 * Checks whether the given event is required for the current user
	 * @param event
	 * @return true if this event is required for the current user, false otherwise.
	 */
	public static boolean requiredForUser(Event event)
	{
		if (!event.hasCategory(collegeType))
			return false;

		switch (studentType)
		{
			case FRESHMAN:
				return event.isFirstYearRequired();
			case TRANSFER:
				return event.isTransferRequired();
		}

		return false;
	}
}
