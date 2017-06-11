package com.cornellsatech.o_week;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class FeedCell extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener
{
	private final TextView startTimeText;
	private final TextView endTimeText;
	private final TextView titleText;
	private final TextView captionText;
	private final CheckBox checkBox;
	private Event event;

	public FeedCell(View itemView)
	{
		super(itemView);
		startTimeText = (TextView) itemView.findViewById(R.id.startTimeText);
		endTimeText = (TextView) itemView.findViewById(R.id.endTimeText);
		titleText = (TextView) itemView.findViewById(R.id.titleText);
		captionText = (TextView) itemView.findViewById(R.id.captionText);
		checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
		checkBox.setOnCheckedChangeListener(this);
		itemView.setOnClickListener(this);
	}

	public void configure(Event event, boolean selected)
	{
		this.event = event;
		startTimeText.setText(event.startTime.toString(Event.DISPLAY_TIME_FORMAT));
		endTimeText.setText(event.endTime.toString(Event.DISPLAY_TIME_FORMAT));
		titleText.setText(event.title);
		captionText.setText(event.caption);
		checkBox.setChecked(selected);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
			UserData.insertToSelectedEvents(event);
		else
			UserData.removeFromSelectedEvents(event);
	}
	@Override
	public void onClick(View v)
	{
		NotificationCenter.DEFAULT.post(new NotificationCenter.EventEventClicked(event));
	}
}
