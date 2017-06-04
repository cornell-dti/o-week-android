package com.cornellsatech.o_week;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

public class DateCell extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private boolean configured = false;
	private TextView dayNum;
	private TextView weekDay;
	private LocalDate date;

	public DateCell(View view)
	{
		super(view);
		dayNum = (TextView) view.findViewById(R.id.dayNumView);
		weekDay = (TextView) view.findViewById(R.id.weekDayView);
		view.setOnClickListener(this);
	}

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

	@Nullable
	public LocalDate getDate()
	{
		return date;
	}

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

	public void unClick()
	{
		dayNum.setSelected(false);
		dayNum.setTextColor(Color.WHITE);
	}
}
