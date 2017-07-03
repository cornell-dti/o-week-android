package com.cornellsatech.o_week;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserData
{
	public static final Map<LocalDate, List<Event>> allEvents;
	public static final Map<LocalDate, List<Event>> selectedEvents;
	public static List<Category> categories = new ArrayList<>();
	public static final List<LocalDate> DATES;
	public static LocalDate selectedDate;
	public static int selectedFilterIndex = 0;
	private static final int YEAR = 2017;
	private static final int MONTH = 8;
	private static final int START_DAY = 19;    //Dates range: [START_DAY, END_DAY], inclusive
	private static final int END_DAY = 24;      //Note: END_DAY must > START_DAY
	private static final String TAG = UserData.class.getSimpleName();

	//initialize DATES
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
	
	public static boolean allEventsContains(Event event)
	{
		List<Event> eventsForDate = allEvents.get(event.date);
		return eventsForDate != null && eventsForDate.contains(event);
	}
	public static boolean selectedEventsContains(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		return eventsForDate != null && eventsForDate.contains(event);
	}
	public static void appendToAllEvents(Event event)
	{
		List<Event> eventsForDate = allEvents.get(event.date);
		if (eventsForDate == null)
		{
			Log.e(TAG, "appendToAllEvents: attempted to add event with date outside orientation");
			return;
		}
		if (!eventsForDate.contains(event))
			eventsForDate.add(event);
	}
	public static void removeFromAllEvents(Event event)
	{
		List<Event> eventsForDate = allEvents.get(event.date);
		if (eventsForDate == null)
			Log.e(TAG, "removeFromAllEvents: No selected events for date");
		else
			eventsForDate.remove(event);
	}
	public static void insertToSelectedEvents(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		if (eventsForDate == null)
		{
			Log.e(TAG, "insertToSelectedEvents: attempted to add event with date outside orientation");
			return;
		}
		if (!eventsForDate.contains(event))
			eventsForDate.add(event);
	}
	public static void removeFromSelectedEvents(Event event)
	{
		List<Event> eventsForDate = selectedEvents.get(event.date);
		if (eventsForDate == null)
			Log.e(TAG, "removeFromSelectedEvents: No selected events for date");
		else
			eventsForDate.remove(event);
	}

	public static void loadData(final Context context)
	{
		final Set<Event> events = Settings.getAllEvents(context);
		for (Event event : events)
			appendToAllEvents(event);

		Set<Category> categories = Settings.getCategories(context);
		UserData.categories = new ArrayList<>(categories);

		Internet.getUpdatesForVersion(Settings.getVersion(context), new Internet.Callback()
		{
			//msg is the versionNum as a String. null if failed
			@Override
			public void execute(String msg)
			{
				//sort everything
				for (List<Event> events : allEvents.values())
					Collections.sort(events);
				Collections.sort(UserData.categories);

				//all version updates have been processed. Now, load events that the user has selected into selectedEvents.
				//this assumes selectedEvents is empty
				Set<Event> selectedEvents = Settings.getSelectedEvents(context);
				for (Event selectedEvent : selectedEvents)
					insertToSelectedEvents(selectedEvent);

				//save all the new data if available
				if (msg == null)
					return;
				Settings.setAllEvents(context);
				Settings.setCategories(context);
				Settings.setVersion(Integer.valueOf(msg), context);
			}
		});
	}
}
