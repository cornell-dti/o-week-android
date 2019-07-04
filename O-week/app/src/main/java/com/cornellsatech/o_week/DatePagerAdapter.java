package com.cornellsatech.o_week;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Allows for swiping between pages of {@link FeedFragment} and {@link ScheduleFragment}.
 * {@link #type}: Whether this will display pages of feed or schedule.
 */
public class DatePagerAdapter extends FragmentPagerAdapter
{
	private static final String TAG = DatePagerAdapter.class.getSimpleName();
    private final Type type;

	/**
	 * Set the {@link #type} of pages we'll be displaying, either feed or schedule.
	 *
	 * @param fm {@link Fragment#getChildFragmentManager()} if this is inside another fragment.
	 * @param type {@link Type}
	 */
	public DatePagerAdapter(FragmentManager fm, Type type)
    {
        super(fm);
        this.type = type;
    }

	/**
	 * Returns the page at the location.
	 * @param position Index of the page.
	 * @return Fragment.
	 */
	@Override
    public Fragment getItem(int position)
    {
        switch (type)
        {
            case Feed:
                return FeedFragment.newInstance(UserData.DATES.get(position));
            case Schedule:
	            return ScheduleFragment.newInstance(UserData.DATES.get(position));
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
    public int getCount()
    {
        return UserData.DATES.size();
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
