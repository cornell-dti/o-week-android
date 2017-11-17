package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity used to provide users a way to change their preferences. This class should remain mostly
 * empty; {@link SettingsFragment} is preferred by Android and has more functionality. This merely opens
 * the fragment and sets a back button.
 */
public class SettingsActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}
}
