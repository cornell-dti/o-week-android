package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FeedFragment extends Fragment
{
	private RecyclerView feedRecycler;
	private FeedAdapter feedAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_feed, container, false);
		feedRecycler = (RecyclerView) view.findViewById(R.id.feedRecycler);

		setUpRecycler();
		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		//detach the adapter so that its onDestroy methods trigger
		feedRecycler.setAdapter(null);
	}

	private void setUpRecycler()
	{
		feedAdapter = new FeedAdapter();
		feedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		feedRecycler.setAdapter(feedAdapter);
	}
}
