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

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Displays {@link Event}s with height proportional to the event's length, laying them side by side
 * should their times overlap.
 *
 * Notes on how view IDs are numbered in this class:
 * 1. For time lines: ID = hour the time line represents + 1
 * 2. For events: the event's hashcode
 * IDs are required for views to specify their positions relative to each other. A structured ID scheme
 * allows us to know which IDs correspond to which events.
 *
 * {@link #scheduleContainer}: Holds all time lines, and the {@link #eventsContainer}. Never redrawn.
 * {@link #eventsContainer}: Holds all events. Redrawn whenever a date changes or an event is selected
 *                           or unselected. Separated from {@link #scheduleContainer} so time lines
 *                           are not also redrawn every time, saving processing power.
 * {@link #pkToEvent}: Returns the {@link Event} for the given {@link Event#pk}. Used in {@link #idToEvent(int)},
 *                     which helps the app determine the event a user clicks.
 * {@link #HOUR_HEIGHT}: The height (dp) of an event that spans 1 hour.
 * {@link #HOUR_TEXT_HEIGHT}: The size (sp) or the hour text (for example: 1:00 PM). Used in calculation
 *                            to find the y position of an event for its start time.
 * {@link #HOURS}: List of hours to display. Should be the full range of start/end times for events.
 *                 Hours range: [START_HOUR, END_HOUR], inclusive. Hours wrap around, from 7~23, then 0~2.
 * {@link #START_HOUR}: The earliest hour an event can start.
 * {@link #END_HOUR}: The latest hour an event can end. Note that this is in AM; END_HOUR must < START_HOUR.
 */
public class ScheduleFragment extends Fragment implements View.OnClickListener
{
	private RelativeLayout scheduleContainer;
	private PercentRelativeLayout eventsContainer;
	private SparseArray<Event> pkToEvent = new SparseArray<>();
	private int HOUR_HEIGHT;
	private int HOUR_TEXT_HEIGHT;
	private static final List<LocalTime> HOURS;
	public static final int START_HOUR = 7;
	public static final int END_HOUR = 2;
	private LocalDate date;

	private static final String DATE_BUNDLE_KEY = "date";
	private static final String TAG = ScheduleFragment.class.getSimpleName();

	/**
	 * Fill {@link #HOURS} with {@link LocalTime}s according to {@link #START_HOUR} and {@link #END_HOUR}.
	 */
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

	/**
	 * Create an instance of {@link ScheduleFragment} with the given date.
	 * This should be the only way you create instances of {@link ScheduleFragment}.
	 *
	 * It passes the given date as a Bundle to {@link ScheduleFragment}.
	 *
	 * @param date The date the schedule will display.
	 * @return Instance of the schedule.
	 */
	public static ScheduleFragment newInstance(LocalDate date)
	{
		ScheduleFragment scheduleFragment = new ScheduleFragment();

		Bundle args = new Bundle();
		args.putSerializable(DATE_BUNDLE_KEY, date);
		scheduleFragment.setArguments(args);

		return scheduleFragment;
	}

	/**
	 * Sets up listener, associate views, retrieve final values from {@link R.dimen}, then draws everything.
	 * Retrieves {@link #date} from the bundle.
	 *
	 * @param inflater {@inheritDoc}
	 * @param container {@inheritDoc}
	 * @param savedInstanceState Ignored.
	 * @return {@inheritDoc}
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		NotificationCenter.DEFAULT.register(this);

		View view = inflater.inflate(R.layout.fragment_schedule, container, false);

		//retrieve date from bundle
		if (getArguments() != null)
			date = (LocalDate) getArguments().getSerializable(DATE_BUNDLE_KEY);
		else
			Log.e(TAG, "onCreateView: date not found");

		scheduleContainer = view.findViewById(R.id.scheduleContainer);
		eventsContainer = view.findViewById(R.id.eventsContainer);
		HOUR_HEIGHT = getActivity().getResources().getDimensionPixelSize(R.dimen.distance_between_time_lines);
		HOUR_TEXT_HEIGHT = getActivity().getResources().getDimensionPixelSize(R.dimen.size_hour_textview);
		drawTimeLines();
		drawCells();
		return view;
	}
	/**
	 * Unregister as listener to avoid memory leaks.
	 */
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
	}
	/**
	 * Draw all the time lines, one line for each hour in {@link #HOURS} and adds them to {@link #scheduleContainer}.
	 */
	private void drawTimeLines()
	{
		LayoutInflater inflater = getActivity().getLayoutInflater();
		for (LocalTime hour : HOURS)
		{
			View timeLine = inflater.inflate(R.layout.time_line, scheduleContainer, false);
			timeLine.setId(hourToId(hour.getHourOfDay()));    //id of view = hour
			TextView hourText = timeLine.findViewById(R.id.hourText);
			hourText.setText(hour.toString("h a")); //Ex: 11 AM
			scheduleContainer.addView(timeLine, timeLineMargins(timeLine, hour));
		}
	}
	/**
	 * Draws all selected events in order.
	 */
	private void drawCells()
	{
		List<Event> selectedEvents = UserData.selectedEvents.get(date);
		Collections.sort(selectedEvents);
		if (selectedEvents.isEmpty())
			return;
		drawEvent(1, new SparseArray<Event>(), new ArrayDeque<>(selectedEvents));
	}
	/**
	 * Recursive function. Each iteration draws an event and adds it to {@link #eventsContainer}.
	 *
	 * Terminology:
	 * Slot = column which events are assigned. Starts from 0.
	 *
	 * Specifically:
	 * 1. Finds the best slot to put this event in based on conflicts with other events. If all available
	 *    slots are full, creates a new slot. The slots in which events are placed are shouldActUpon to earlier
	 *    events in case its margins are updated (the new event shrinks the previous event's width when
	 *    a new slot is created).
	 * 2. Creates the event cell ({@link View}).
	 * 3. If there are more events, calculate their slots too. If the new event won't overlap with this
	 *    one, it automatically is assigned the entire width (numSlots = 1).
	 * 4. Sets event margins, text, listener. Stores in appropriate data structures.
	 * 5. Returns numSlots and eventForSlot to the parent with relevant new positioning info.
	 *
	 * @param numSlots Number of slots currently available to events. Starts from 1.
	 * @param eventForSlot A map of events occupying a given slot. Note: slots CAN be empty (return null).
	 * @param events The remaining events to position on screen.
	 * @return numSlots and eventForSlot to alert the previous event.
	 */
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
		TextView title = scheduleCell.findViewById(R.id.titleText);
		TextView caption = scheduleCell.findViewById(R.id.captionText);
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

	/**
	 * Set the margins of a time line.
	 * Top margin must be managed so that the time line is centered horizontally.
	 *
	 * @param timeLine View. Must be child of {@link RelativeLayout}
	 * @param time The hour the time line represents.
	 * @return Margins
	 */
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

	/**
	 * Returns the best slot for a given event based on the events that were already placed. The leftmost
	 * unoccupied slot will be chosen, unless all slots are occupied, in which case the slot returned
	 * will be out of range.
	 *
	 * @param event The event that we want to find a slot for.
	 * @param numSlots The current # of slots assigned to events. The more slots, the skinnier the
	 *                 events will appear. numSlot = 1 means each event takes up the entire width.
	 * @param eventForSlot The events that are currently assigned to each slot.
	 * @return The best slot to put this event in.
	 */
	private int slotForEvent(Event event, int numSlots, SparseArray<Event> eventForSlot)
	{
		for (int i = 0; i < numSlots; i++)
			if (canUseSlot(i, event, eventForSlot))
				return i;
		return numSlots;
	}
	/**
	 * Returns false if the given event can fit into every available slot. That means the event should
	 * be placed below, not next to, the current available events.
	 *
	 * @param event The event that we test every slot's event against.
	 * @param numSlots The current # of slots assigned to events.
	 * @param eventForSlot The events that are currently assigned to each slot.
	 * @return True if there exists a slot that this event cannot use.
	 */
	private boolean areEventOverlaps(Event event, int numSlots, SparseArray<Event> eventForSlot)
	{
		for (int i = 0; i < numSlots; i++)
			if (!canUseSlot(i, event, eventForSlot))
				return true;
		return false;
	}
	/**
	 * Returns true if the given event can be fitted into this slot. There are 3 ways this can happen:
	 * 1. The slot is empty.
	 * 2. The event in the slot ends before this event starts.
	 * 3. The event in the slot starts after this event ends.
	 *
	 * @param slot The slot we plan to put the event in.
	 * @param event The event we want to put in the slot.
	 * @param eventForSlot The events that are currently assigned to each slot.
	 * @return True if the event can use the slot.
	 */
	private boolean canUseSlot(int slot, Event event, SparseArray<Event> eventForSlot)
	{
		return eventForSlot.get(slot) == null
				|| minutesBetween(event.endTime, eventForSlot.get(slot).startTime) >= 0
				|| minutesBetween(eventForSlot.get(slot).endTime, event.startTime) >= 0;
	}
	/**
	 * Distance from the top for a given event based on its start time.
	 * @param startTime {@link Event#startTime}
	 * @return Distance from the top (in pixels)
	 */
	private float marginTopForStartTime(LocalTime startTime)
	{
		int minutesFrom7 = minutesBetween(HOURS.get(0), startTime);
		return ((float) HOUR_HEIGHT) / 60.0f * ((float) minutesFrom7) + ((float) HOUR_HEIGHT) / 2;
	}
	/**
	 * Returns how wide an event should be based on the number of slots and whether or not there are
	 * events to the right of this one that this would conflict with. An event will attempt to occupy
	 * all available space to its right.
	 *
	 * @param event The event to determine the widthPercent for.
	 * @param slot The slot the event occupies.
	 * @param numSlots The current # of slots assigned to events.
	 * @param eventForSlot The events that are currently assigned to each slot.
	 * @return The % of the width of the parent view that the event is allowed to take up.
	 */
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
	/**
	 * Returns how tall an event should be based on its length.
	 * @param event The event to determine the height for.
	 * @return Height of the event in pixels.
	 */
	private int heightForEvent(Event event)
	{
		int minutes = minutesBetween(event.startTime, event.endTime);
		double height = ((double) HOUR_HEIGHT) / 60.0 * ((double) minutes);
		return (int) height;
	}

	/**
	 * Converts an hour (that a time line represents) into the time line's view ID. This is required
	 * since view IDs must be positive (rule of Android), but we must accept hour 0.
	 * @param hour The hour a time line represents.
	 * @return The view ID of the time line.
	 */
	@IdRes
	private int hourToId(int hour)
	{
		return hour + 1;
	}
	/**
	 * Returns the view ID of an event, which is based on {@link Event#pk}, known to be unique. However,
	 * since view IDs 0 to 24 (+1) may already be occupied by hours, we must add 31 so the view IDs
	 * do not conflict.
	 *
	 * @param event Event that we want to know the view ID of.
	 * @return The view ID of the event.
	 */
	@IdRes
	private int eventToId(Event event)
	{
		return event.pk + 31;
	}
	/**
	 * Returns the event that is associated with the a view's ID.
	 * @param id The view ID of the scheduleCell.
	 * @return The event the cell represents.
	 */
	private Event idToEvent(int id)
	{
		int pk = id - 31;
		return pkToEvent.get(pk);
	}
	/**
	 * Returns the number of minutes between 2 given times. Note that this accounts for events that cross
	 * over midnight. An event that begins at 11PM and ends at 2AM lasts 3 hours, not -21.
	 * @param startTime Start
	 * @param endTime End
	 * @return Number of minutes in between.
	 */
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
	/**
	 * Opens {@link DetailsActivity} for an event if the event's corresponding scheduleCell was clicked.
	 * @param v {@inheritDoc}
	 */
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
		Intent intent = new Intent(getContext(), DetailsActivity.class);
		intent.putExtra(DetailsActivity.EVENT_KEY, event.toString());
		startActivity(intent);
	}
	/**
	 * Listener for the selection or deselection of an event. This means we might need to display more
	 * or fewer events.
	 * @param eventSelectionChanged Ignored.
	 */
	@Subscribe
	public void onEventSelectionChanged(NotificationCenter.EventSelectionChanged eventSelectionChanged)
	{
		redrawEvents();
	}
	/**
	 * Listener for an update from the database.
	 * @param eventReload Ignored.
	 */
	@Subscribe
	public void onEventReload(NotificationCenter.EventReload eventReload)
	{
		redrawEvents();
	}
	/**
	 * Removes all scheduleCells and redraws them.
	 */
	private void redrawEvents()
	{
		eventsContainer.removeAllViews();
		drawCells();
	}
}
