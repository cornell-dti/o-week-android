package com.cornellsatech.o_week;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Settings;

/**
 * Displays a user-selected event in a separate page. An {@link android.app.Activity} is used instead
 * of a Fragment since this page should have a back button.
 *
 * {@link #event}: The event displayed to the user. NOTE: This should already be set when the Activity
 *                 is opened, set by whomever called {@link #startActivity(Intent)}. The better option
 *                 would be to put {@link #event} in a {@link Bundle}, but then {@link Event} would
 *                 have to extend {@link android.os.Parcelable}.
 * {@link #coordinatorLayout}: Layout that will be passed to
 *                             {@link Internet#getImageForEvent(Event, ImageView, CoordinatorLayout, boolean)}.
 *                             A reference to the {@link CoordinatorLayout} is necessary to display
 *                             {@link android.support.design.widget.Snackbar}.
 */
public class DetailsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{
	public static Event event;
	private CoordinatorLayout coordinatorLayout;
	private ImageView eventImage;
	private TextView titleText;
	private TextView captionText;
	private TextView startTimeText;
	private TextView endTimeText;
	private TextView descriptionText;
	private CheckBox checkBox;
	private static final String TAG = DetailsActivity.class.getSimpleName();

	/**
	 * Link to layout, add back button to toolbar, sets up views.
	 *
	 * @param savedInstanceState Ignored.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//set back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViews();
		setEventData();
	}
	/**
	 * Links {@link android.view.View}s with their pointers.
	 */
	private void findViews()
	{
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		eventImage = (ImageView) findViewById(R.id.eventImage);
		titleText = (TextView) findViewById(R.id.titleText);
		captionText = (TextView) findViewById(R.id.captionText);
		startTimeText = (TextView) findViewById(R.id.startTimeText);
		endTimeText = (TextView) findViewById(R.id.endTimeText);
		descriptionText = (TextView) findViewById(R.id.descriptionText);
	}
	/**
	 * Shows the {@link #event}'s data on screen. Attempts to retrieve an image from the database or
	 * from saved files.
	 */
	private void setEventData()
	{
		if (event == null)
		{
			Log.e(TAG, "setEventData: event is null, should be set by whomever opened this activity");
			return;
		}

		titleText.setText(event.title);
		captionText.setText(event.caption);
		descriptionText.setText(event.description);
		startTimeText.setText(event.startTime.toString(Event.DISPLAY_TIME_FORMAT));
		endTimeText.setText(event.endTime.toString(Event.DISPLAY_TIME_FORMAT));

		//we must know if we can write the image we downloaded to file
		boolean canWriteToFile = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		if (!canWriteToFile)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

		Internet.getImageForEvent(event, eventImage, coordinatorLayout, canWriteToFile);
	}

	/**
	 * Put the {@link #checkBox} to the top right.
	 * @param menu {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_details, menu);

		checkBox = (CheckBox) menu.findItem(R.id.checkbox).getActionView();
		//hacky way to set right margin of check box
		checkBox.setText("   ");
		//set checkbox to white
		int[][] states = {{android.R.attr.state_checked}, {}};
		int[] colors = {Color.WHITE, Color.WHITE};
		checkBox.setButtonTintList(new ColorStateList(states, colors));
		//set checkbox checked based on whether event is selected
		checkBox.setChecked(UserData.selectedEventsContains(event));
		checkBox.setOnCheckedChangeListener(this);

		return true;
	}

	/**
	 * Handle user selection of event.
	 * @param buttonView Ignored.
	 * @param isChecked Whether the {@link #checkBox} is checked.
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
			UserData.insertToSelectedEvents(event);
		else
			UserData.removeFromSelectedEvents(event);
		NotificationCenter.DEFAULT.post(new NotificationCenter.EventSelectionChanged(event));
	}

	/**
	 * This runs when the user answers the dialog that asks for file-writing permissions. If the user
	 * grants us permission this time, re-download the image and save it.
	 *
	 * @param requestCode {@inheritDoc}
	 * @param permissions {@inheritDoc}
	 * @param grantResults {@inheritDoc}
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		//reload the image, this time saving to file
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			Internet.getImageForEvent(event, eventImage, coordinatorLayout, true);
	}

	/**
	 * Save the selected events to file in case the user checked/unchecked the {@link #checkBox}.
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.setSelectedEvents(this);
	}
}
