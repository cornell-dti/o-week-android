package com.cornellsatech.o_week;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserData
{
	public static final Map<LocalDate, List<Event>> allEvents;
	public static final Map<LocalDate, List<Event>> selectedEvents;
	public static final List<LocalDate> DATES;
	public static LocalDate selectedDate;
	private static final int YEAR = 2017;
	private static final int MONTH = 8;
	private static final int START_DAY = 19;    //Dates range: [START_DAY, END_DAY], inclusive
	private static final int END_DAY = 24;      //Note: END_DAY must > START_DAY
	public static final String TAG = "UserData";

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

	/**
	 * Saves event to SharedPreferences and appends it to the array of all events.
	 * @param event Event to save
	 */
	public static void saveEvent(Event event, Context context)
	{
		appendToAllEvents(event);
		Settings.setEvent(event, context);
	}

	public static void loadData(Context context)
	{
		Set<Event> events = Settings.getAllEvents(context);
		Set<String> selectedEventPks = Settings.getAllSelectedEventsPks(context);

		if (events.isEmpty())
			for (LocalDate date : DATES)
				Internet.getEventsOnDate(date, context);
		else
		{
			for (Event event : events)
			{
				appendToAllEvents(event);
				if (selectedEventPks.contains(String.valueOf(event.pk)))
					insertToSelectedEvents(event);
			}

			//Telling other classes to reload their data
			NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
		}
	}
}
