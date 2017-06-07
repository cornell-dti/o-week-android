package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity
{
	public static Event event;
	private ImageView eventImage;
	private TextView titleText;
	private TextView captionText;
	private TextView startTimeText;
	private TextView endTimeText;
	private TextView descriptionText;
	private static final String TAG = DetailsActivity.class.getSimpleName();

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
	private void findViews()
	{
		eventImage = (ImageView) findViewById(R.id.eventImage);
		titleText = (TextView) findViewById(R.id.titleText);
		captionText = (TextView) findViewById(R.id.captionText);
		startTimeText = (TextView) findViewById(R.id.startTimeText);
		endTimeText = (TextView) findViewById(R.id.endTimeText);
		descriptionText = (TextView) findViewById(R.id.descriptionText);
	}
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
		startTimeText.setText(event.startTime.toString(Event.TIME_FORMAT));
		endTimeText.setText(event.endTime.toString(Event.TIME_FORMAT));
	}
}
