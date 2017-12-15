package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Display search results under two headers: My Calendar & All Events
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Integer> headerCellIndexes;
    private final static int HEADER_VIEW_FIRST = 0;
    private final static int HEADER_VIEW_SECOND = 1;
    private final static int CONTENT_VIEW = 2;
    public List<Event> events;
    private static final String TAG = SearchAdapter.class.getSimpleName();

    public SearchAdapter(List<Integer> headerCellIndexes)
    {
        this.headerCellIndexes = headerCellIndexes;
        NotificationCenter.DEFAULT.register(this);
        loadData();
    }

    /**
     * Depending on type, returns a {@link HeaderCell} or {@link FeedCell} to be displayed
     *
     * @param parent {@inheritDoc}
     * @param position {@inheritDoc}
     * @return {@link FeedCell} or {@link HeaderCell}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int viewType = getItemViewType(position);
        switch(viewType) {
            case HEADER_VIEW_FIRST:
                return new HeaderCell(inflater.inflate(R.layout.cell_header, parent, false));
            case HEADER_VIEW_SECOND:
                return new HeaderCell(inflater.inflate(R.layout.cell_header, parent, false));
            case CONTENT_VIEW:
                return new FeedCell(inflater.inflate(R.layout.cell_feed, parent, false));
            default:
                return null;
        }
    }


    /**
     * Determines first and second header from headerCellIndexes and returns integer associated with
     * the type of cell (Header or Feed cell)
     * @param position
     * @return integer associated with the type of cell (Header or Feed cell)
     */
    @Override
    public int getItemViewType(int position) {
        int headerIndexMin = Collections.min(headerCellIndexes);
        int headerIndexMax = Collections.max(headerCellIndexes);
        if (position == headerIndexMin) {
            return HEADER_VIEW_FIRST;
        } else if (position == headerIndexMax) {
            return HEADER_VIEW_SECOND;
        } else {
            return CONTENT_VIEW;
        }
    }

    /**
     * Based on type, configures Header cell with correct title or Feed cell
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch(viewType) {
            case HEADER_VIEW_FIRST:
                HeaderCell headerCell = (HeaderCell) viewHolder;
                headerCell.configure(true);
                break;
            case HEADER_VIEW_SECOND:
                HeaderCell headerCell2 = (HeaderCell) viewHolder;
                headerCell2.configure(false);
                break;
            case CONTENT_VIEW:
                Event event = events.get(position);
                FeedCell feedCell = (FeedCell) viewHolder;
                feedCell.configure(event, UserData.selectedEventsContains(event));
                break;
            default:
                break;
        }
    }

    /**
     * Returns total number of cells (# of {@link #events} + # of header cells)
     * that will be shown in the {@link RecyclerView}.
     * @return Total number of cells
     */
    @Override
    public int getItemCount() {
        return events.size() + headerCellIndexes.size();
    }

    /**
     * Listens for event selections or de-selections. Updates only the event that has been changed.
     *
     * @param eventSelectionChanged Event object containing the selected {@link Event}
     */
    @Subscribe
    public void onSelectionChanged(NotificationCenter.EventSelectionChanged eventSelectionChanged)
    {
        Event event = eventSelectionChanged.event;
        int index = events.indexOf(event);
        notifyItemChanged(index);
    }
    /**
     * Listens for updates to the list from the database. We don't know which events were updated, so
     * the entire list is swapped out.
     *
     * @param eventReload Ignored.
     */
    @Subscribe
    public void onEventReload(NotificationCenter.EventReload eventReload)
    {
        loadData();
        notifyDataSetChanged();
    }
    /**
     * Unregisters this object as a listener to avoid memory leaks. Registered in {@link #FeedAdapter()}.
     * @param recyclerView Ignored.
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        NotificationCenter.DEFAULT.unregister(this);
    }

    /**
     * Saves shallow copies of lists of events from {@link UserData#allEvents} and {@link UserData#selectedEvents}.
     * The copies allow list manipulation of which events are displayed depending on the user's filter.
     */
    private void loadData()
    {
        events = new ArrayList<>(UserData.allEvents.get(UserData.selectedDate));
        applyFilters();
    }

    /**
     * Depending on the filter the user applied, filter the events shown.
     *
     * @see UserData for documentation on {@link UserData#selectedFilterIndex}
     */
    private void applyFilters()
    {
        switch (UserData.selectedFilterIndex)
        {
            case 0: //show all events, don't do anything special
                break;
            case 1: //show required events
                filterRequired(events);
                break;
            default:
                Category category = UserData.categories.get(UserData.selectedFilterIndex - 2);
                filterForCategory(category.pk, events);
                break;
        }
    }

    /**
     * Removes all events that are not required.
     * @param events The list of events to remove from.
     */
    private void filterRequired(Collection<Event> events)
    {
        Iterator<Event> eventsIterator = events.iterator();
        while (eventsIterator.hasNext())
        {
            Event event = eventsIterator.next();
            if (!event.required)
                eventsIterator.remove();
        }
    }

    /**
     * Removes all events that are not part of the given category.
     * @param category {@link Category#pk}
     * @param events The list of events to remove from.
     */
    private void filterForCategory(int category, Collection<Event> events)
    {
        Iterator<Event> eventsIterator = events.iterator();
        while (eventsIterator.hasNext())
        {
            Event event = eventsIterator.next();
            if (event.category != category)
                eventsIterator.remove();
        }
    }
}
