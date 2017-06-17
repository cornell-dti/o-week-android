package com.cornellsatech.o_week;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Notes on how view IDs are numbered in this class:
 * 1. For time lines: ID = hour the time line represents + 1
 * 2. For events: the event's hashcode
 */
public class ScheduleFragment extends Fragment implements View.OnClickListener
{
	private RelativeLayout scheduleContainer;
	private PercentRelativeLayout eventsContainer;
	private SparseArray<Event> pkToEvent = new SparseArray<>();
	private int HOUR_HEIGHT;
	private int HOUR_TEXT_HEIGHT;
	private static final List<LocalTime> HOURS;
	public static final int START_HOUR = 7;    //Hours range: [START_HOUR, END_HOUR], inclusive
	public static final int END_HOUR = 2;      //Note: Hours wrap around, from 7~23, then 0~2. END_HOUR must < START_HOUR
	private static final String TAG = ScheduleFragment.class.getSimpleName();

	static
	{
		ImmutableList.Builder<LocalTime> tempHours = ImmutableList.builder();
		for (int hour = START_HOUR; hour != END_HOUR + 1; hour++)
		{
			if (hour == 24)
				hour = 0;
			tempHours.add(new LocalTime(hour, 0));
		}
		HOURS = tempHours.build();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		NotificationCenter.DEFAULT.register(this);

		View view = inflater.inflate(R.layout.fragment_schedule, container, false);
		scheduleContainer = (RelativeLayout) view.findViewById(R.id.scheduleContainer);
		eventsContainer = (PercentRelativeLayout) view.findViewById(R.id.eventsContainer);
		HOUR_HEIGHT = getActivity().getResources().getDimensionPixelSize(R.dimen.distance_between_time_lines);
		HOUR_TEXT_HEIGHT = getActivity().getResources().getDimensionPixelSize(R.dimen.size_hour_textview);
		drawTimeLines();
		drawCells();
		return view;
	}
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
	}

	private void drawTimeLines()
	{
		LayoutInflater inflater = getActivity().getLayoutInflater();
		for (LocalTime hour : HOURS)
		{
			View timeLine = inflater.inflate(R.layout.time_line, scheduleContainer, false);
			timeLine.setId(hourToId(hour.getHourOfDay()));    //id of view = hour
			TextView hourText = (TextView) timeLine.findViewById(R.id.hourText);
			hourText.setText(hour.toString("h a")); //Ex: 11 AM
			scheduleContainer.addView(timeLine, timeLineMargins(timeLine, hour));
		}
	}
	private void drawCells()
	{
		List<Event> selectedEvents = UserData.selectedEvents.get(UserData.selectedDate);
		Collections.sort(selectedEvents);
		if (selectedEvents.isEmpty())
			return;
		drawEvent(1, new SparseArray<Event>(), new ArrayDeque<>(selectedEvents));
	}
	private Pair<Integer, SparseArray<Event>> drawEvent(int numSlots, SparseArray<Event> eventForSlot, Queue<Event> events)
	{
		Event event = events.poll();
		int slot = slotForEvent(event, numSlots, eventForSlot);

		int newNumSlots = numSlots;
		SparseArray<Event> newEventForSlot = eventForSlot.clone();
		newEventForSlot.append(slot, event);

		//if this event was assigned to a slot equal to the current number of slots, resize the current number of slots
		if (slot == numSlots)
			newNumSlots += 1;

		//before processing future events, create the schedule cell for this event so future schedule cells can reference it in recursion
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View scheduleCell = inflater.inflate(R.layout.cell_schedule, eventsContainer, false);
		scheduleCell.setId(eventToId(event));

		//if there's a later event, process that before we can calculate the position of the current event
		if (!events.isEmpty())
		{
			Event nextEvent = events.peek();
			if (areEventOverlaps(nextEvent, newNumSlots, newEventForSlot))
			{
				Pair<Integer, SparseArray<Event>> recursiveData = drawEvent(newNumSlots, newEventForSlot, events);
				newNumSlots = recursiveData.first;
				newEventForSlot = recursiveData.second;
			}
			else
				drawEvent(1, new SparseArray<Event>(), events);
		}

		scheduleCell.setLayoutParams(scheduleCellMargins(scheduleCell, event, slot, newNumSlots, eventForSlot, newEventForSlot));
		scheduleCell.setOnClickListener(this);
		TextView title = (TextView) scheduleCell.findViewById(R.id.titleText);
		TextView caption = (TextView) scheduleCell.findViewById(R.id.captionText);
		title.setText(event.title);
		caption.setText(event.caption);
		eventsContainer.addView(scheduleCell);
		pkToEvent.put(event.pk, event);

		//the parent event wants to know if any new events have been added to the right of it, but not beneath it. Therefore, only let the parent know of slots that are added, not replaced. This way it expands to the right by the correct value.
		SparseArray<Event> parentEventForSlot = eventForSlot.clone();
		for (int i = 0; i < newEventForSlot.size(); i++)
		{
			int slotToCheck = newEventForSlot.keyAt(i);
			if (canUseSlot(slotToCheck, event, parentEventForSlot))
				parentEventForSlot.put(slotToCheck, newEventForSlot.get(slotToCheck));
		}

		return new Pair<>(newNumSlots, parentEventForSlot);
	}

	private RelativeLayout.LayoutParams timeLineMargins(View timeLine, LocalTime time)
	{
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();

		//set top of layout
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

		int topMargin = HOUR_HEIGHT * (minutesBetween(HOURS.get(0), time) / 60);
		topMargin += HOUR_HEIGHT / 2;    //extra padding at top
		topMargin -= HOUR_TEXT_HEIGHT / 2; //turn "topMargin" into "centerMargin." Makes aligning events easier
		layoutParams.topMargin = topMargin;

		//set bottom of layout
		if (time.getHourOfDay() == END_HOUR)
			layoutParams.bottomMargin = HOUR_HEIGHT / 2;

		return layoutParams;
	}

	/**
	 * Layout the margins for a scheduleCell
	 *
	 * @param scheduleCell Cell for which the event is represented. MUST be child of {@link PercentRelativeLayout}
	 * @param event Event used to find height and top margin.
	 * @param slot The column the schedule cell is in. Range from [0, numSlots)
	 * @param numSlots Number of columns of schedule cells.
	 * @param eventForSlot Required to left side of current scheduleCell. newEventForSlot may have different cell to the left of the event.
	 * @param newEventForSlot Required to see how much more space current scheduleCell can expand to on its right side.
	 * @return LayoutParams for a scheduleCell
	 */
	private PercentRelativeLayout.LayoutParams scheduleCellMargins(View scheduleCell, Event event, int slot, int numSlots, SparseArray<Event> eventForSlot, SparseArray<Event> newEventForSlot)
	{
		PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) scheduleCell.getLayoutParams();

		//left margin
		if (slot == 0)
			layoutParams.addRule(PercentRelativeLayout.ALIGN_PARENT_LEFT);
		else
			layoutParams.addRule(PercentRelativeLayout.RIGHT_OF, eventToId(eventForSlot.get(slot - 1)));

		//top margin
		layoutParams.addRule(PercentRelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.topMargin = (int) marginTopForStartTime(event.startTime);

		//width
		layoutParams.getPercentLayoutInfo().widthPercent = widthPercent(event, slot, numSlots, newEventForSlot);

		//height
		layoutParams.height = heightForEvent(event);

		return layoutParams;
	}

	private int slotForEvent(Event event, int numSlots, SparseArray<Event> eventForSlot)
	{
		for (int i = 0; i < numSlots; i++)
			if (canUseSlot(i, event, eventForSlot))
				return i;
		return numSlots;
	}
	private boolean areEventOverlaps(Event event, int numSlots, SparseArray<Event> eventForSlot)
	{
		for (int i = 0; i < numSlots; i++)
			if (!canUseSlot(i, event, eventForSlot))
				return true;
		return false;
	}
	private boolean canUseSlot(int slot, Event event, SparseArray<Event> eventForSlot)
	{
		return eventForSlot.get(slot) == null
				|| minutesBetween(event.endTime, eventForSlot.get(slot).startTime) >= 0
				|| minutesBetween(eventForSlot.get(slot).endTime, event.startTime) >= 0;
	}
	private float marginTopForStartTime(LocalTime startTime)
	{
		int minutesFrom7 = minutesBetween(HOURS.get(0), startTime);
		return ((float) HOUR_HEIGHT) / 60.0f * ((float) minutesFrom7) + ((float) HOUR_HEIGHT) / 2;
	}
	private float widthPercent(Event event, int slot, int numSlots, SparseArray<Event> eventForSlot)
	{
		float occupiedSlots = 1;
		int nextSlot = slot + 1;
		while (canUseSlot(nextSlot, event, eventForSlot) && nextSlot < numSlots)
		{
			occupiedSlots += 1;
			nextSlot += 1;
		}
		return occupiedSlots / ((float) numSlots);
	}
	private int heightForEvent(Event event)
	{
		int minutes = minutesBetween(event.startTime, event.endTime);
		double height = ((double) HOUR_HEIGHT) / 60.0 * ((double) minutes);
		return (int) height;
	}

	//hour - view id conversion. Required since view ids must be positive (hour 0 should have id 1)
	@IdRes
	private int hourToId(int hour)
	{
		return hour + 1;
	}
	@IdRes
	private int eventToId(Event event)
	{
		return event.hashCode() + 31;
	}
	private Event idToEvent(int id)
	{
		int pk = id - 31;
		return pkToEvent.get(pk);
	}

	private int minutesBetween(LocalTime startTime, LocalTime endTime)
	{
		//check if we're crossing over the midnight mark. If we are, reverse the minutes
		if (endTime.getHourOfDay() < START_HOUR && startTime.getHourOfDay() >= START_HOUR)      //Ex: 23:00 to 1:00 = 2hr
			return 24*60 - Minutes.minutesBetween(endTime, startTime).getMinutes();
		else if (startTime.getHourOfDay() < START_HOUR && endTime.getHourOfDay() >= START_HOUR) //Ex: 1:00 to 23:00 = -2hr
			return Minutes.minutesBetween(endTime, startTime).getMinutes();
		else
			return Minutes.minutesBetween(startTime, endTime).getMinutes();
	}
	@Override
	public void onClick(View v)
	{
		Event event = idToEvent(v.getId());
		if (event == null)
		{
			Log.e(TAG, "onClick: non-scheduleCell view clicked");
			return;
		}

		//start details
		DetailsActivity.event = event;
		startActivity(new Intent(getContext(), DetailsActivity.class));
	}

	@Subscribe
	public void onDateChanged(NotificationCenter.EventDateSelected eventDateSelected)
	{
		redrawEvents();
	}
	@Subscribe
	public void onEventSelectionChanged(NotificationCenter.EventSelectionChanged eventSelectionChanged)
	{
		redrawEvents();
	}
	@Subscribe
	public void onEventReload(NotificationCenter.EventReload eventReload)
	{
		redrawEvents();
	}
	private void redrawEvents()
	{
		eventsContainer.removeAllViews();
		drawCells();
	}
}
