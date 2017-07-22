package com.cornellsatech.o_week;

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
public class NotificationCenter
{
	public static final EventBus DEFAULT = new EventBus();

	/**
	 * Indicates information about events have changed (updated from the database).
	 * Note: {@link UserData#selectedEvents} will not be accurate at this time.
	 */
	static class EventReload {}
	/**
	 * Indicates the user selected a new date. The new date can be accessed with {@link DateCell#getDate()}.
	 * Note: {@link UserData#selectedDate} will not be accurate at this time.
	 */
	static class EventDateSelected
	{
		public final DateCell selectedCell;
		public EventDateSelected(DateCell selectedCell)
		{
			this.selectedCell = selectedCell;
		}
	}
	/**
	 * Indicates the user clicked on an event in a list of events. The event clicked is {@link #event}.
	 */
	static class EventEventClicked
	{
		public final Event event;
		public EventEventClicked(Event event)
		{
			this.event = event;
		}
	}
	/**
	 * Indicates the {@link #event} has been selected or unselected.
	 */
	static class EventSelectionChanged
	{
		public final Event event;
		public EventSelectionChanged(Event event)
		{
			this.event = event;
		}
	}

	/**
	 * Indicates a new {@link Category} has been selected as the filter for {@link FeedAdapter}.
	 */
	static class EventFilterChanged {}
}
