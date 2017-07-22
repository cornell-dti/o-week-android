package com.cornellsatech.o_week;


import android.app.Application;

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
		JodaTimeAndroid.init(this);
		UserData.loadData(this);
	}
}
