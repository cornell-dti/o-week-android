package com.cornellsatech.o_week;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.common.io.CharStreams;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Internet
{
	public static final String DATABASE = "https://oweekapp.herokuapp.com/flow/";

	//suppress default constructor
	private Internet(){}


	public static void getEventsOnDate(final LocalDate date, final Context context)
	{
		get(DATABASE + "feed/" + date.getDayOfMonth(), new Callback()
		{
			@Override
			public void execute(String msg)
			{
				if (msg.isEmpty())
					return;

				try
				{
					JSONArray jsonArray = new JSONArray(msg);
					for (int i = 0; i < jsonArray.length(); i++)
					{
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						UserData.saveEvent(new Event(jsonObject), context);
					}

					if (date.isEqual(UserData.selectedDate))
						NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
				}
				catch (JSONException e) {e.printStackTrace();}
			}
		});
	}

	public static void getEventsOnDate(LocalDate date, String category)
	{

	}
	public static void getEventWithPk(int pk)
	{

	}
	public static void getImageForEvent(Event event)
	{

	}
	public static void getCategories()
	{

	}
	private static void get(final String urlString, final Callback callback)
	{
		//must use AsyncTask since going on internet may lag app & should be done in bg
		new AsyncTask<Void, Void, String>()
		{
			@Override
			@WorkerThread
			protected String doInBackground(Void... params)
			{
				try
				{
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					String body = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
					connection.disconnect();
					Log.i("Internet", "GET from database succeeded");
					return body;
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.wtf("Internet", "GET from database failed :(");
				}
				return "";
			}

			@Override
			protected void onPostExecute(String body)
			{
				callback.execute(body);
			}
		}.execute(null, null, null);
	}

	private interface Callback
	{
		void execute(String msg);
	}
}
