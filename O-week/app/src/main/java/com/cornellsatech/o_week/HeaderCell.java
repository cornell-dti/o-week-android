package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * HeaderCell for Search Page
 */

public class HeaderCell extends RecyclerView.ViewHolder {

    private final TextView titleTextView;

    /**
     * Initializes header cell with title
     * @param itemView {@inheritDoc}
     */
    public HeaderCell(View itemView)
    {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.headerTextView);
    }

    /**
     * If first header, set title to {@link R.string#header_my_calendar}.
     * Else, set title to {@link R.string#header_all_events}
     * @param isFirstHeader
     */
    public void configure(boolean isFirstHeader) {
        if(isFirstHeader) {
            titleTextView.setText(R.string.header_my_calendar);
        } else {
            titleTextView.setText(R.string.header_all_events);
        }
    }




}
