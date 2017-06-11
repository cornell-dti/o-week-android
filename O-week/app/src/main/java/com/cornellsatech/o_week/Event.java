package com.cornellsatech.o_week;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

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
	public static final String DISPLAY_TIME_FORMAT = "h:mm a";  //hour:minute AM/PM
	private static final String DATABASE_TIME_FORMAT = "HH:mm:ss";
	private static final String DATABASE_DATE_FORMAT = "yyyy-MM-dd";
	private static final DateTimeFormatter DATABASE_TIME_FORMATTER = DateTimeFormat.forPattern(DATABASE_TIME_FORMAT);
	private static final DateTimeFormatter DATABASE_DATE_FORMATTER = DateTimeFormat.forPattern(DATABASE_DATE_FORMAT);

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

	public Event(JSONObject jsonObject)
	{
		title = jsonObject.optString("name");
		pk = jsonObject.optInt("pk");
		description = jsonObject.optString("description");
		caption = jsonObject.optString("location");
		category = jsonObject.optString("category");
		String startDate = jsonObject.optString("start_date");
		String startTime = jsonObject.optString("start_time");
		String endTime = jsonObject.optString("end_time");

		date = LocalDate.parse(startDate, DATABASE_DATE_FORMATTER);
		this.startTime = LocalTime.parse(startTime, DATABASE_TIME_FORMATTER);
		this.endTime = LocalTime.parse(endTime, DATABASE_TIME_FORMATTER);
		//TODO change to actual required value
		required = false;
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

	@Override
	public String toString()
	{
		return title + "|" + caption + "|" + description + "|" + category + "|" + DATABASE_DATE_FORMATTER.print(date) +
				"|" + DATABASE_TIME_FORMATTER.print(startTime) + "|" + DATABASE_TIME_FORMATTER.print(endTime) + "|" +
				(required ? 1 : 0) + "|" + pk;
	}

	public static Event fromString(String string)
	{
		String[] parts = string.split("|");
		String title = parts[0];
		String caption = parts[1];
		String description = parts[2];
		String category = parts[3];
		String date = parts[4];
		String startTime = parts[5];
		String endTime = parts[6];
		String required = parts[7];
		String pk = parts[8];
		return new Event(title, caption, description, category, LocalDate.parse(date, DATABASE_DATE_FORMATTER),
				LocalTime.parse(startTime, DATABASE_TIME_FORMATTER), LocalTime.parse(endTime, DATABASE_TIME_FORMATTER),
				required.equals("1"), Integer.valueOf(pk));
	}
}
