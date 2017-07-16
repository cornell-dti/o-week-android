package com.cornellsatech.o_week;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Holds data and reference pointers to {@link View}s for an orientation date button. Its physical
 * representation is in {@link R.layout#cell_date_picker}.
 *
 * Fields:
 * {@link #configured}: True if the method {@link #configure(LocalDate)} had ever been called.
 *                      Used for determining whether a default orientation date should be selected,
 *                      which should only be done once (on app launch).
 *                      More on that in {@link #configure(LocalDate)}.
 * {@link #date}: The date that this object currently represents.
 *
 * @see DatePickerAdapter
 */
public class DateCell extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private boolean configured = false;
	private TextView dayNum;
	private TextView weekDay;
	private LocalDate date;

	/**
	 * Stores pointers to all the subviews and sets up a {@link View.OnClickListener}.
	 * @param view The {@link View} that is this cell.
	 */
	public DateCell(View view)
	{
		super(view);
		dayNum = (TextView) view.findViewById(R.id.dayNumView);
		weekDay = (TextView) view.findViewById(R.id.weekDayView);
		view.setOnClickListener(this);
	}

	/**
	 * Sets the {@link #date} that this cell will represent, and updates the text it is displaying.
	 * If this is the first time this cell has been set up, {@link #configured} will be false.
	 * We will then attempt to computationally select this cell if its {@link #date} matches
	 * {@link UserData#selectedDate}. If this is NOT the first time this cell has been set up,
	 * {@link #configured} will be true, and a cell should already be selected.
	 *
	 * @param date The {@link LocalDate} that this cell will represent as long as it is visible.
	 */
	public void configure(LocalDate date)
	{
		this.date = date;
		dayNum.setText(Integer.toString(date.getDayOfMonth()));
		weekDay.setText(DateTimeFormat.forPattern("E").print(date).toUpperCase());

		if (!configured)
			if (UserData.selectedDate.isEqual(date))
				onClick(dayNum);
		configured = true;
	}

	/**
	 * Returns the {@link #date} this cell currently represents.
	 * @return {@link #date}
	 */
	@Nullable
	public LocalDate getDate()
	{
		return date;
	}
	/**
	 * Selects this cell, changing its colors and propagating an event for listeners to catch. The
	 * event ({@link NotificationCenter.EventDateSelected}) will contain a reference to this object,
	 * which is useful should the listener wish to access the date selected through {@link #getDate()}.
	 *
	 * @param v Ignored.
	 * @see DatePickerAdapter#onItemClick(NotificationCenter.EventDateSelected)
	 */
	@Override
	public void onClick(View v)
	{
		//can't double select
		if (dayNum.isSelected())
			return;
		dayNum.setSelected(true);
		dayNum.setTextColor(Color.BLACK);
		NotificationCenter.DEFAULT.post(new NotificationCenter.EventDateSelected(this));
	}
	/**
	 * Deselects this cell, reverting its colors. Called by {@link DatePickerAdapter} so that only
	 * one cell looks like it's selected at any time.
	 *
	 * @see #onClick(View)
	 */
	public void unClick()
	{
		dayNum.setSelected(false);
		dayNum.setTextColor(Color.WHITE);
	}
}
