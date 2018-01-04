package com.cornellsatech.o_week;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.models.StudentType;
import com.cornellsatech.o_week.util.Callback;
import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Settings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all data shared between classes. Many of these variables have associated {@link NotificationCenter}
 * events that should be fired when they are changed, so do so when changing their values.
 *
 * {@link #allEvents}: All events on disk, sorted by date.
 * {@link #selectedEvents}: All events selected by the user, sorted by date.
 * {@link #categories}: All categories on disk.
 * {@link #DATES}: Dates of the orientation. Determined from {@link #YEAR}, {@link #MONTH}, {@link #START_DAY},
 *                 and {@link #END_DAY}.
 * {@link #selectedDate}: The date to display events for.
 * {@link #selectedFilters}: An integer set that represents all the currently selected filters.
 *                               appear in the feed.
 * {@link #filterRequired}: True if "required events" filter is on.
 * {@link #collegeType}: Which college the student belongs in.
 * {@link #studentType}: What kind of student the user is.
 */
public final class UserData
{
	public static final Map<LocalDate, List<Event>> allEvents;
	public static final Map<LocalDate, List<Event>> selectedEvents;
	public static List<Category> categories = new ArrayList<>();
	public static final List<LocalDate> DATES;
	public static LocalDate selectedDate;
	public final static Set<Integer> selectedFilters = new HashSet<>();
	public static boolean filterRequired = false;
	private static CollegeType collegeType;
	private static StudentType studentType;
	private static final int YEAR = 2018;
	private static final int MONTH = 1;
	private static final int START_DAY = 18;    //Dates range: [START_DAY, END_DAY], inclusive
	private static final int END_DAY = 23;      //Note: END_DAY must > START_DAY
	private static final String TAG = UserData.class.getSimpleName();

	/**
	 * Initialize {@link #DATES} and lists for maps of events
	 */
	static
	{
		ImmutableList.Builder<LocalDate> tempDates = ImmutableList.builder();
		ImmutableMap.Builder<LocalDate, List<Event>> tempAllEvents = ImmutableMap.builder();
		ImmutableMap.Builder<LocalDate, List<Event>> tempSelectedEvents = ImmutableMap.builder();
		LocalDate today = LocalDate.now();
		for (int i = START_DAY; i <= END_DAY; i++)
		{
			LocalDate date = new LocalDate(YEAR, MONTH, i);
			if (date.isEqual(today))
				selectedDate = date;
			tempDates.add(date);
			tempAllEvents.put(date, new ArrayList<Event>());
			tempSelectedEvents.put(date, new ArrayList<Event>());
		}
		DATES = tempDates.build();
		allEvents = tempAllEvents.build();
		selectedEvents = tempSelectedEvents.build();

		if (selectedDate == null)
			selectedDate = DATES.get(0);
	}

	//suppress instantiation
	private UserData(){}

	/**
	 * Returns true if the event is selected.
	 * @param event The event that we want to check is selected.
	 * @return See method description.
	 */
	public static boolean selectedEventsContains(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		return eventsForDate != null && eventsForDate.contains(event);
	}
	/**
	 * Adds event to {@link #allEvents} for the correct date according to {@link Event#date}.
	 * The date should match a date in {@link #DATES}.
	 * @param event Event to add.
	 */
	public static void appendToAllEvents(Event event)
	{
		List<Event> eventsForDate = allEvents.get(event.date);
		if (eventsForDate == null)
			return;
		if (!eventsForDate.contains(event))
			eventsForDate.add(event);
	}
	/**
	 * Removes event form {@link #allEvents}.
	 * @param event Event to remove.
	 */
	public static void removeFromAllEvents(Event event)
	{
		List<Event> eventsForDate = allEvents.get(event.date);
		if (eventsForDate != null)
			eventsForDate.remove(event);
	}
	/**
	 * Adds event to {@link #selectedEvents}. The date should match a date in {@link #DATES}.
	 * @param event Event to add.
	 * @return True if the event was added
	 */
	@CanIgnoreReturnValue
	public static boolean insertToSelectedEvents(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		if (eventsForDate == null)
		{
			Log.e(TAG, "insertToSelectedEvents: attempted to add event with date outside orientation");
			return false;
		}
		if (eventsForDate.contains(event))
			return false;

		eventsForDate.add(event);
		return true;
	}
	/**
	 * Removes event from {@link #selectedEvents}.
	 * @param event Event to remove.
	 */
	public static void removeFromSelectedEvents(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		if (eventsForDate == null)
			Log.e(TAG, "removeFromSelectedEvents: No selected events for date");
		else
			eventsForDate.remove(event);
	}
	/**
	 * Linear search for an event given its pk value.
	 * @param pk {@link Event#pk}
	 * @return Event. May be null.
	 */
	@Nullable
	public static Event eventForPk(int pk)
	{
		for (List<Event> eventsOfDay : allEvents.values())
			for (Event event : eventsOfDay)
				if (event.pk == pk)
					return event;
		Log.e(TAG, "eventForPk: Event not found for given pk");
		return null;
	}
	/**
	 * Linear search for a category given its pk value.
	 * @param pk {@link Category#pk}
	 * @return Category. May be null.
	 * @see #eventForPk(int)
	 */
	@Nullable
	public static Category categoryForPk(int pk)
	{
		for (Category category : categories)
			if (category.pk == pk)
				return category;
		Log.e(TAG, "categoryForPk: Category not found for given pk");
		return null;
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
		final Set<Event> events = Settings.getAllEvents(context);
		for (Event event : events)
			appendToAllEvents(event);

		Set<Category> categories = Settings.getCategories(context);
		UserData.categories = new ArrayList<>(categories);

		sortEventsAndCategories();

		Internet.getUpdatesForVersion(Settings.getVersion(context), context, new Callback<Integer>()
		{
			//versionNum is 0 if failed
			@Override
			public void execute(Integer versionNum)
			{
				//all version updates have been processed. Now, load events that the user has selected into selectedEvents.
				//this assumes selectedEvents is empty
				Set<Event> selectedEvents = Settings.getSelectedEvents(context);
				for (Event selectedEvent : selectedEvents)
					insertToSelectedEvents(selectedEvent);

				//save all the new data if available
				if (versionNum == 0)
					return;
				//sort everything again, since things have been updated
				sortEventsAndCategories();
				Settings.setAllEvents(context);
				Settings.setCategories(context);
				Settings.setVersion(versionNum, context);
			}
		});

		loadStudentCollegeTypes(context);
	}
	/**
	 * Sorts {@link #allEvents} and {@link #categories}.
	 */
	private static void sortEventsAndCategories()
	{
		for (List<Event> events : allEvents.values())
			Collections.sort(events);
		Collections.sort(UserData.categories);
	}

	/**
	 * Sets {@link #collegeType} and {@link #studentType} based on saved settings.
	 * Necessary for {@link #requiredForUser(Event)} to always return the correct result.
	 *
	 * @param context
	 */
	public static void loadStudentCollegeTypes(Context context)
	{
		collegeType = Settings.getStudentSavedCollegeType(context);
		studentType = Settings.getStudentSavedType(context);
	}

	/**
	 * Checks whether the given event is required for the current user
	 * @param event
	 * @return true if this event is required for the current user, false otherwise.
	 */
	public static boolean requiredForUser(Event event)
	{
		if (event.required)
			return true;

		if (!event.categoryRequired)
			return false;

		boolean collegeRequired = CollegeType.toCollegeType(event.category) == collegeType;
		boolean studentTypeRequired = StudentType.toStudentType(event.category) == studentType;
		return collegeRequired || studentTypeRequired;
	}

	/**
	 * Returns an array of Strings of filters available to the user for {@link FeedFragment}.
	 * @param context Context to get the string for "Show required events" {@link R.string#filter_show_required_events}
	 * @return See method description.
	 */
	public static String[] getFilters(Context context)
	{
		String[] filters = new String[categories.size() + 1];
		//index 0 represents filter for "required events"
		filters[0] = context.getString(R.string.filter_show_required_events);
		for (int i = 1; i < filters.length; i++)
			filters[i] = categories.get(i - 1).name;
		return filters;
	}

	/**
	 * Returns an array of booleans that represents whether the filter is checked.
	 *
	 * @see #getFilters(Context)
	 * @return See method description
	 */
	public static boolean[] getCheckedFilters()
	{
		boolean[] filters = new boolean[categories.size() + 1];
		//index 0 represents filter for "required events"
		filters[0] = filterRequired;
		//all other indices represent categories, in order.
		for (int i = 0; i < categories.size(); i++)
			filters[i+1] = selectedFilters.contains(categories.get(i).pk);
		return filters;
	}
}
