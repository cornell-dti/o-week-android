package com.cornellsatech.o_week;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Displays the dates that the user can select to see his/her events on that particular date.
 * @see DateCell
 */
public class DatePickerAdapter extends RecyclerView.Adapter<DateCell>
{
	/**
	 * Creates a {@link DateCell} to display dates by inflating it from {@link com.cornellsatech.o_week.R.layout#cell_date_picker}.
	 *
	 * @param parent {@inheritDoc}
	 * @param viewType {@inheritDoc}
	 * @return {@link DateCell}
	 */
	@NonNull
	@Override
	public DateCell onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
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
	public void onBindViewHolder(@NonNull DateCell holder, int position)
	{
		holder.setIsRecyclable(false);
		holder.configure(UserData.sortedDates.get(position));
	}

	/**
	 * Returns total number of dates that will be shown in the {@link RecyclerView}.
	 * @return Size of {@link UserData#sortedDates}
	 */
	@Override
	public int getItemCount()
	{
		return UserData.sortedDates.size();
	}
}
