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
}
