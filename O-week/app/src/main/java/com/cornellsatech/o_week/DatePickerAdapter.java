package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.eventbus.Subscribe;

import org.joda.time.LocalDate;

public class DatePickerAdapter extends RecyclerView.Adapter<DateCell>
{
	LocalDate[] sortedDates = new LocalDate[UserData.dates.size()];
	DateCell selectedCell;

	public DatePickerAdapter()
	{
		ImmutableSortedSet.copyOf(UserData.dates).toArray(sortedDates);
		NotificationCenter.DEFAULT.register(this);
	}

	@Override
	public DateCell onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		return new DateCell(inflater.inflate(R.layout.cell_date_picker, parent, false));
	}

	@Override
	public void onBindViewHolder(DateCell holder, int position)
	{
		holder.configure(sortedDates[position]);
	}

	@Override
	public int getItemCount()
	{
		return sortedDates.length;
	}

	@Subscribe
	public void onItemClick(NotificationCenter.EventDateSelected event)
	{
		if (selectedCell != null)
			selectedCell.unClick();
		selectedCell = event.selectedCell;
	}
}
