package com.cornellsatech.o_week;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * HeaderCell for Search Page
 */

public class HeaderCell extends RecyclerView.ViewHolder
{

	private final TextView titleTextView;

	/**
	 * Initializes header cell with title
	 */
	public HeaderCell(View itemView)
	{
		super(itemView);
		titleTextView = itemView.findViewById(R.id.headerTextView);
	}

	/**
	 * If first header, set title to {@link com.cornellsatech.o_week.R.string#header_my_calendar}.
	 * Else, set title to {@link com.cornellsatech.o_week.R.string#header_all_events}
	 */
	public void configure(boolean isFirstHeader)
	{
		if (isFirstHeader)
			titleTextView.setText(R.string.header_my_calendar);
		else
			titleTextView.setText(R.string.header_all_events);
	}
}
