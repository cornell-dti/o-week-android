package com.cornellsatech.o_week;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Displays a user-selected event in a separate page. An {@link android.app.Activity} is used instead
 * of a Fragment since this page should have a back button.
 *
 * {@link #EVENT_KEY}: See {@link #onCreate(Bundle)}.
 * {@link #event}: The event displayed to the user.
 * {@link #coordinatorLayout}: Layout that will be shouldActUpon to
 *                             {@link Internet#getImageForEvent(Event, ImageView, CoordinatorLayout, boolean)}.
 *                             A reference to the {@link CoordinatorLayout} is necessary to display
 *                             {@link android.support.design.widget.Snackbar}.
 */
public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback, Button.OnClickListener
{
	public static String EVENT_KEY = "event";
	private Event event;
	private CoordinatorLayout coordinatorLayout;
	private ImageView eventImage;
	private TextView titleText;
	private TextView captionText;
	private TextView startTimeText;
	private TextView endTimeText;
	private TextView descriptionText;
	private TextView additionalText;
	private TextView requiredButton;
	private TextView requiredButton2;
	private TextView requirementDetails;
	private ImageView horizontalBreakBar2;
	private Button addButton;
	private Button readMoreButton;
	private static final String TAG = DetailsActivity.class.getSimpleName();
	public static final int MAP_ZOOM = 16;

	/**
	 * Link to layout, add back button to toolbar, sets up views. Retrieves {@link #event} from the
	 * bundle shouldActUpon in the parameter, which contains a String that describes an Event.
	 *
	 * @param savedInstanceState Contains a String that is the event to display.
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

		//get the event
		event = Event.fromString(getIntent().getExtras().getString(EVENT_KEY));

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
		additionalText = (TextView) findViewById(R.id.additionalText);
		requiredButton = (TextView) findViewById(R.id.requiredLabel);
		requiredButton2 = (TextView) findViewById(R.id.requiredLabel2);
		addButton = (Button) findViewById(R.id.addButton);
		readMoreButton = (Button) findViewById(R.id.readMoreButton);
		requirementDetails = (TextView) findViewById(R.id.requirementDetails);
		horizontalBreakBar2 = (ImageView) findViewById(R.id.horizontalBreakBar2);

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
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
		if(!event.required){
			requiredButton.setVisibility(View.GONE);
		}
		if(UserData.selectedEventsContains(event)){
			addButton.setText(R.string.button_text_event_added);
		}
		addButton.setOnClickListener(this);
		setAdditionalText();

		//Set the description text cap at 3 for the time being.
		descriptionText.setMaxLines(3);
		readMoreButton.setOnClickListener(this);

		if(event.categoryRequired){
			Category category = UserData.categoryForPk(event.category);
			if(category != null){
				requirementDetails.setVisibility(View.VISIBLE);
				requirementDetails.setText("Required for " + category.name);
				horizontalBreakBar2.setVisibility(View.VISIBLE);
				requiredButton.setVisibility(View.VISIBLE);
				requiredButton2.setVisibility(View.VISIBLE);
			}
		}


		//we must know if we can write the image we downloaded to file
		boolean canWriteToFile = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		if (!canWriteToFile)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

		Internet.getImageForEvent(event, eventImage, coordinatorLayout, canWriteToFile);
	}
	/**
	 * Sets {@link #additionalText} if {@link Event#additional} is non-empty.
	 */
	private void setAdditionalText()
	{
		if (event.additional.isEmpty())
			return;
		additionalText.setVisibility(View.VISIBLE);
		additionalText.setText(event.formattedAdditionalText());
	}

	/**
	 * This runs when a button is clicked on the activity_details.xml view. All buttons
	 * share the same onClick handler (this), and therefore should be distinguished from one
	 * another programmatically instead of creating new listeners for each button. Do this by checking Resource ID.
	 *
	 * @param pressedButton {@inheritDoc}
	 */
	@Override
	public void onClick(View pressedButton){
		Button button = (Button) pressedButton;
		if(button.getId() == R.id.addButton){
			if(UserData.selectedEventsContains(event)){
				UserData.removeFromSelectedEvents(event);
				Notifications.unscheduleForEvent(event, this);
				button.setText(R.string.button_text_event_not_added);
				Toast.makeText(this, R.string.toast_text_event_removed, Toast.LENGTH_SHORT).show();
			}else{
				UserData.insertToSelectedEvents(event);
				if (Settings.getReceiveReminders(this))
					Notifications.scheduleForEvent(event, this);
				button.setText(R.string.button_text_event_added);
				Toast.makeText(this, R.string.toast_text_event_added, Toast.LENGTH_SHORT).show();
			}
			NotificationCenter.DEFAULT.post(new NotificationCenter.EventSelectionChanged(event, button.getText()==getString(R.string.button_text_event_added)));
		}
		if(button.getId() == R.id.readMoreButton){
			if(readMoreButton.getText().toString().equalsIgnoreCase(getString(R.string.read_more_button_unopened))){
				descriptionText.setMaxLines(100000000);
				readMoreButton.setText(getString(R.string.read_more_button_opened));
			}
			else if(readMoreButton.getText().equals(getString(R.string.read_more_button_opened))){
				descriptionText.setMaxLines(3);
				readMoreButton.setText(getString(R.string.read_more_button_unopened));
			}
		}
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
	 * Save the selected events to file in case the user checked/unchecked the {@link #addButton}.
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Settings.setSelectedEvents(this);
	}

	/**
	 * Loads {@link Event#latitude} and longitude onto the map, adds a marker.
	 * @param map {@inheritDoc}
	 */
	@Override
	public void onMapReady(GoogleMap map)
	{
		LatLng position = new LatLng(event.latitude, event.longitude);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
		map.addMarker(new MarkerOptions().position(position).title(event.caption));
	}
}
