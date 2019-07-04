package com.cornellsatech.o_week;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cornellsatech.o_week.util.NotificationCenter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Holds data and reference pointers to {@link View}s for an orientation date button. Its physical
 * representation is in {@link com.cornellsatech.o_week.R.layout#cell_date_picker}.
 *
 * {@link #date}: The date that this object currently represents.
 *
 * @see DatePickerAdapter
 */
public class DateCell extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private final TextView dayNum;
	private final TextView weekDay;
	private LocalDate date;

	/**
	 * Stores pointers to all the subviews and sets up a {@link View.OnClickListener}.
	 * @param view {@inheritDoc}.
	 */
	public DateCell(View view)
	{
		super(view);
		dayNum = view.findViewById(R.id.dayNumView);
		weekDay = view.findViewById(R.id.weekDayView);
		view.setOnClickListener(this);
	}

	/**
	 * Sets the {@link #date} that this cell will represent, and updates the text it is displaying.
	 *
	 * @param date The {@link LocalDate} that this cell will represent as long as it is visible.
	 */
	public void configure(LocalDate date)
	{
		this.date = date;
		dayNum.setText(date.toString("dd"));
		weekDay.setText(DateTimeFormat.forPattern("E").print(date).toUpperCase());

		if (UserData.selectedDate.isEqual(date))
			setClick();
		else
			unClick();
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
	 * event ({@link NotificationCenter.EventDateChanged}) will contain a reference to this date.
	 *
	 * @param v Ignored.
	 * @see MainActivity#onDateChanged(NotificationCenter.EventDateChanged)
	 */
	@Override
	public void onClick(View v)
	{
		//can't double select
		if (dayNum.isSelected())
			return;
		setClick();
		UserData.selectedDate = date;
		NotificationCenter.DEFAULT.post(new NotificationCenter.EventDateChanged());
	}

	/**
	 * Selects this cell & changes its colors.
	 *
	 * @see #unClick()
	 */
	private void setClick()
	{
		dayNum.setSelected(true);
		dayNum.setTextColor(Color.BLACK);
	}
	/**
	 * Deselects this cell, reverting its colors.
	 *
	 * @see #setClick()
	 */
	private void unClick()
	{
		dayNum.setSelected(false);
		dayNum.setTextColor(Color.WHITE);
	}
}
