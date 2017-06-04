package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.eventbus.Subscribe;

public class DatePickerAdapter extends RecyclerView.Adapter<DateCell>
{
	private DateCell selectedCell;

	public DatePickerAdapter()
	{
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
		holder.configure(UserData.dates.get(position));
	}

	@Override
	public int getItemCount()
	{
		return UserData.dates.size();
	}

	@Subscribe
	public void onItemClick(NotificationCenter.EventDateSelected event)
	{
		if (selectedCell != null)
			selectedCell.unClick();
		selectedCell = event.selectedCell;
		UserData.selectedDate = selectedCell.getDate();
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		NotificationCenter.DEFAULT.unregister(this);
	}
}
