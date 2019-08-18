package com.cornellsatech.o_week;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cornellsatech.o_week.models.StudentType;


/**
 * The pager fragment for the student type initial settings.
 */

public class InitialSettingsPage1Fragment extends Fragment implements View.OnClickListener
{
	private Button freshman;
	private Button transfer;
	private StudentType studentType = StudentType.NOTSET;
	private static final String TAG = InitialSettingsPage1Fragment.class.getSimpleName();

	/**
	 * Sets up the view components of the student type settings.
	 * add action listener to each button.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_student_type, container, false);
		freshman = rootView.findViewById(R.id.freshman);
		transfer = rootView.findViewById(R.id.transfer);

		//Sometimes view is reloaded after scrolling back from the third page. This ensures that the options the user had chosen remains visible.
		switch (studentType)
		{
			case FRESHMAN:
				setSelected(freshman);
				disableSelected(transfer);
				break;
			case TRANSFER:
				setSelected(transfer);
				disableSelected(freshman);
				break;
			case NOTSET:
				disableSelected(freshman);
				disableSelected(transfer);
		}

		freshman.setOnClickListener(this);
		transfer.setOnClickListener(this);

		return rootView;
	}

	/**
	 * Set a button's style as selected
	 */
	private void setSelected(Button button)
	{
		button.setBackgroundResource(R.drawable.bg_button_selected_ripple);
		button.setTextColor(Color.WHITE);
	}

	/**
	 * set a button's style as not selected
	 */
	private void disableSelected(Button button)
	{
		button.setBackgroundResource(R.drawable.bg_button_ripple);
		button.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
	}

	/**
	 * Handles button clicks.
	 *
	 * @param view {@link #freshman}, {@link #transfer}
	 */
	@Override
	public void onClick(View view)
	{
		Button buttonToSelect;
		Button buttonToDeselect;

		switch (view.getId())
		{
			case R.id.freshman:
				buttonToSelect = freshman;
				buttonToDeselect = transfer;
				studentType = StudentType.FRESHMAN;
				break;
			case R.id.transfer:
				buttonToSelect = transfer;
				buttonToDeselect = freshman;
				studentType = StudentType.TRANSFER;
				break;
			default:
				Log.e(TAG, "onClick unexpected id: " + view);
				return;
		}

		setSelected(buttonToSelect);
		disableSelected(buttonToDeselect);

		InitialSettingsActivity parentActivity = (InitialSettingsActivity) getActivity();
		parentActivity.setStudentType(studentType);
		parentActivity.switchToNextPage();
	}
}
