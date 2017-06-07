package com.cornellsatech.o_week;

public class NotificationCenter
{
	public static final com.google.common.eventbus.EventBus DEFAULT = new com.google.common.eventbus.EventBus();

	static class EventReload {}
	static class EventDateSelected
	{
		public final DateCell selectedCell;
		public EventDateSelected(DateCell selectedCell)
		{
			this.selectedCell = selectedCell;
		}
	}
	static class EventEventClicked
	{
		public final Event event;
		public EventEventClicked(Event event)
		{
			this.event = event;
		}
	}
	static class EventSelectionChanged
	{
		public final Event event;
		public EventSelectionChanged(Event event)
		{
			this.event = event;
		}
	}
}
