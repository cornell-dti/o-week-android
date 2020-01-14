package com.cornellsatech.o_week.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.WorkerThread;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Event;
import com.cornellsatech.o_week.models.VersionUpdate;
import com.cornellsatech.o_week.models.VersionUpdateWithTags;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.AllArgsConstructor;

/**
 * Handles ALL web-related activities for this app.
 */
public final class Internet
{
	private static final String TAG = Internet.class.getSimpleName();

	//suppress default constructor
	private Internet(){}

	/**
	 * Downloads all events and categories to update the app to the database's newest version.
	 * The {@link Callback} provided will be executed when the data has been processed. The parameter for {@link Callback#execute(Object)} will be the new version (int).
	 * A toast is sent out with titles of changed events that the user had selected.
	 * @param timestamp Current version of database on file. Should be 0 if never downloaded from database.
	 * @param onCompletion Function to execute when data is processed. String in parameter is new version.
	 */
	public static void getUpdatesForVersion(long timestamp, final Callback<VersionUpdate> onCompletion)
	{
        Log.i(TAG, "Updating with timestamp: " + timestamp);
		new GET("http://scraperjanorientationcornell.herokuapp.com/events/", new Callback<String>()
		{
			@Override
			public void execute(String msg)
			{
				onCompletion.execute(msg.isEmpty() ? null : VersionUpdateWithTags.fromJSON(msg));
			}
		}).execute();
	}

	public static void getResources(final Context context) {
		new GET("https://us-east1-oweek-1496849141291.cloudfunctions.net/getResources", new Callback<String>()
		{
			@Override
			public void execute(String msg)
			{
				try
				{
					JSONArray json = new JSONArray(msg);
					for (int i = 0; i < json.length(); i++)
					{
						JSONObject resource = json.getJSONObject(i);
						String name = resource.getString("name");
						String link = resource.getString("link");
						UserData.resourceNameLink.put(name, link);
					}

					Settings.setResources(UserData.resourceNameLink, context);
				}
				catch (JSONException e)
				{
					Log.e(TAG, "Could not parse resources: " + msg);
				}
			}
		}).execute();
	}
	/**
	 * Try to download image from the internet to the given {@link ImageView}.
	 *
	 * @param event Event to get image for
	 * @param imageView View to display image
	 * @param layout Layout to display {@link Snackbar} error
	 */
	public static void getImageForEvent(final Event event, final ImageView imageView, final CoordinatorLayout layout)
	{
		new GetImage(event.getImg(), new Callback<Bitmap>()
		{
			@Override
			public void execute(Bitmap bitmap)
			{
				if (bitmap == null)
					Snackbar.make(layout, R.string.snackbar_image_error, Snackbar.LENGTH_SHORT).show();
				else
					imageView.setImageBitmap(bitmap);
			}
		}).execute();
	}
	/**
	 * Opens the user to the website given.
	 * @param url Site address.
	 */
	public static void openToPage(String url, Context context)
	{
		Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
		websiteIntent.setData(Uri.parse(url));
		context.startActivity(websiteIntent);
	}

	/**
	 * Retrieves data from {@link #urlString} and calls {@link #callback} with it.
	 * Must be private static class to prevent memory leaks from inner classes.
	 * Extends AsyncTask since going on internet may lag app & should be done in bg.
	 *
	 * @see <a href="https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur">StackOverFlow</a>
	 */
	@AllArgsConstructor
	private static class GET extends AsyncTask<Void, Void, String>
	{
		private final String urlString;
		private final Callback<String> callback;

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
				Log.i(TAG, "GET from database succeeded");
				return body;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.e(TAG, "GET from database failed :(");
			}
			return "";
		}

		@Override
		protected void onPostExecute(String body)
		{
			callback.execute(body);
		}
	}

	@AllArgsConstructor
	private static class GetImage extends AsyncTask<Void, Void, Bitmap>
	{
		private final String urlString;
		private final Callback<Bitmap> callback;

		@Override
		@WorkerThread
		protected Bitmap doInBackground(Void... params) {
			try {
				return BitmapFactory.decodeStream(new URL(urlString).openStream());
			} catch (Exception e) {
				Log.d(TAG, "Get image from url " + urlString + " failed");
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap body)
		{
			callback.execute(body);
		}
	}
}
