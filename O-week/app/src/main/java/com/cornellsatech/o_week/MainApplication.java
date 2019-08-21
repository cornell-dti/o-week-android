package com.cornellsatech.o_week;

import android.app.Application;
import android.content.Intent;

import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.StudentType;
import com.cornellsatech.o_week.util.Settings;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * The entry point into the app. Initialize all singletons, static variables, or anything that should only
 * run once when the app launches here.
 */
public class MainApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		Settings.clearAllForNewVersion(this);
		JodaTimeAndroid.init(this);
		UserData.loadData(this);

		//if the user never filled out his info, he needs to do so.
		if (Settings.getStudentSavedType(this) == StudentType.NOTSET ||
				Settings.getStudentSavedCollegeType(this) == CollegeType.NOTSET)
		{
			Intent intent = new Intent(this, InitialSettingsActivity.class);
			// TODO This fixes the crash for now (this may mess up activity history though)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
