package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cornellsatech.o_week.util.NotificationCenter;

/**
 * Displays the dates that the user can select to see his/her events on that particular date.
 * @see DateCell
 */
public class DatePickerAdapter extends RecyclerView.Adapter<DateCell>
{
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
		holder.setIsRecyclable(false);
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
}
