package com.cornellsatech.o_week;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.models.StudentType;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Notifications;
import com.cornellsatech.o_week.util.Settings;
import com.google.common.eventbus.Subscribe;

import java.util.List;
import java.util.Set;

/**
 * Controls interactions of the user initial settings. (student type, whether is international, which college)
 * <p>
 * {@Link #INITIAL_SETTINGS_PAGENUM}: The number of pages that the initial settings contains.
 * {@link #waitingOnEventDownload}: True if the user is ready to end the tutorial but the events in
 * {@link UserData} have yet to be downloaded.
 */

public class InitialSettingsActivity extends AppCompatActivity
{
	private final int INITIAL_SETTINGS_PAGENUM = 3;
	private ConstraintLayout constraintLayout;
	private ViewPager pager;
	private InitialSettingsPagerAdapter adapter;
	private StudentType studentType = StudentType.NOTSET;
	private CollegeType collegeType = CollegeType.NOTSET;
	private boolean waitingOnEventDownload = false;
	private static final String TAG = InitialSettingsActivity.class.getSimpleName();

	/**
	 * Initialize the view pager for initial settings. the view pager is locked so that user cannot scroll to bypass the selection.
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		NotificationCenter.DEFAULT.register(this);
		setContentView(R.layout.initial_settings_pager);

		constraintLayout = findViewById(R.id.constraintLayout);
		pager = findViewById(R.id.initialSettingsPager);
		adapter = new InitialSettingsPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
	}

	/**
	 * Unregister this as a listener.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		NotificationCenter.DEFAULT.unregister(this);
	}

	/**
	 * Switch to the next page of the initial settings. If all settings are complete, this activity is finished.
	 */
	public void switchToNextPage()
	{
		if (pager.getCurrentItem() < INITIAL_SETTINGS_PAGENUM - 1)
		{
			if (pager.getCurrentItem() == adapter.getProgress())
			{
				adapter.advanceProgress();
				adapter.notifyDataSetChanged();
			}
			pager.setCurrentItem(pager.getCurrentItem() + 1, true);
		}
		else if (studentType != StudentType.NOTSET && collegeType != CollegeType.NOTSET)
		{
			attemptFinish();
		}
	}

	/**
	 * Attempt to add the required events for the user and end the tutorial. Will stall until the
	 * events are downloaded.
	 *
	 * Does not save the student type and the college type until the events
	 * are updated, otherwise if the app is closed while we're waiting for events to update,
	 * next time the app is opened, it'll skip through the tutorial and no events will be added.
	 */
	private void attemptFinish()
	{
		if (Settings.getVersion(this) == 0)
		{
			waitingOnEventDownload = true;
			showRetryDownloadSnackbar();
		}
		else
		{
			Settings.setStudentInfo(this, studentType, collegeType);
			UserData.loadStudentCollegeTypes(this);

			addRequiredEvents();
			Toast.makeText(this, R.string.schedule_auto_add_notice, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * Add all required events for the user to schedule. (Selected events)
	 */
	private void addRequiredEvents()
	{
		for (List<Event> eventsForDay : UserData.allEvents.values())
		{
			for (Event e : eventsForDay)
			{
				if (!UserData.requiredForUser(e))
					continue;
				UserData.insertToSelectedEvents(e);
				NotificationCenter.DEFAULT.post(new NotificationCenter.EventSelectionChanged(e, true));
			}
		}

		Notifications.scheduleForEvents(this);
	}

	/**
	 * Make sure when back is pressed, the app goes home instead of the main activity so that user cannot bypass the selection.
	 */
	@Override
	public void onBackPressed()
	{
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	/**
	 * Sets {@Link #studentType}
	 *
	 * @param s
	 */
	public void setStudentType(StudentType s)
	{
		studentType = s;
	}

	/**
	 * Sets {@Link #collegeType}
	 *
	 * @param c
	 */
	public void setCollegeType(CollegeType c)
	{
		collegeType = c;
	}

	/**
	 * Listens to {@link com.cornellsatech.o_week.util.NotificationCenter.EventReload} events, which
	 * indicate that an attempt to download events has completed.
	 * If {@link #waitingOnEventDownload} is true, then the user has completed the tutorial but
	 * the events have yet to finish downloading. Tell the user and let him try again.
	 *
	 * @param e Ignored.
	 */
	@Subscribe
	public void onEventsReload(NotificationCenter.EventReload e)
	{
		if (waitingOnEventDownload)
		{
			//download successful
			if (Settings.getVersion(this) != 0)
				attemptFinish();
			//download failed
			else
				showRetryDownloadSnackbar();
		}
	}

	/**
	 * Show a snackbar that allows the user to redownload events from the database.
	 */
	private void showRetryDownloadSnackbar()
	{
		final Context context = this;
		Snackbar.make(constraintLayout, R.string.initial_settings_redownload, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.dialog_retry_button, new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						UserData.loadData(context);
					}
				})
				.show();
	}
}
