package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.eventbus.Subscribe;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedCell>
{
	private List<Event> events;
	private List<Event> selectedEvents;

	public FeedAdapter()
	{
		NotificationCenter.DEFAULT.register(this);
		events = UserData.allEvents.get(UserData.selectedDate);
		selectedEvents = UserData.selectedEvents.get(UserData.selectedDate);
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
	public void onDateChanged(NotificationCenter.EventDateSelected event)
	{
		events = UserData.allEvents.get(UserData.selectedDate);
		selectedEvents = UserData.selectedEvents.get(UserData.selectedDate);
		notifyDataSetChanged();
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView)
	{
		NotificationCenter.DEFAULT.unregister(this);
	}
}
