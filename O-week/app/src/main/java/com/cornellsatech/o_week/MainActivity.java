package com.cornellsatech.o_week;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
	private RecyclerView datePickerRecycler;
	private DatePickerAdapter datePickerAdapter;
	private MenuItem filterMenu;
	private MenuItem feedMenu;
	private MenuItem scheduleMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		setUpRecycler();
		startFeedFragment();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//detach the adapter so that its onDestroy methods trigger
		datePickerRecycler.setAdapter(null);
	}

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
	private void startFeedFragment()
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragmentContainer, new FeedFragment());
		transaction.commit();
		getSupportActionBar().setTitle(R.string.title_fragment_feed);
	}
	private void startScheduleFragment()
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragmentContainer, new ScheduleFragment());
		transaction.commit();
		getSupportActionBar().setTitle(R.string.title_fragment_my_schedule);
	}
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

	//region Menu methods
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_of_feed, menu);
		filterMenu = menu.findItem(R.id.filterMenu);
		feedMenu = menu.findItem(R.id.feedMenu);
		scheduleMenu = menu.findItem(R.id.myScheduleMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.filterMenu:
				showFilterDialog();
				return true;
			case R.id.myScheduleMenu:
				startScheduleFragment();
				filterMenu.setVisible(false);
				feedMenu.setVisible(true);
				scheduleMenu.setVisible(false);
				return true;
			case R.id.feedMenu:
				startFeedFragment();
				filterMenu.setVisible(true);
				feedMenu.setVisible(false);
				scheduleMenu.setVisible(true);
				return true;
			case R.id.settingsMenu:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	//endregion

	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.setSelectedEvents(this);
	}
}
