package com.cornellsatech.o_week;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.RelativeLayout;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.eventbus.Subscribe;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Search Fragment that displays search results from my calender and feed.
 * This is a {@link Fragment} so that it can be  swapped out with {@link SearchFragment}
 * Layout in {@link R.layout#fragment_search}
 */

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private Context mContext;
    private RelativeLayout emptyView;
    private RecyclerView searchRecycler;
    private SearchAdapter searchAdapter;
    private List<Integer> headerIndexes;
    private List<Event> events;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        emptyView = (RelativeLayout) view.findViewById(R.id.searchEmptyView);
        emptyView.setVisibility(View.GONE);

        NotificationCenter.DEFAULT.register(this);

        searchRecycler = view.findViewById(R.id.searchRecycler);
        setUpRecycler();

        mContext = getActivity(); //returns Activity associated w/ fragment
        setHasOptionsMenu(true);

        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        //Set placeholder text in search bar
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        //Makes search bar expands automatically when open Search page
        searchView.setIconified(false);
        searchView.clearFocus();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String searchText) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return true;
    }

    /**
     * Unregisters as listener to prevent memory leaks. Also triggers {@link SearchAdapter}'s un-registration.
     * Registered in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        NotificationCenter.DEFAULT.unregister(this);
        //detach the adapter so that its onDestroy methods trigger
        searchRecycler.setAdapter(null);
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
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.EVENT_KEY, eventEventClicked.event.toString());
        startActivity(intent);
    }
    /**
     * Connects {@link #searchRecycler} to {@link #searchAdapter}.
     */
    private void setUpRecycler()
    {
        headerIndexes = Arrays.asList(0,5);
        searchAdapter = new SearchAdapter(headerIndexes);
        searchRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchRecycler.setAdapter(searchAdapter);
        scrollToNextEvent();
    }
    /**
     * Scrolls {@link #searchRecycler} to the next upcoming event based on the current time and date.
     */
    private void scrollToNextEvent()
    {
        LocalDate date = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!date.isEqual(UserData.selectedDate))
            return;
        for (int i = 0; i < searchAdapter.events.size(); i++)
        {
            Event event = searchAdapter.events.get(i);
            if (event.startTime.isBefore(now))
                continue;

            searchRecycler.scrollToPosition(i);
            return;
        }
    }
}
