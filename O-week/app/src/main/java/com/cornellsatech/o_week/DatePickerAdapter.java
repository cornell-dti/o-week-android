package com.cornellsatech.o_week;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.eventbus.Subscribe;

/**
 * Displays the dates that the user can select to see his/her events on that particular date.
 *
 * {@link #selectedCell}: The cell that was last selected (by user or by machine when the app launched).
 *                        This cell is deselected once a new cell is selected. This will then point
 *                        to the newly selected cell. Note that since cells ARE recycled in a
 *                        {@link RecyclerView}, this is usually not a great idea. However, most dates
 *                        are usually visible to the user in our case, which means they shouldn't be
 *                        recycled.
 *
 * @see DateCell
 */
public class DatePickerAdapter extends RecyclerView.Adapter<DateCell>
{
	@Nullable
	private DateCell selectedCell;

	/**
	 * Registers this object to listen in on notifications when a {@link DateCell} is selected.
	 * Unregisters itself in {@link #onDetachedFromRecyclerView(RecyclerView)} to avoid memory leaks.
	 */
	public DatePickerAdapter()
	{
		NotificationCenter.DEFAULT.register(this);
	}

	/**
	 * Creates a {@link DateCell} to display dates by inflating it from {@link R.layout#cell_date_picker}.
	 *
	 * @param parent {@inheritDoc}
	 * @param viewType {@inheritDoc}
	 * @return {@link DateCell}
	 */
	@Override
	public DateCell onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new DateCell(inflater.inflate(R.layout.cell_date_picker, parent, false));
	}

	/**
	 * Updates a {@link DateCell} with the new {@link org.joda.time.LocalDate} it should represent.
	 * @param holder {@inheritDoc}
	 * @param position {@inheritDoc}
	 */
	@Override
	public void onBindViewHolder(DateCell holder, int position)
	{
		holder.configure(UserData.DATES.get(position));
	}

	/**
	 * Returns total number of dates that will be shown in the {@link RecyclerView}.
	 * @return Size of {@link UserData#DATES}
	 */
	@Override
	public int getItemCount()
	{
		return UserData.DATES.size();
	}

	/**
	 * Listens for {@link DateCell} click events, then {@link DateCell#unClick()}s the previously
	 * selected cell. Also updates {@link UserData#selectedDate}.
	 *
	 * @param event Event object containing the selected {@link DateCell}
	 */
	@Subscribe
	public void onItemClick(NotificationCenter.EventDateSelected event)
	{
		if (selectedCell != null)
			selectedCell.unClick();
		selectedCell = event.selectedCell;
		UserData.selectedDate = selectedCell.getDate();
	}
	/**
	 * Unregisters this object as a listener to avoid memory leaks. Registered in {@link #DatePickerAdapter()}.
	 * @param recyclerView Ignored.
	 */
	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		NotificationCenter.DEFAULT.unregister(this);
	}
}
