package com.cornellsatech.o_week;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.eventbus.Subscribe;

/**
 * Displays a list of events, ordered chronologically. This is a {@link Fragment} so that it can be
 * easily swapped out with {@link ScheduleFragment} while keeping the same date picker up top.
 * Layout in {@link R.layout#fragment_feed}
 */
public class FeedFragment extends Fragment
{
	private RecyclerView feedRecycler;
	private FeedAdapter feedAdapter;

	/**
	 * Connects views, sets up recycler, registers as listener. Unregisters in {@link #onDestroyView()}.
	 * Listens for event clicks and opens {@link DetailsActivity} upon a click.
	 *
	 * @param inflater {@inheritDoc}
	 * @param container {@inheritDoc}
	 * @param savedInstanceState {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_feed, container, false);
		NotificationCenter.DEFAULT.register(this);

		feedRecycler = (RecyclerView) view.findViewById(R.id.feedRecycler);
		setUpRecycler();
		return view;
	}

	/**
	 * Unregisters as listener to prevent memory leaks. Also triggers {@link FeedAdapter}'s un-registration.
	 * Registered in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 */
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
		//detach the adapter so that its onDestroy methods trigger
		feedRecycler.setAdapter(null);
	}
	/**
	 * Listens for {@link FeedCell#onClick(View)}. Starts {@link DetailsActivity} featuring the {@link Event}
	 * that was clicked upon the click.
	 *
	 * @param eventEventClicked Event that holds the {@link Event} clicked
	 */
	@Subscribe
	public void onEventClicked(NotificationCenter.EventEventClicked eventEventClicked)
	{
		DetailsActivity.event = eventEventClicked.event;
		startActivity(new Intent(getContext(), DetailsActivity.class));
	}
	/**
	 * Connects {@link #feedRecycler} to {@link #feedAdapter}.
	 */
	private void setUpRecycler()
	{
		feedAdapter = new FeedAdapter();
		feedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		feedRecycler.setAdapter(feedAdapter);
	}
}
