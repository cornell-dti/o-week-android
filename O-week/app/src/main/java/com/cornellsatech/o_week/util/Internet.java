package com.cornellsatech.o_week.util;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.WorkerThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.ImageView;

import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;
import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Handles ALL web-related activities for this app.
 *
 * {@link #DATABASE}: Link to the database.
 */
public final class Internet
{
	public static final String DATABASE = "https://oweekapp.herokuapp.com/flow/";
	public static final String ORIENTATION_PAMPHLET = "http://ccengagement.cornell.edu/sites/ccengagement.cornell.edu/files/a3c/cornell_orientation_guide_08_2017.pdf";
	public static final String CAMPUS_MAP = "https://www.cornell.edu/about/maps/cornell-campus-map-2015.pdf";
	public static final String NEW_STUDENTS_WEBPAGE = "https://newstudents.cornell.edu/fall-2017/first-year/cornell-orientation-august-18-21-2017";
	public static final String CORNELL_RESCUER = "market://details?id=cornell.sa.rescuer";
	private static final String TAG = Internet.class.getSimpleName();

	//suppress default constructor
	private Internet(){}

	/**
	 * Downloads all events and categories to update the app to the database's newest version.
	 * The {@link Callback} provided will be executed when the data has been processed. The String msg used
	 * as the parameter for {@link Callback#execute(String)} will be the string for the new version (int).
	 * A notification is sent out (with titles of changed events).
	 *
	 * Note: {@link UserData#selectedEvents} will not be updated by this method.
	 *       {@link UserData#categories} and {@link UserData#allEvents} should already be filled with events
	 *       loaded from {@link android.content.SharedPreferences}.
	 *
	 * Expected JSON structure:
	 * {
	 *     version: Int,
	 *     categories:
	 *     {
	 *         changed: [{@link Category#Category(JSONObject)}, category2, ...],
	 *         deleted: [{@link Category#pk}, pk2, ...]
	 *     },
	 *     events:
	 *     {
	 *         changed: [{@link Event#Event(JSONObject)}, event2, ...],
	 *         deleted: [{@link Event#pk}, pk2, ...]
	 *     }
	 * }
	 *
	 * @param version Current version of database on file. Should be 0 if never downloaded from database.
	 * @param onCompletion Function to execute when data is processed. String in parameter is new version.
	 */
	public static void getUpdatesForVersion(int version, final Context context, final Callback onCompletion)
	{
		get(DATABASE + "version/" + version, new Callback()
		{
			@Override
			public void execute(String msg)
			{
				if (msg.isEmpty())
				{
					onCompletion.execute(null);
					NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
					return;
				}
				try
				{
					JSONObject json = new JSONObject(msg);
					int newestVersion = json.getInt("version");
					JSONObject categories = json.getJSONObject("categories");
					JSONArray changedCategories = categories.getJSONArray("changed");
					JSONArray deletedCategories = categories.getJSONArray("deleted");
					JSONObject events = json.getJSONObject("events");
					JSONArray changedEvents = events.getJSONArray("changed");
					JSONArray deletedEvents = events.getJSONArray("deleted");

					//update categories
					for (int i = 0; i < changedCategories.length(); i++)
					{
						JSONObject categoryJSON = changedCategories.getJSONObject(i);
						Category category = new Category(categoryJSON);
						UserData.categories.remove(category);   //this works because categories are compared using pk
						UserData.categories.add(category);
					}
					//delete categories
					Set<Integer> deletedCategoriesPks = new HashSet<>(deletedCategories.length());
					for (int i = 0; i < deletedCategories.length(); i++)
						deletedCategoriesPks.add(deletedCategories.getInt(i));
					Iterator<Category> categoriesIterator = UserData.categories.iterator();
					while (categoriesIterator.hasNext())
					{
						Category category = categoriesIterator.next();
						if (deletedCategoriesPks.contains(category.pk))
							categoriesIterator.remove();
					}

					//keep track of all changed events to notify the user
					List<String> changedEventsTitles = new ArrayList<>();
					//update events
					for (int i = 0; i < changedEvents.length(); i++)
					{
						JSONObject eventJSON = changedEvents.getJSONObject(i);
						Event event = new Event(eventJSON);
						changedEventsTitles.add(event.title);
						UserData.removeFromAllEvents(event);
						UserData.appendToAllEvents(event);
					}
					//delete events
					Set<Integer> deletedEventsPks = new HashSet<>(deletedEvents.length());
					for (int i = 0; i < deletedEvents.length(); i++)
						deletedEventsPks.add(deletedEvents.getInt(i));
					for (List<Event> eventsForDay : UserData.allEvents.values())
					{
						Iterator<Event> eventsIterator = eventsForDay.iterator();
						while (eventsIterator.hasNext())
						{
							Event event = eventsIterator.next();
							if (deletedEventsPks.contains(event.pk))
							{
								changedEventsTitles.add(event.title);
								eventsIterator.remove();
							}
						}
					}

					onCompletion.execute(String.valueOf(newestVersion));
					NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
					Notifications.createForChangedEvents(changedEventsTitles, context);
				}
				catch (JSONException e) {e.printStackTrace();}
			}
		});
	}
	/**
	 * Try to assign an image to the given {@link ImageView}.
	 * Attempts the following, in order:
	 * 1. Read image from disk.
	 * 2. If above fails, download image from the internet.
	 * 3. Save downloaded image if saveImage is true.
	 *
	 * Note: Images are saved as "{@link Event#pk}.jpg"
	 *
	 * @param event Event to get image for
	 * @param imageView View to display image
	 * @param layout Layout to display {@link Snackbar}
	 * @param saveImage If true, we'll attempt to save the image we downloaded.
	 */
	public static void getImageForEvent(final Event event, final ImageView imageView, final CoordinatorLayout layout, final boolean saveImage)
	{
		final File imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + event.pk + Bitmap.CompressFormat.JPEG.name().toLowerCase());
		if (imageFile.exists())
		{
			try
			{
				Bitmap image = BitmapFactory.decodeStream(new FileInputStream(imageFile));
				imageView.setImageBitmap(image);
				Log.i(TAG, "getImageForEvent: retrieved from memory");
				return;
			}
			catch (FileNotFoundException e) {e.printStackTrace();}
		}

		new ImageDownloader(new ImageDownloader.OnImageLoaderListener()
		{
			@Override
			public void onError(ImageDownloader.ImageError error)
			{
				Log.e(TAG, "image download error: " + error.getErrorCode());
				Snackbar.make(layout, R.string.snackbar_image_error, Snackbar.LENGTH_SHORT).show();
			}
			@Override
			public void onProgressChange(int percent) {}
			@Override
			public void onComplete(Bitmap result)
			{
				if (saveImage)
				{
					ImageDownloader.writeToDisk(imageFile, result, new ImageDownloader.OnBitmapSaveListener()
					{
						@Override
						public void onBitmapSaved() {}
						@Override
						public void onBitmapSaveError(ImageDownloader.ImageError error)
						{
							Log.e(TAG, "onBitmapSaveError: " + error.getMessage());
						}
					}, Bitmap.CompressFormat.JPEG, false);
				}
				imageView.setImageBitmap(result);
			}
		}).download(DATABASE + "event/" + event.pk + "/image", false);
	}
	/**
	 * Opens the user to the website given.
	 * @param url Site address.
	 * @param context
	 */
	public static void openToPage(String url, Context context)
	{
		Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
		websiteIntent.setData(Uri.parse(url));
		context.startActivity(websiteIntent);
	}
	/**
	 * Connects to the website given, then calls {@link Callback#execute(String)} with the output
	 * received from the website as the String parameter.
	 * Identical to a GET request.
	 *
	 * @param urlString Link to the website
	 * @param callback Contains method to execute once the website responds
	 */
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
					Log.e("Internet", "GET from database failed :(");
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

	/**
	 * Alternative to functional programming in Java 7. A wrapper for a function that takes a String.
	 */
	public interface Callback
	{
		void execute(String msg);
	}
}
