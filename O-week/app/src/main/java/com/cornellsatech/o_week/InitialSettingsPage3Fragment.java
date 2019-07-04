package com.cornellsatech.o_week;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * the pager fragment for the welcome page.
 */

public class InitialSettingsPage3Fragment extends Fragment
{

	/**
	 * Sets up the view components.
	 * Add action listener for button.
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_welcome, container, false);

		Button getStarted = rootView.findViewById(R.id.getStartedButton);
		getStarted.setBackgroundResource(R.drawable.bg_button_ripple);
		getStarted.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				InitialSettingsActivity parentActivity = (InitialSettingsActivity) getActivity();
				parentActivity.switchToNextPage();
			}
		});

		return rootView;
	}
}
