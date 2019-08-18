package com.cornellsatech.o_week;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cornellsatech.o_week.models.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Search Fragment that displays search results from my calender and feed.
 * This is a {@link Fragment} so that it can be  swapped out with {@link SearchFragment}
 * Layout in {@link com.cornellsatech.o_week.R.layout#fragment_search}
 * {@link #MIN_NUM_LETTERS} Minimum letters that must be typed in search bar to cause a refresh
 */

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener
{
	private LinearLayout emptyView;
	private RecyclerView searchRecycler;
	private SearchAdapter searchAdapter;
	private static final int MIN_NUM_LETTERS = 3;
	private static final String TAG = SearchFragment.class.getSimpleName();

	/**
	 * Initialize search page to display empty view. Set up options menu & recycler.
	 */
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
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
	 */
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
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
		for (Event event : UserData.allEvents)
		{
			if (event.getName().toLowerCase().contains(searchTextLowercase)
					|| event.getDescription().toLowerCase().contains(searchTextLowercase)
					|| event.getLocation().toLowerCase().contains(searchTextLowercase)
					|| event.getAdditional().toLowerCase().contains(searchTextLowercase))
				events.add(event);
		}
		//If Events List is empty, remove headers
		if (events.isEmpty())
		{
			clearSearchPage();
			return false;
		}
		emptyView.setVisibility(View.GONE);

		//Separate events into user's events and non user events
		List<Event> nonUserEvents = new ArrayList<>(events.size());
		Iterator<Event> userEventsIterator = events.iterator();
		while (userEventsIterator.hasNext())
		{
			Event event = userEventsIterator.next();
			if (!UserData.selectedEvents.contains(event)) {
				nonUserEvents.add(event);
				userEventsIterator.remove();
			}
		}
		Collections.sort(events);
		Collections.sort(nonUserEvents);

		//Combine user's events & non-users events into 1 list
		//Note: Added null at header indices to make it easy to access events at correct index
		int userEventsSize = events.size();
		events.addAll(nonUserEvents);
		events.add(0, null);
		int headerIndex2 = userEventsSize + 1;
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
