package com.cornellsatech.o_week;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Allows for swiping between pages of {@link FeedFragment} and {@link ScheduleFragment}.
 * {@link #type}: Whether this will display pages of feed or schedule.
 */
public class DatePagerAdapter extends FragmentStateAdapter
{
	private static final String TAG = DatePagerAdapter.class.getSimpleName();
    private final Type type;

	/**
	 * Set the {@link #type} of pages we'll be displaying, either feed or schedule.
	 *
	 * @param fragment Parent
	 * @param type {@link Type}
	 */
	public DatePagerAdapter(Fragment fragment, Type type)
    {
        super(fragment);
        this.type = type;
    }

	/**
	 * Returns the page at the location.
	 * @param position Index of the page.
	 * @return Fragment.
	 */
	@Override
	@NonNull
    public Fragment createFragment(int position)
    {
        switch (type)
        {
            case Feed:
                return FeedFragment.newInstance(UserData.sortedDates.get(position));
            case Schedule:
	            return ScheduleFragment.newInstance(UserData.sortedDates.get(position));
	        default:
                Log.e(TAG, "getItem: unknown type");
                return null;
        }
    }

	/**
	 * Returns the number of pages.
	 * @return Number of days in orientation.
	 */
	@Override
    public int getItemCount()
    {
        return UserData.sortedDates.size();
    }

	/**
	 * The types of pages this can display.
	 * {@link #Feed} = {@link FeedFragment}, {@link #Schedule} = {@link ScheduleFragment}.
	 */
	enum Type
    {
        Feed, Schedule
    }
}
