package com.cornellsatech.o_week;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{
	public static Event event;
	private ImageView eventImage;
	private TextView titleText;
	private TextView captionText;
	private TextView startTimeText;
	private TextView endTimeText;
	private TextView descriptionText;
	private CheckBox checkBox;
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
			UserData.insertToSelectedEvents(event);
		else
			UserData.removeFromSelectedEvents(event);
	}
}
