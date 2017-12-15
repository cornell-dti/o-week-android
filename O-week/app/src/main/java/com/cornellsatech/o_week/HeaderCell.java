package com.cornellsatech.o_week;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cornellsatech.o_week.models.Event;

/**
 * HeaderCell for Search Page
 */

public class HeaderCell extends RecyclerView.ViewHolder {

    private final TextView titleTextView;
    private final Context context;
    private Event event;

    /**
     * Stores pointers to all the subviews and sets up listeners.
     * @param itemView {@inheritDoc}
     */
    public HeaderCell(View itemView)
    {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.headerTextView);
        context = itemView.getContext();
    }

    public void configure(boolean isFirstHeader) {
        if(isFirstHeader) {
            titleTextView.setText("My Calendar");
        }
        titleTextView.setText("All Events");
    }




}
