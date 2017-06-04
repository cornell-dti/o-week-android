package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.joda.time.LocalTime;

import java.util.List;

public class ScheduleFragment extends Fragment
{
	private RelativeLayout scheduleContainer;
	private static final List<LocalTime> HOURS;
	private static final int START_HOUR = 7;    //Hours range: [START_HOUR, END_HOUR], inclusive
	private static final int END_HOUR = 2;      //Note: Hours wrap around, from 7~23, then 0~2

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
		View view = inflater.inflate(R.layout.fragment_schedule, container, false);
		scheduleContainer = (RelativeLayout) view.findViewById(R.id.scheduleContainer);
		drawTimeLines();
		return view;
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
			scheduleContainer.addView(timeLine, timeLineMargins(timeLine));
		}
	}
	private RelativeLayout.LayoutParams timeLineMargins(View timeLine)
	{
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
		int hour = idToHour(timeLine.getId());

		//set top of layout
		if (hour == START_HOUR)
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		//hour 0 should be beneath hour 23
		else if (hour == 0)
			layoutParams.addRule(RelativeLayout.BELOW, hourToId(23));
		else
			layoutParams.addRule(RelativeLayout.BELOW, hourToId(hour - 1));

		layoutParams.topMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.distance_between_time_lines);

		//set bottom of layout
		if (hour == END_HOUR)
			layoutParams.bottomMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.distance_between_time_lines);

		return layoutParams;
	}

	//hour - view id conversion. Required since view ids must be positive (hour 0 should have id 1)
	private int idToHour(@IdRes int id)
	{
		return id - 1;
	}
	@IdRes
	private int hourToId(int hour)
	{
		return hour + 1;
	}
}
