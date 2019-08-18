package com.cornellsatech.o_week;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays either {@link FeedFragment} or {@link ScheduleFragment} through {@link DatePagerAdapter}.
 * Allows swiping between dates.
 *
 * {@link #type}: Whether the feed or the schedule will be displayed.
 * {@link #datePager}: View that displays the multiple pages of feed/schedule.
 */
public class DatePagerFragment extends Fragment
{
	private static final String TAG = DatePagerFragment.class.getSimpleName();
	private static final String TYPE_BUNDLE_KEY = "type";
	private DatePagerAdapter.Type type;
	private ViewPager2 datePager;
	private MenuItem filterMenu;
	private List<Category> categories;
	private String[] filters;
	private boolean[] checkedFilters;

	/**
	 * Create an instance of {@link DatePagerFragment} on the given type.
	 * This should be the only way you create instances of {@link DatePagerFragment}.
	 *
	 * It passes the given type as a Bundle to {@link DatePagerFragment}.
	 *
	 * @param type Whether this fragment displays {@link FeedFragment} or {@link ScheduleFragment}.
	 * @return Instance of the date pager.
	 */
	public static DatePagerFragment newInstance(DatePagerAdapter.Type type)
	{
		DatePagerFragment datePagerFragment = new DatePagerFragment();

		Bundle args = new Bundle();
		args.putSerializable(TYPE_BUNDLE_KEY, type);
		datePagerFragment.setArguments(args);

		return datePagerFragment;
	}

	/**
	 * Set up {@link DatePagerAdapter} to display feed or schedule, depending on {@link #type}.
	 * {@link #type} will be retrieved from the bundle.
	 */
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_pager, container, false);

		//retrieve type from bundle
		if (getArguments() != null)
			type = (DatePagerAdapter.Type) getArguments().getSerializable(TYPE_BUNDLE_KEY);
		else
			Log.e(TAG, "onCreateView: type not found");

		datePager = view.findViewById(R.id.viewPager);
		setDatePagerAdapter();
		//trigger a call to onDateChanged so this fragment starts out with the correct date
		onDateChanged(null);

		if (type == DatePagerAdapter.Type.Feed)
			setHasOptionsMenu(true);

		NotificationCenter.DEFAULT.register(this);
		return view;
	}

	private void setDatePagerAdapter()
	{
		DatePagerAdapter adapter = new DatePagerAdapter(this, type);
		datePager.setAdapter(adapter);
		datePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
		{
			/**
			 * Send page change events when the user swipes to a new page.
			 * @param position The index of the new page.
			 */
			@Override
			public void onPageSelected(int position)
			{
				UserData.selectedDate = UserData.sortedDates.get(position);
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventDateChanged());
			}
		});
	}

	/**
	 * Unregister ourselves as a listener.
	 */
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		NotificationCenter.DEFAULT.unregister(this);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_of_feed, menu);
		filterMenu = menu.findItem(R.id.filterMenu);
		updateFilterIcon();
	}

	/**
	 * Listens for clicks and performs the following actions:
	 * filterMenu: Shows filter dialog.
	 *
	 * @param item {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if (item.getItemId() == R.id.filterMenu) {
			showFilterDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Shows the dialog that allows the user to choose what {@link Category} to filter events by.
	 * If the user does select a NEW category to filter by, an event is sent out notifying listeners.
	 */
	private void showFilterDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.menu_filter);
		if (categories == null || filters == null || checkedFilters == null)
			cacheCategories();

		builder.setMultiChoiceItems(filters, checkedFilters, new DialogInterface.OnMultiChoiceClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int index, boolean b)
			{
				//row 0 is reserved for "required events"
				if (index == 0)
					UserData.filterRequired = !UserData.filterRequired;
				else
				{
					String categoryPk = categories.get(index - 1).getPk();
					if (!UserData.selectedFilters.remove(categoryPk))
						UserData.selectedFilters.add(categoryPk);
				}

				NotificationCenter.DEFAULT.post(new NotificationCenter.EventFilterChanged());
				updateFilterIcon();
			}
		});
		//clear button to remove all filters
		builder.setNegativeButton(R.string.dialog_clear_button, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				clearFilters();
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventFilterChanged());
				updateFilterIcon();
			}
		});
		builder.setPositiveButton(R.string.dialog_positive_button, null);
		builder.show();
	}

	private void cacheCategories()
	{
		categories = new ArrayList<>(UserData.categories);
		Collections.sort(categories);

		String[] filters = new String[categories.size() + 1];
		//index 0 represents filter for "required events"
		filters[0] = getString(R.string.filter_show_required_events);
		boolean[] checkedFilters = new boolean[categories.size() + 1];
		//index 0 represents filter for "required events"
		checkedFilters[0] = UserData.filterRequired;

		for (int i = 1; i < filters.length; i++)
		{
			Category category = categories.get(i - 1);
			filters[i] = category.getCategory();
			checkedFilters[i] = UserData.selectedFilters.contains(category.getPk());
		}

		this.filters = filters;
		this.checkedFilters = checkedFilters;
	}

	/**
	 * Remove all filters.
	 */
	private void clearFilters()
	{
		UserData.filterRequired = false;
		UserData.selectedFilters.clear();
	}

	/**
	 * Change the filter icon based on whether any filters are activated.
	 */
	private void updateFilterIcon()
	{
		if (UserData.selectedFilters.isEmpty() && !UserData.filterRequired)
			filterMenu.setIcon(R.drawable.ic_tune_white_24dp);
		else
			filterMenu.setIcon(R.drawable.ic_tune_inverted);
	}

	/**
	 * Refresh dates shown in pager and selected date.
	 */
	@Subscribe
	public void onInternetUpdate(NotificationCenter.EventInternetUpdate eventNewDates)
	{
		setDatePagerAdapter();
		cacheCategories();
		onDateChanged(null); // refresh selected date too
	}

	/**
	 * Flip the page to the location of the new date.
	 */
	@Subscribe
	public void onDateChanged(NotificationCenter.EventDateChanged event)
	{
		int position = UserData.sortedDates.indexOf(UserData.selectedDate);
		if (position == -1)
			return;
		if (position == datePager.getCurrentItem())
			return;
		datePager.setCurrentItem(position, false);
	}
}
