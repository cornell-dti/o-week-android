package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedCell>
{
	private List<Event> events;
	private List<Event> selectedEvents;

	public FeedAdapter()
	{
		NotificationCenter.DEFAULT.register(this);
		loadData();
	}
	@Override
	public FeedCell onCreateViewHolder(ViewGroup parent, int i)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new FeedCell(inflater.inflate(R.layout.cell_feed, parent, false));
	}
	@Override
	public void onBindViewHolder(FeedCell viewHolder, int i)
	{
		Event event = events.get(i);
		viewHolder.configure(event, selectedEvents.contains(event));
	}
	@Override
	public int getItemCount()
	{
		return events.size();
	}

	@Subscribe
	public void onSelectionChanged(NotificationCenter.EventSelectionChanged eventSelectionChanged)
	{
		Event event = eventSelectionChanged.event;
		int index = events.indexOf(event);
		notifyItemChanged(index);
	}
	@Subscribe
	public void onDateChanged(NotificationCenter.EventDateSelected eventDateSelected)
	{
		loadData();
		notifyDataSetChanged();
	}
	@Subscribe
	public void onEventReload(NotificationCenter.EventReload eventReload)
	{
		loadData();
		notifyDataSetChanged();
	}
	@Subscribe
	public void onFilterChanged(NotificationCenter.EventFilterChanged eventFilterChanged)
	{
		loadData();
		notifyDataSetChanged();
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		NotificationCenter.DEFAULT.unregister(this);
	}

	private void loadData()
	{
		//shallow copy of arrays. This is so nothing is deleted from UserData
		events = new ArrayList<>(UserData.allEvents.get(UserData.selectedDate));
		selectedEvents = new ArrayList<>(UserData.selectedEvents.get(UserData.selectedDate));
		applyFilters();
	}
	private void applyFilters()
	{
		switch (UserData.selectedFilterIndex)
		{
			case 0: //show all events, don't do anything special
				break;
			case 1: //show required events
				filterRequired(events);
				filterRequired(selectedEvents);
				break;
			default:
				Category category = UserData.categories.get(UserData.selectedFilterIndex - 2);
				filterForCategory(category.pk, events);
				filterForCategory(category.pk, selectedEvents);
				break;
		}
	}
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
