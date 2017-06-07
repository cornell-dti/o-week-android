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

public class FeedFragment extends Fragment
{
	private RecyclerView feedRecycler;
	private FeedAdapter feedAdapter;

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

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
		//detach the adapter so that its onDestroy methods trigger
		feedRecycler.setAdapter(null);
	}

	@Subscribe
	public void onEventClicked(NotificationCenter.EventEventClicked eventEventClicked)
	{
		DetailsActivity.event = eventEventClicked.event;
		startActivity(new Intent(getContext(), DetailsActivity.class));
	}

	private void setUpRecycler()
	{
		feedAdapter = new FeedAdapter();
		feedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		feedRecycler.setAdapter(feedAdapter);
	}
}
