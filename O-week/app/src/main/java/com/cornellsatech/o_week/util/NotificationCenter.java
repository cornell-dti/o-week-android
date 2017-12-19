package com.cornellsatech.o_week.util;

import com.cornellsatech.o_week.DatePickerAdapter;
import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.FeedAdapter;
import com.cornellsatech.o_week.UserData;
import com.google.common.eventbus.EventBus;

/**
 * Used for communication between objects that have no reference to each other.
 *
 * Any classes that wish to listen to events (not {@link Event}s) must register itself by calling
 * {@link EventBus#register(Object)}, and call {@link EventBus#unregister(Object)} on {@link #DEFAULT}
 * before it is garbage collected to avoid memory leaks. To listen to events, write methods as so:
 *
 * <code>
 *     \@Subscribe
 *     public void methodName(EventToListenTo e){}
 * </code>
 *
 * Since methods are recognized based on the type of their parameter, each event must have a unique
 * parameter type. For every new event, create the parameter type as a class below. Give the class
 * a {@code public final} variable if the event should transmit data.
 *
 * See more documentation <a href="https://github.com/google/guava/wiki/EventBusExplained">here</a>.
 */
public final class NotificationCenter
{
	public static final EventBus DEFAULT = new EventBus();

	/**
	 * Indicates information about events have changed. Anything that displays a list of events should update.
	 */
	public static class EventReload {}

	/**
	 * Indicates the user either
	 * 1. Swiped to a new date in {@link com.cornellsatech.o_week.DatePagerAdapter}.
	 * 2. Clicked on a new date in {@link DatePickerAdapter}.
	 * Note: {@link UserData#selectedDate} will contain the new date.
	 */
	public static class EventDateChanged
	{
		public EventDateChanged() {}
	}
	/**
	 * Indicates the {@link #event} has been selected or unselected.
	 */
	public static class EventSelectionChanged
	{
		public final Event event;
		public final boolean selected;
		public EventSelectionChanged(Event event, boolean selected)
		{
			this.event = event;
			this.selected = selected;
		}
	}

	/**
	 * Indicates a new {@link Category} has been selected as the filter for {@link FeedAdapter}.
	 */
	public static class EventFilterChanged {}
}
