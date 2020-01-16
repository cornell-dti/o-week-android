package com.cornellsatech.o_week;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

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
 * <p>
 * {@link #EVENT_KEY}: See {@link #onCreate(Bundle)}.
 * {@link #event}: The event displayed to the user.
 * {@link #coordinatorLayout}: Layout that will be shouldActUpon to
 * {@link Internet#getImageForEvent(Event, ImageView, CoordinatorLayout)}.
 * A reference to the {@link CoordinatorLayout} is necessary to display
 * {@link com.google.android.material.snackbar.Snackbar}.
 */
public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback, Button.OnClickListener
{
	public static final String EVENT_KEY = "event";
	private Event event;
	private CoordinatorLayout coordinatorLayout;
	private ImageView eventImage;
	private TextView titleText;
	private TextView locationText;
	private TextView timeText;
	private TextView descriptionText;
	private TextView urlText;
	private TextView additionalText;
	private TextView requiredLabel;
	private TextView requirementDetails;
	private View horizontalBreakBar;
	private Button addButton;
	private TextView moreButton;
	private View moreButtonGradient;
	private Button directionsButton;

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
		event = Event.fromJSON(getIntent().getExtras().getString(EVENT_KEY));

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
		locationText = findViewById(R.id.locationText);
		timeText = findViewById(R.id.timeText);
		urlText = findViewById(R.id.urlText);
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

		setTitle(event.getStartDate().toString(Event.DISPLAY_DATE_FORMAT));

		titleText.setText(event.getName());
		locationText.setText(event.getLocation());
		descriptionText.setText(event.getDescription());
		timeText.setText(event.getStartTime().toString(Event.DISPLAY_TIME_FORMAT) + " - " + event.getEndTime().toString(Event.DISPLAY_TIME_FORMAT));
		if (UserData.selectedEvents.contains(event))
			addButton.setText(R.string.button_text_event_added);
		addButton.setBackgroundResource(R.drawable.bg_button_selected_ripple);
		addButton.setOnClickListener(this);
		moreButton.setOnClickListener(this);
		directionsButton.setOnClickListener(this);

		configureDescription();
		configureRequired();
		configureURL();
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

		if (!event.getAdditional().isEmpty())
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
		if (!(event.isRequired()))
		{
			requiredLabel.setVisibility(View.GONE);
			requirementDetails.setVisibility(View.GONE);
			horizontalBreakBar.setVisibility(View.GONE);
		}
		else
		{
			//change color of RQ label based on whether or not it's required for this user
			boolean requiredForUser = UserData.requiredForUser(event);
			int requiredLabelBg = requiredForUser ? R.drawable.required_label :
					R.drawable.required_label_gray;
			requiredLabel.setBackground(ContextCompat.getDrawable(this, requiredLabelBg));
			requirementDetails.setText(requiredForUser ? R.string.required_for_you :
					R.string.required_for_others);
		}
	}

	/**
	 * Clear the previous image and set the new one.
	 */
	private void configureImage()
	{
		eventImage.setImageResource(0); // clear image from cache
		Internet.getImageForEvent(event, eventImage, coordinatorLayout);
	}

    private void configureURL()
    {
        if (event.getUrl() != null && !event.getUrl().isEmpty())
        {
            urlText.setVisibility(View.VISIBLE);
            urlText.setOnClickListener(this);
            urlText.setText(event.getUrl());
        }
        else
            urlText.setVisibility(View.GONE);
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
				if (UserData.selectedEvents.remove(event))
				{
					Notifications.unscheduleForEvent(event, this);
					addButton.setText(R.string.button_text_event_not_added);
					Toast.makeText(this, R.string.toast_text_event_removed, Toast.LENGTH_SHORT).show();
				}
				else
				{
					UserData.selectedEvents.add(event);
					if (Settings.getReceiveReminders(this))
						Notifications.scheduleForEvent(event, this);
					addButton.setText(R.string.button_text_event_added);
					Toast.makeText(this, R.string.toast_text_event_added, Toast.LENGTH_SHORT).show();
				}
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventSelectionChanged());
				break;
			case R.id.moreButton:
				descriptionText.setMaxLines(Integer.MAX_VALUE);
				moreButton.setVisibility(View.GONE);
				break;
			case R.id.directionsButton:
				startMap();
				break;
            case R.id.urlText:
                Internet.openToPage(event.getUrl(), this);
                break;
			default:
				Log.e(TAG, "onClick: Unknown button pressed");
		}
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
	 * Loads {@link Event#getLocation()} onto the map, adds a marker.
	 */
	@Override
	public void onMapReady(final GoogleMap map)
	{
		LatLng position = new LatLng(event.getLatitude(), event.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
		map.addMarker(new MarkerOptions().position(position).title(event.getLocation()));
	}

	/**
	 * Open the map with the event's location.
	 */
	private void startMap()
	{
		Uri uri = Uri.parse("geo:0,0?q=" + event.getLatitude() + "," + event.getLongitude() +
				"(" + Uri.encode(event.getLocation()) + ")");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setPackage("com.google.android.apps.maps");
		startActivity(intent);
	}
}
