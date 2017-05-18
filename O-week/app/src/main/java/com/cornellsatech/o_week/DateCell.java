package com.cornellsatech.o_week;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

public class DateCell extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private TextView dayNum;
	private TextView weekDay;

	public DateCell(View view)
	{
		super(view);
		dayNum = (TextView) view.findViewById(R.id.dayNumView);
		weekDay = (TextView) view.findViewById(R.id.weekDayView);
		view.setOnClickListener(this);
	}

	public void configure(LocalDate date)
	{
		dayNum.setText(Integer.toString(date.getDayOfMonth()));
		weekDay.setText(DateTimeFormat.forPattern("E").print(date).toUpperCase());
	}


	@Override
	public void onClick(View v)
	{
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
