package com.cornellsatech.o_week;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;

/**
 * Holds data and reference pointers to {@link View}s for an {@link Event}. Its physical representation
 * is in {@link R.layout#cell_feed}.
 *
 * {@link #event}: The event that this object currently represents.
 * {@link #context}: To be used in {@link #onCheckedChanged(CompoundButton, boolean)}
 *
 * @see FeedAdapter
 */
public class FeedCell extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private final TextView startTimeText;
	private final TextView endTimeText;
	private final TextView titleText;
	private final TextView captionText;
	private final CheckBox checkBox;
	private final Context context;
	private Event event;
	private static final String TAG = FeedCell.class.getSimpleName();

	/**
	 * Stores pointers to all the subviews and sets up listeners.
	 * @param itemView {@inheritDoc}
	 */
	public FeedCell(View itemView)
	{
		super(itemView);
		startTimeText = (TextView) itemView.findViewById(R.id.startTimeText);
		endTimeText = (TextView) itemView.findViewById(R.id.endTimeText);
		titleText = (TextView) itemView.findViewById(R.id.titleText);
		captionText = (TextView) itemView.findViewById(R.id.captionText);
		checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
		checkBox.setOnClickListener(this);
		itemView.setOnClickListener(this);
		context = itemView.getContext();
	}
	/**
	 * Sets the current event to display. Time display formats are specified here: {@link Event#DISPLAY_TIME_FORMAT}.
	 *
	 * @param event The {@link Event} this cell will represent as long as it is visible.
	 * @param selected Whether this {@link #event} is selected.
	 */
	public void configure(Event event, boolean selected)
	{
		this.event = event;
		startTimeText.setText(event.startTime.toString(Event.DISPLAY_TIME_FORMAT));
		endTimeText.setText(event.endTime.toString(Event.DISPLAY_TIME_FORMAT));
		titleText.setText(event.title);
		captionText.setText(event.caption);
		checkBox.setChecked(selected);
	}
	/**
	 * This object has been clicked. List listeners know.
	 * @param v Clicked view.
	 */
	@Override
	public void onClick(View v)
	{
		if (v.getId() == checkBox.getId())  //checkBox was clicked
		{
			if (checkBox.isChecked())
			{
				UserData.insertToSelectedEvents(event);
				if (Notifications.shouldScheduleForEvent(event, context))
					Notifications.scheduleForEvent(event, context);
			}
			else
			{
				UserData.removeFromSelectedEvents(event);
				Notifications.unscheduleForEvent(event, context);
			}

		}
		else    //entire view was clicked
			NotificationCenter.DEFAULT.post(new NotificationCenter.EventEventClicked(event));
	}
}
