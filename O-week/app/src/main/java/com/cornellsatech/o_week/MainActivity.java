package com.cornellsatech.o_week;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Settings;

/**
 * The first {@link android.app.Activity} that will execute when the app launches.
 *
 * {@link #datePickerRecycler}: List of all dates for the orientation.
 * {@link #filterMenu}: Button to filter events by category. Should only be visible when {@link FeedFragment}
 *                      is showing.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
	private RecyclerView datePickerRecycler;
	private DatePickerAdapter datePickerAdapter;
	private MenuItem filterMenu;

	/**
	 * Sets up toolbar, {@link #datePickerRecycler}, and starts {@link FeedFragment}
	 * @param savedInstanceState Ignored.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpRecycler();

        String feedTitle = getResources().getString(R.string.title_fragment_feed);
        startFragment(new FeedFragment(), feedTitle);

        BottomNavigationView bottomNavBar = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavBar.setOnNavigationItemSelectedListener(this);
        //Highlights feed tab bar button
        bottomNavBar.getMenu().getItem(0).setChecked(false);
        bottomNavBar.getMenu().getItem(1	).setChecked(true);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
	    switch (item.getItemId()) {
            case R.id.bottom_nav_feed:
                String feedTitle = getResources().getString(R.string.title_fragment_feed);
                startFragment(new FeedFragment(), feedTitle);
                break;
            case R.id.bottom_nav_my_schedule:
                String scheduleTitle = getResources().getString(R.string.title_fragment_my_schedule);
                startFragment(new ScheduleFragment(), scheduleTitle);
                break;
            case R.id.bottom_nav_settings:
                String settingsTitle = getResources().getString(R.string.title_activity_settings);
                startFragment(new SettingsFragment(), settingsTitle);
                break;
            default:
                return false;
        }
        return true;
    }
	/**
	 * Trigger {@link DatePickerAdapter#onDetachedFromRecyclerView(RecyclerView)} so it unregisters itself
	 * as a listener.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//detach the adapter so that its onDestroy methods trigger
		datePickerRecycler.setAdapter(null);
	}
	/**
	 * Connect {@link #datePickerRecycler} to {@link #datePickerAdapter}. Set up orientation for the
	 * recycler, set margin between items.
	 */
	private void setUpRecycler()
	{
		datePickerRecycler = (RecyclerView) findViewById(R.id.datePicker);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		datePickerRecycler.setLayoutManager(layoutManager);
		SpacingItemDecor spacingItemDecor = new SpacingItemDecor(R.dimen.margin_horiz_date_cell, R.dimen.margin_horiz_date_cell, this);
		datePickerRecycler.addItemDecoration(spacingItemDecor);
		datePickerAdapter = new DatePickerAdapter();
		datePickerRecycler.setAdapter(datePickerAdapter);
	}

	/**
	 * Switches out the fragment for {@link FeedFragment}, {@link ScheduleFragment}, or {@link SettingsFragment},
     * sets an appropriate title and changes visibility of date picker bar
	 */
	private void startFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
        getSupportActionBar().setTitle(title);
        if(fragment instanceof SettingsFragment) {
            datePickerRecycler.setVisibility(View.GONE);
        } else {
            datePickerRecycler.setVisibility(View.VISIBLE);
        }
    }

	/**
	 * Shows the dialog that allows the user to choose what {@link Category} to filter events by.
	 * If the user does select a NEW category to filter by, an event is sent out notifying listeners.
	 */
	private void showFilterDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.menu_filter);

		//get an array of categories
		String[] categories = new String[UserData.categories.size() + 2];
		//2 default filters
		categories[0] = getString(R.string.filter_show_all_events);
		categories[1] = getString(R.string.filter_show_required_events);
		for (int i = 2; i < categories.length; i++)
			categories[i] = UserData.categories.get(i - 2).name;

		builder.setSingleChoiceItems(categories, UserData.selectedFilterIndex, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//notify listeners that the filter has changed
				if (UserData.selectedFilterIndex != which)
				{
					UserData.selectedFilterIndex = which;
					NotificationCenter.DEFAULT.post(new NotificationCenter.EventFilterChanged());
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.dialog_negative_button, null);
		builder.show();
	}

	/**
	 * Sets references to button in toolbar.
	 * @param menu {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_of_feed, menu);
		filterMenu = menu.findItem(R.id.filterMenu);
		return true;
	}

	/**
	 * Listens for clicks and performs the following actions:
	 * {@link #filterMenu}: Shows filter dialog.
	 *
	 * @param item {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.filterMenu:
				showFilterDialog();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Save selected events when this app is about to enter the background, in case user selections changed.
	 * This is not done continuously to save on processing power.
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.setSelectedEvents(this);
	}

}
