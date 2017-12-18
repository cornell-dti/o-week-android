package com.cornellsatech.o_week;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cornellsatech.o_week.models.CollegeType;

import java.util.HashMap;
import java.util.Map;

/**
 * The pager fragment for the college settings page.
 */

public class InitialSettingsPage2Fragment extends Fragment implements View.OnClickListener
{
	private final Map<Button, CollegeType> collegeButtons = new HashMap<>(CollegeType.values().length);

	/**
	 * Sets up the views
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_college, container, false);
		createCollegeButtons(rootView);
		return rootView;
	}

	/**
	 * Creates buttons for each college in {@link CollegeType}.
	 * @param rootView View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 */
	private void createCollegeButtons(View rootView)
	{
		LinearLayout linearLayout = rootView.findViewById(R.id.linearLayout);
		int MARGIN = getResources().getDimensionPixelSize(R.dimen.margin);
		int HEIGHT = getResources().getDimensionPixelSize(R.dimen.height_initial_settings_small_button);
		float TEXT_SIZE = getResources().getDimension(R.dimen.size_initial_settings_small_button);

		for (CollegeType collegeType : CollegeType.values())
		{
			Button button = new Button(getContext(), null, R.style.Button);
			button.setText(collegeType.toString());
			button.setTextAppearance(getContext(), R.style.TextAppearance_AppCompat);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXT_SIZE);
			button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			button.setOnClickListener(this);
			collegeButtons.put(button, collegeType);

			//set constraints, add to linear layout
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HEIGHT);
			layoutParams.topMargin = MARGIN;
			linearLayout.addView(button, layoutParams);
		}
	}

	/**
	 * Set the given button as active, other buttons in the class as inactive.
	 *
	 * @param button A button representing a college type.
	 */
	private void setActiveButton(Button button)
	{
		for (Button collegeButton : collegeButtons.keySet())
			disableSelected(collegeButton);
		setSelected(button);
	}

	/**
	 * Set a button's style as selected
	 *
	 * @param button A button representing a college type.
	 */
	private void setSelected(Button button)
	{
		button.setBackgroundResource(R.drawable.bg_button_selected_ripple);
		button.setTextColor(Color.WHITE);
	}

	/**
	 * set a button's style as not selected
	 *
	 * @param button A button representing a college type.
	 */
	private void disableSelected(Button button)
	{
		button.setBackgroundResource(R.drawable.bg_button_ripple);
		button.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
	}

	/**
	 * Handles button clicks. Set the new value and advance to the next fragment.
	 *
	 * @param view The button click
	 */
	@Override
	public void onClick(View view)
	{
		Button buttonClicked = (Button) view;
		CollegeType collegeClicked = collegeButtons.get(buttonClicked);
		setActiveButton(buttonClicked);

		InitialSettingsActivity parentActivity = (InitialSettingsActivity) getActivity();
		parentActivity.setCollegeType(collegeClicked);
		parentActivity.switchToNextPage();
	}
}
