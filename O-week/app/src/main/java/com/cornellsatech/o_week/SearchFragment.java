package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cornellsatech.o_week.models.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Search Fragment that displays search results from my calender and feed.
 * This is a {@link Fragment} so that it can be  swapped out with {@link SearchFragment}
 * Layout in {@link R.layout#fragment_search}
 * {@link #MIN_NUM_LETTERS} Minimum letters that must be typed in search bar to cause a refresh
 */

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener
{
	private LinearLayout emptyView;
	private RecyclerView searchRecycler;
	private SearchAdapter searchAdapter;
	private static final int MIN_NUM_LETTERS = 3;

	/**
	 * Initialize search page to display empty view. Set up options menu & recycler.
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		emptyView = view.findViewById(R.id.searchEmptyState);

		searchRecycler = view.findViewById(R.id.searchRecycler);
		setUpRecycler();

		setHasOptionsMenu(true);

		return view;
	}


	/**
	 * Initialize search bar
	 *
	 * @param menu
	 * @param inflater
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_search, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(this);

		//Set placeholder text in search bar
		searchView.setQueryHint(getResources().getString(R.string.search_hint));

		//Makes search bar expands automatically when open Search page
		searchView.setIconified(false);
		searchView.clearFocus();
	}

	@Override
	public boolean onQueryTextSubmit(String queryText)
	{
		return true;
	}

	/**
	 * Display events that match search query
	 * Note: events list is the length of all events + header cells
	 *
	 * @param searchText
	 * @return
	 */
	@Override
	public boolean onQueryTextChange(String searchText)
	{

		//If what the user types is nothing or is less than MIN_NUM_LETTERS
		if (searchText == null || searchText.length() < MIN_NUM_LETTERS)
		{
			clearSearchPage();
			return false;
		}

		List<Event> events = new ArrayList<>();
		String searchTextLowercase = searchText.toLowerCase();

		//Add events that contains search text
		for (List<Event> listsOfEvents : UserData.allEvents.values())
		{
			for (Event event : listsOfEvents)
			{
				if (event.title.toLowerCase().contains(searchTextLowercase)
						|| event.description.toLowerCase().contains(searchTextLowercase)
						|| event.caption.toLowerCase().contains(searchTextLowercase)
						|| event.additional.toLowerCase().contains(searchTextLowercase))
					events.add(event);
			}
		}
		//If Events List is empty, remove headers
		if (events.isEmpty())
		{
			clearSearchPage();
			return false;
		}
		emptyView.setVisibility(View.GONE);

		//Separate events into user's events and non user events
		List<Event> userEvents = new ArrayList<>(events.size());
		List<Event> nonUserEvents = new ArrayList<>(events.size());

		for (Event event : events)
		{
			if (UserData.selectedEventsContains(event))
			{
				userEvents.add(event);
			}
			else
			{
				nonUserEvents.add(event);
			}
		}
		//Combine user's events & non-users events into 1 list
		//Note: Added null at header indices to make it easy to access events at correct index
		events = new ArrayList<>(userEvents);
		events.addAll(nonUserEvents);
		events.add(0, null);
		int headerIndex2 = userEvents.size() + 1;
		events.add(headerIndex2, null);

		//Pass updated events list & header indices list to Search Adapter
		List<Integer> headerIndices = Arrays.asList(0, headerIndex2);
		searchAdapter.updateEventsAndHeaders(events, headerIndices);
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem menuItem)
	{
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem menuItem)
	{
		return true;
	}

	/**
	 * Connects {@link #searchRecycler} to {@link #searchAdapter}.
	 */
	private void setUpRecycler()
	{
		searchAdapter = new SearchAdapter();
		searchRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		searchRecycler.setAdapter(searchAdapter);
	}

	/**
	 * Search page displays empty view & removes headers
	 */
	private void clearSearchPage()
	{
		emptyView.setVisibility(View.VISIBLE);
		searchAdapter.updateEventsAndHeaders(Collections.<Event>emptyList(), Collections.<Integer>emptyList());
	}

}
