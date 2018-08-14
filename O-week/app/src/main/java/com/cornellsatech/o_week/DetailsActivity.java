package com.cornellsatech.o_week;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.util.Internet;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Displays a user-selected event in a separate page. An {@link android.app.Activity} is used instead
 * of a Fragment since this page should have a back button.
 * <p>
 * {@link #EVENT_KEY}: See {@link #onCreate(Bundle)}.
 * {@link #event}: The event displayed to the user.
 * {@link #coordinatorLayout}: Layout that will be shouldActUpon to
 * {@link Internet#getImageForEvent(Event, ImageView, CoordinatorLayout, boolean)}.
 * A reference to the {@link CoordinatorLayout} is necessary to display
 * {@link android.support.design.widget.Snackbar}.
 */
public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback, Button.OnClickListener
{
	public static final String EVENT_KEY = "event";
	private Event event;
	private CoordinatorLayout coordinatorLayout;
	private ImageView eventImage;
	private TextView titleText;
	private TextView captionText;
	private TextView timeText;
	private TextView descriptionText;
	private TextView additionalText;
	private TextView requiredLabel;
	private TextView requirementDetails;
	private View horizontalBreakBar;
	private Button addButton;
	private TextView moreButton;
	private View moreButtonGradient;
	private Button directionsButton;
	private GeoDataClient geoDataClient;
	@Nullable
	private String placeName;
	@Nullable
	private String placeAddress;

	private static final String TAG = DetailsActivity.class.getSimpleName();
	private static final int MAP_ZOOM = 16;
	private static final int NUM_LINES_IN_CONDENSED_DESCRIPTION = 3;

	/**
	 * Starts {@link DetailsActivity} with the event.
	 * @param event Event to display.
	 * @param context Context to start the activity in
	 */
	public static void startWithEvent(Event event, Context context)
	{
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.putExtra(DetailsActivity.EVENT_KEY, event.toString());
		context.startActivity(intent);
	}
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
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//set back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//get the event
		event = Event.fromString(getIntent().getExtras().getString(EVENT_KEY));

		geoDataClient = Places.getGeoDataClient(this);

		findViews();
		setEventData();
	}

	/**
	 * Links {@link android.view.View}s with their pointers.
	 */
	private void findViews()
	{
		coordinatorLayout = findViewById(R.id.coordinatorLayout);
		eventImage = findViewById(R.id.eventImage);
		titleText = findViewById(R.id.titleText);
		captionText = findViewById(R.id.captionText);
		timeText = findViewById(R.id.timeText);
		horizontalBreakBar = findViewById(R.id.horizontalBreakBar);
		requiredLabel = findViewById(R.id.requiredLabel);
		requirementDetails = findViewById(R.id.requirementDetails);
		additionalText = findViewById(R.id.additionalText);
		descriptionText = findViewById(R.id.descriptionText);
		addButton = findViewById(R.id.addButton);
		moreButton = findViewById(R.id.moreButton);
		moreButtonGradient = findViewById(R.id.moreButtonGradient);
		directionsButton = findViewById(R.id.directionsButton);

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

		setTitle(event.readableDate());

		titleText.setText(event.title);
		captionText.setText(event.caption);
		descriptionText.setText(event.description);
		timeText.setText(event.startTime.toString(Event.DISPLAY_TIME_FORMAT) + " - " + event.endTime.toString(Event.DISPLAY_TIME_FORMAT));
		if (UserData.selectedEventsContains(event))
			addButton.setText(R.string.button_text_event_added);
		addButton.setOnClickListener(this);
		moreButton.setOnClickListener(this);
		directionsButton.setOnClickListener(this);

		configureDescription();
		configureRequired();
		configureImage();
	}

	/**
	 * Set the text to {@link #descriptionText} and {@link #additionalText}, showing/hiding the
	 * {@link #moreButton} as necessary.
	 */
	private void configureDescription()
	{
		descriptionText.setMaxLines(NUM_LINES_IN_CONDENSED_DESCRIPTION);
		//find out how many lines the description text will be
		descriptionText.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
		{
			@Override
			public boolean onPreDraw()
			{
				if (moreButton.getVisibility() != View.VISIBLE)
					return true;

				int lineCount = descriptionText.getLayout().getLineCount();
				if (lineCount > NUM_LINES_IN_CONDENSED_DESCRIPTION)
				{
					moreButton.setVisibility(View.VISIBLE);
					moreButtonGradient.setVisibility(View.VISIBLE);
				}
				else
				{
					moreButton.setVisibility(View.GONE);
					moreButtonGradient.setVisibility(View.GONE);
				}
				return true;
			}
		});

		if (!event.additional.isEmpty())
		{
			additionalText.setVisibility(View.VISIBLE);
			additionalText.setText(event.formattedAdditionalText());
		}
		else
			additionalText.setVisibility(View.GONE);
	}

	/**
	 * Show/hide the {@link #requiredLabel} and set the text for {@link #requirementDetails} based
	 * on whether the event is required. If the event is required (but not for this user),
	 * {@link #requiredLabel} will be gray.
	 */
	private void configureRequired()
	{
		if (!(event.required || event.categoryRequired))
		{
			requiredLabel.setVisibility(View.GONE);
			requirementDetails.setVisibility(View.GONE);
			horizontalBreakBar.setVisibility(View.GONE);
		}
		else
		{
			//change color of RQ label based on whether or not it's required for this user
			int requiredLabelBg = UserData.requiredForUser(event) ? R.drawable.required_label : R.drawable.required_label_gray;
			requiredLabel.setBackground(ContextCompat.getDrawable(this, requiredLabelBg));

			if (event.required)
				requirementDetails.setText(R.string.required_for_all);
			else
				requirementDetails.setText(getString(R.string.required_for_category, UserData.categoryForPk(event.category).name));
		}
	}

	/**
	 * Set the image, asking the user for permission to save the image on disk if necessary.
	 */
	private void configureImage()
	{
		//we must know if we can write the image we downloaded to file
		boolean canWriteToFile = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		if (!canWriteToFile)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

		Internet.getImageForEvent(event, eventImage, coordinatorLayout, canWriteToFile);
	}

	/**
	 * This runs when a button is clicked on the activity_details.xml view. All buttons
	 * share the same onClick handler (this), and therefore should be distinguished from one
	 * another programmatically instead of creating new listeners for each button. Do this by checking Resource ID.
	 *
	 * @param pressedButton {@inheritDoc}
	 */
	@Override
	public void onClick(View pressedButton)
	{
		switch (pressedButton.getId())
		{
			case R.id.addButton:
				boolean selected;
				if (UserData.selectedEventsContains(event))
				{
					UserData.removeFromSelectedEvents(event);
					Notifications.unscheduleForEvent(event, this);
					addButton.setText(R.string.button_text_event_not_added);
					Toast.makeText(this, R.string.toast_text_event_removed, Toast.LENGTH_SHORT).show();
					selected = false;
				}
				else
				{
					UserData.insertToSelectedEvents(event);
					if (Settings.getReceiveReminders(this))
						Notifications.scheduleForEvent(event, this);
					addButton.setText(R.string.button_text_event_added);
					Toast.makeText(this, R.string.toast_text_event_added, Toast.LENGTH_SHORT).show();
					selected = true;
				}
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventSelectionChanged(event, selected));
				break;
			case R.id.moreButton:
				descriptionText.setMaxLines(Integer.MAX_VALUE);
				moreButton.setVisibility(View.GONE);
				break;
			case R.id.directionsButton:
				startMap();
				break;
			default:
				Log.e(TAG, "onClick: Unknown button pressed");
		}
	}

	/**
	 * This runs when the user answers the dialog that asks for file-writing permissions. If the user
	 * grants us permission this time, re-download the image and save it.
	 *
	 * @param requestCode  {@inheritDoc}
	 * @param permissions  {@inheritDoc}
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
	 *
	 * @param map {@inheritDoc}
	 */
	@Override
	public void onMapReady(final GoogleMap map)
	{
		LatLng position = new LatLng(event.latitude, event.longitude);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
		map.addMarker(new MarkerOptions().position(position).title(event.caption));

//		geoDataClient.getPlaceById(event.placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>()
//		{
//			@Override
//			public void onComplete(@NonNull Task<PlaceBufferResponse> task)
//			{
//				if (!task.isSuccessful()) {
//					Log.e(TAG, "onMapReady: place not found");
//					return;
//				}
//
//				PlaceBufferResponse places = task.getResult();
//				Place place = places.get(0);
//				placeName = place.getName().toString();
//				placeAddress = place.getAddress().toString();
//				LatLng position = place.getLatLng();
//				map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
//				map.addMarker(new MarkerOptions().position(position).title(event.caption));
//				places.release();
//			}
//		});
	}

	/**
	 * Open the map with the event's location.
	 */
	private void startMap()
	{
//		if (placeName == null || placeAddress == null)
//			return;
//
//		Uri uri = Uri.parse("geo:0,0?q=" + placeName + ", " + placeAddress);
		Uri uri = Uri.parse("geo:0,0?q=" + event.latitude + "," + event.longitude + "(" + event.caption + ")");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setPackage("com.google.android.apps.maps");
		startActivity(intent);
	}
}
