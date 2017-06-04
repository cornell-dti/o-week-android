package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
	RecyclerView datePickerRecycler;
	DatePickerAdapter datePickerAdapter;

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
	}

	//region Menu methods
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_of_feed, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.filterMenu:
				return true;
			case R.id.myScheduleMenu:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	//endregion
}
