package com.cornellsatech.o_week;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Settings;
import com.google.common.eventbus.Subscribe;

/**
 * The first {@link android.app.Activity} that will execute when the app launches.
 * <p>
 * {@link #datePickerRecycler}: List of all dates for the orientation.
 * {@link #filterMenu}: Button to filter events by category. Should only be visible when {@link FeedFragment} is showing.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
	private RecyclerView datePickerRecycler;
	private DatePickerAdapter datePickerAdapter;
	private MenuItem filterMenu;

	private static final String TAG = MainActivity.class.getSimpleName();

	/**
	 * Sets up toolbar, {@link #datePickerRecycler}, and starts {@link FeedFragment}
	 *
	 * @param savedInstanceState Ignored.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setUpRecycler();

		NotificationCenter.DEFAULT.register(this);

		BottomNavigationView bottomNavBar = findViewById(R.id.navigation);
		bottomNavBar.setOnNavigationItemSelectedListener(this);
		//Highlights feed tab bar button
		bottomNavBar.setSelectedItemId(R.id.bottom_nav_my_schedule);
	}

	/**
	 * Called when an item on the bottom navigation bar is selected.
	 *
	 * @param item The item that was selected.
	 * @return True if the event was handled, false otherwise.
	 */
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.bottom_nav_feed:
				String feedTitle = getString(R.string.title_fragment_feed);
				startFragment(DatePagerFragment.newInstance(DatePagerAdapter.Type.Feed), feedTitle, true, true);
				break;
			case R.id.bottom_nav_my_schedule:
				String scheduleTitle = getString(R.string.title_fragment_my_schedule);
				startFragment(DatePagerFragment.newInstance(DatePagerAdapter.Type.Schedule), scheduleTitle, true, false);
				break;
			case R.id.bottom_nav_settings:
				String settingsTitle = getString(R.string.title_activity_settings);
				startFragment(new SettingsFragment(), settingsTitle, false, false);
				break;
			case R.id.bottom_nav_search:
				startFragment(new SearchFragment(), null, false, false);
				break;
			default:
				return false;
		}
		return true;
	}

	/**
	 * Trigger {@link DatePickerAdapter#onDetachedFromRecyclerView(RecyclerView)} so it unregisters itself
	 * as a listener.
	 * Also unregister ourselves as a listener.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//detach the adapter so that its onDestroy methods trigger
		datePickerRecycler.setAdapter(null);
		NotificationCenter.DEFAULT.unregister(this);
	}

	/**
	 * Connect {@link #datePickerRecycler} to {@link #datePickerAdapter}. Set up orientation for the
	 * recycler, set margin between items.
	 */
	private void setUpRecycler()
	{
		datePickerRecycler = findViewById(R.id.datePicker);
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
	 * sets an appropriate title and changes visibility of date picker bar.
	 *
	 * @param fragment       Fragment to transition to
	 * @param title          New title of toolbar. Null if none is to be set
	 * @param showDatePicker Whether {@link #datePickerRecycler} should be visible
	 * @param showFilter     Whether {@link #filterMenu} should be visible
	 */
	private void startFragment(Fragment fragment, @Nullable String title, boolean showDatePicker, boolean showFilter)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragmentContainer, fragment);
		transaction.commit();
		if (title != null)
			getSupportActionBar().setTitle(title);
		datePickerRecycler.setVisibility(showDatePicker ? View.VISIBLE : View.GONE);
		if (filterMenu != null)
			filterMenu.setVisible(showFilter);
	}


	/**
	 * Shows the dialog that allows the user to choose what {@link Category} to filter events by.
	 * If the user does select a NEW category to filter by, an event is sent out notifying listeners.
	 */
	private void showFilterDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.menu_filter);

		builder.setMultiChoiceItems(UserData.getFilters(this), UserData.getCheckedFilters(), new DialogInterface.OnMultiChoiceClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int index, boolean b)
			{
				//row 0 is reserved for "required events"
				if (index == 0)
				{
					UserData.filterRequired = !UserData.filterRequired;
				}
				else
				{
					int categoryPk = UserData.categories.get(index - 1).pk;
					if (UserData.selectedFilters.contains(categoryPk))
						UserData.selectedFilters.remove(categoryPk);
					else
						UserData.selectedFilters.add(categoryPk);
				}

				NotificationCenter.DEFAULT.post(new NotificationCenter.EventFilterChanged());
			}
		});
		//clear button to remove all filters
		builder.setNegativeButton(R.string.dialog_clear_button, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				UserData.filterRequired = false;
				UserData.selectedFilters.clear();
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventFilterChanged());
			}
		});
		builder.setPositiveButton(R.string.dialog_positive_button, null);
		builder.show();
	}

	/**
	 * Sets references to button in toolbar.
	 *
	 * @param menu {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_of_feed, menu);
		filterMenu = menu.findItem(R.id.filterMenu);
		filterMenu.setVisible(false);
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

	/**
	 * Listens for date changes, then notifies {@link #datePickerAdapter} to re-bind the
	 * {@link DateCell}s.
	 *
	 * @param event Ignored.
	 */
	@Subscribe
	public void onDateChanged(NotificationCenter.EventDateChanged event)
	{
		datePickerAdapter.notifyDataSetChanged();
	}
}
