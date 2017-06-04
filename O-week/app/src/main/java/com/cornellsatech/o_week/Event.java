package com.cornellsatech.o_week;

import android.support.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class Event
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
		return (title.hashCode() + 31 * startTime.hashCode() * 31 + date.hashCode());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		return other.title.equals(title) && other.startTime.isEqual(startTime) && other.date.isEqual(date);
	}
}
