package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.eventbus.Subscribe;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Displays events for a given day in a list.
 *
 * {@link #events}: All events on the {@link UserData#selectedDate}. However, this list may be filtered
 *                  by category, so we must keep this list from mutating {@link UserData#allEvents}.
 *
 * @see FeedCell
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedCell>
{
	private final LocalDate date;
	public List<Event> events;
	private final View emptyView;

	private static final String TAG = FeedAdapter.class.getSimpleName();

	/**
	 * Registers this object to listen in on notifications.
	 * Unregisters itself in {@link #onDetachedFromRecyclerView(RecyclerView)} to avoid memory leaks.
	 */
	public FeedAdapter(LocalDate date, View emptyView)
	{
		NotificationCenter.DEFAULT.register(this);
		this.emptyView = emptyView;
		this.date = date;
		loadData();
	}
	/**
	 * Creates a {@link FeedCell} to display dates by inflating it from {@link R.layout#cell_feed}.
	 *
	 * @param parent {@inheritDoc}
	 * @param i {@inheritDoc}
	 * @return {@link FeedCell}
	 */
	@Override
	public FeedCell onCreateViewHolder(ViewGroup parent, int i)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new FeedCell(inflater.inflate(R.layout.cell_feed, parent, false));
	}
	/**
	 * Updates a {@link FeedCell} with the new {@link Event} it should represent, including whether
	 * or not that event is selected by the user.
	 *
	 * @param viewHolder {@inheritDoc}
	 * @param position {@inheritDoc}
	 */
	@Override
	public void onBindViewHolder(FeedCell viewHolder, int position)
	{
		Event event = events.get(position);
		viewHolder.configure(event);
	}
	/**
	 * Returns total number of events that will be shown in the {@link RecyclerView}.
	 * @return Size of {@link #events}
	 */
	@Override
	public int getItemCount()
	{
		return events.size();
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
	}
	/**
	 * Listens for a new filter the user chose. Reloads all events.
	 *
	 * @param eventFilterChanged Ignored.
	 */
	@Subscribe
	public void onFilterChanged(NotificationCenter.EventFilterChanged eventFilterChanged)
	{
		loadData();
	}
	/**
	 * Unregisters this object as a listener to avoid memory leaks. Registered in {@link FeedAdapter()}.
	 * @param recyclerView Ignored.
	 */
	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		NotificationCenter.DEFAULT.unregister(this);
	}
	/**
	 * Saves shallow copies of lists of events from {@link UserData#allEvents}.
	 * The copies allow list manipulation of which events are displayed depending on the user's filter.
	 */
	private void loadData()
	{
		List<Event> events = new ArrayList<>(UserData.allEvents.get(date));
		filterEvents(events);

		if (events.isEmpty())
			emptyView.setVisibility(View.VISIBLE);
		else
			emptyView.setVisibility(View.GONE);

		this.events = events;
		notifyDataSetChanged();
	}

	/**
	 * Removes all events that are not part of the given category or required (if that option is selected).
	 * @param events The list of events to remove from.
	 */
	private void filterEvents(Collection<Event> events)
	{
		if (UserData.selectedFilters.isEmpty() && !UserData.filterRequired)
			return;

		Iterator<Event> eventsIterator = events.iterator();
		while (eventsIterator.hasNext())
		{
			Event event = eventsIterator.next();

			if (UserData.selectedFilters.contains(event.category))
				continue;
			if (UserData.filterRequired && UserData.requiredForUser(event))
				continue;
			eventsIterator.remove();
		}
	}
}
