package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.NotificationCenter;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Displays a list of events, ordered chronologically. This is a {@link Fragment} so that it can be
 * easily swapped out with {@link ScheduleFragment} while keeping the same date picker up top.
 * Layout in {@link R.layout#fragment_feed}
 */
public class FeedFragment extends Fragment
{
	private static final String TAG = FeedFragment.class.getSimpleName();
	private static final String DATE_BUNDLE_KEY = "date";
	private LocalDate date;
	private RecyclerView feedRecycler;
	private FeedAdapter feedAdapter;

	/**
	 * Create an instance of {@link FeedFragment} with the given date.
	 * This should be the only way you create instances of {@link FeedFragment}.
	 *
	 * It passes the given date as a Bundle to {@link FeedFragment}.
	 *
	 * @param date The date the feed will display.
	 * @return Instance of the feed.
	 */
	public static FeedFragment newInstance(LocalDate date)
	{
		FeedFragment feedFragment = new FeedFragment();

		Bundle args = new Bundle();
		args.putSerializable(DATE_BUNDLE_KEY, date);
		feedFragment.setArguments(args);

		return feedFragment;
	}
	/**
	 * Connects views, sets up recycler, registers as listener. Unregisters in {@link #onDestroyView()}.
	 * Listens for event clicks and opens {@link DetailsActivity} upon a click.
	 *
	 * Retrieves {@link #date} from the bundle.
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

		//retrieve date from bundle
		if (getArguments() != null)
			date = (LocalDate) getArguments().getSerializable(DATE_BUNDLE_KEY);
		else
			Log.e(TAG, "onCreateView: date not found");

		feedRecycler = view.findViewById(R.id.feedRecycler);
		setUpRecycler(view.findViewById(R.id.emptyState), view.findViewById(R.id.feedRecycler));
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
	 * Connects {@link #feedRecycler} to {@link #feedAdapter}.
	 * @param emptyView the empty view the recycler should use when there are no elements
	 * @param recyclerView the default recycler view
	 */
	private void setUpRecycler(View emptyView, View recyclerView)
	{
		feedAdapter = new FeedAdapter(date, recyclerView, emptyView);
		feedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		feedRecycler.setAdapter(feedAdapter);
		scrollToNextEvent();
	}
	/**
	 * Scrolls {@link #feedRecycler} to the next upcoming event based on the current time and date.
	 */
	private void scrollToNextEvent()
	{
		LocalDate date = LocalDate.now();
		LocalTime now = LocalTime.now();

		if (!date.isEqual(UserData.selectedDate))
			return;
		for (int i = 0; i < feedAdapter.events.size(); i++)
		{
			Event event = feedAdapter.events.get(i);
			if (event.startTime.isBefore(now))
				continue;

			feedRecycler.scrollToPosition(i);
			return;
		}
	}
}
