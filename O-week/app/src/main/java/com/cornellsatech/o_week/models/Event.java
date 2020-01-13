package com.cornellsatech.o_week.models;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import com.cornellsatech.o_week.util.Internet;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data-type that holds all information about an event. Designed to be immutable. This will be downloaded
 * from the database via methods in {@link Internet}, where new events will be compared with saved ones.
 * Notable fields are explained below:
 *
 * {@link #categories}: The {@link Category#getPk()} of the {@link Category}s this event
 * belongs to.
 * {@link #start}: Epoch millis of start time in correct Ithaca timezone.
 * {@link #additional}: Additional information to display in a special format. Formatted like so:
 *                      ## HEADER ## ____BULLET # INFO ____BULLET # INFO
 *
 * @see Category
 */
@Getter
@RequiredArgsConstructor
public class Event implements Comparable<Event>
{
	private final String pk;
	private final String name;
	private final String description;
	private final String url;
	private final String img;
	private final String additional;
	private final String location;
	private final double longitude;
	private final double latitude;
	private final long start;
	private final long end;
	private LocalDate startDate;
	private LocalTime startTime;
	private LocalTime endTime;

	private final List<String> categories;
	private final boolean firstYearRequired;
	private final boolean transferRequired;
	private static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new EventExclusionStrategy())
            .addDeserializationExclusionStrategy(new EventExclusionStrategy())
            .create();
	public static final String DISPLAY_TIME_FORMAT = "h:mm a";  //hour:minute AM/PM
	public static final String DISPLAY_PADDED_TIME_FORMAT = "hh:mm a";
	public static final String DISPLAY_DATE_FORMAT = "EEEE, MMM d";
	private static final String TAG = Event.class.getSimpleName();

	public List<String> getCategories() {
		return categories;
	}

	public LocalDate getStartDate()
	{
		if (startDate == null)
			startDate = new LocalDate(start);
		return startDate;
	}

	public LocalTime getStartTime() {
		if (startTime == null)
			startTime = new LocalTime(start);
		return startTime;
	}

	public LocalTime getEndTime() {
		if (endTime == null)
			endTime = new LocalTime(end);
		return endTime;
	}

	public boolean hasCategory(CollegeType college)
	{
		return categories.contains(CollegeType.collegeToPk.get(college));
	}

	/**
	 * Returns the formatted additional text, with headers and bullets.
	 * String is like so: ## HEADER ## ____BULLET # INFO ____BULLET # INFO.
	 * PLEASE check that {@link #additional} is not empty before calling this method.
	 *
	 * @return Formatted text to be set for a {@link android.widget.TextView}
	 */
	public SpannableStringBuilder formattedAdditionalText()
	{
		SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
		String[] headerAndRemaining = additional.split("##");
		String header = headerAndRemaining[1].trim();
		String remaining = headerAndRemaining[2].trim();

		//set header
		stringBuilder.append(header).append("\n\n");
		stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, header.length(), 0);

		String[] sections = remaining.split("____");
		for (String section : sections)
		{
			if (section.isEmpty())
				continue;
			String[] bulletAndInfo = section.trim().split(" # ");
			String bullet = bulletAndInfo[0];
			String info = bulletAndInfo[1];

			//set bullet
			int bulletStart = stringBuilder.length();
			int bulletEnd = bulletStart + bullet.length();
			stringBuilder.append(bullet).append("\n");
			stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), bulletStart, bulletEnd, 0);
			stringBuilder.setSpan(new ForegroundColorSpan(Color.RED), bulletStart, bulletEnd, 0);

			//set info
			int infoStart = stringBuilder.length();
			int infoEnd = infoStart + info.length();
			stringBuilder.append(info).append("\n");
			stringBuilder.setSpan(new LeadingMarginSpan.Standard(100), infoStart, infoEnd, 0);
		}

		return stringBuilder;
	}
	/**
	 * Returns the {@link #pk}, which is unique to each {@link Event}.
	 *
	 * @return {@link #pk}
	 */
	@Override
	public int hashCode()
	{
		return pk.hashCode();
	}
	/**
	 * Returns whether this object has the same {@link #pk} as the given object.
	 *
	 * @param obj {@inheritDoc}
	 * @return See description.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		return other.pk.equals(pk);
	}

	/**
	 * Compares 2 {@link Event}s using their start times. Useful for ordering chronologically.
	 */
	@Override
	public int compareTo(@NonNull Event o)
	{
		return Double.compare(start, o.start);
	}

	@NonNull
	@Override
	public String toString() {
		return GSON.toJson(this);
	}

	/**
	 * Returns a {@link Event} from JSON.
	 *
	 * @param json String produced by {@link #toString()}.
	 * @return {@link Event} created from the String.
	 */
	public static Event fromJSON(String json)
	{
		return GSON.fromJson(json, Event.class);
	}

	public static class EventExclusionStrategy implements ExclusionStrategy
    {
        @Override
        public boolean shouldSkipField(FieldAttributes f)
        {
            if (f.getDeclaringClass() != Event.class)
                return false;
            switch (f.getName())
            {
                case "startDate":
                case "startTime":
                case "endTime":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz)
        {
            return false;
        }
    }
}
