package com.cornellsatech.o_week;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class Event implements Comparable<Event>
{
	public final String title;
	public final String caption;
	public final String description;
	public final String category;
	public final LocalDate date;
	public final LocalTime startTime;
	public final LocalTime endTime;
	public final boolean required;
	public final int pk;

	public Event(String title, String caption, @Nullable String description, @Nullable String category, LocalDate date, LocalTime startTime, LocalTime endTime, boolean required, int pk)
	{
		this.title = title;
		this.caption = caption;
		this.description = (description == null) ? "No description available at this time" : description;
		this.category = (category == null) ? "Default" : category;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.required = required;
		this.pk = pk;
	}

	@Override
	public int hashCode()
	{
		return pk;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		return other.pk == pk;
	}

	//Events ordered by start time
	@Override
	public int compareTo(@NonNull Event o)
	{
		if (!date.isEqual(o.date))
			return date.compareTo(o.date);
		//If this event starts in the next day, and event "o" starts in the previous day
		if (startTime.getHourOfDay() <= ScheduleFragment.END_HOUR && o.startTime.getHourOfDay() >= ScheduleFragment.START_HOUR)
			return 1;
		//If this event starts in the previous day, and event "o" starts in the next day
		if (o.startTime.getHourOfDay() <= ScheduleFragment.END_HOUR && startTime.getHourOfDay() >= ScheduleFragment.START_HOUR)
			return -1;
		//if the times are the same, return 0. This is done because LocalTime.compareTo also looks at things like milliseconds
		if (startTime.isEqual(o.startTime))
			return 0;
		return startTime.compareTo(o.startTime);
	}
}
