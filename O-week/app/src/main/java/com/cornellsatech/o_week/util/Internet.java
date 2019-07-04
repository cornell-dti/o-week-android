package com.cornellsatech.o_week.util;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.WorkerThread;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.cornellsatech.o_week.R;
import com.cornellsatech.o_week.UserData;
import com.cornellsatech.o_week.models.Category;
import com.cornellsatech.o_week.models.Event;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Joiner;
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
	public static final String ORIENTATION_PAMPHLET = "https://ccengagement.cornell.edu/sites/ccengagement.cornell.edu/files/rnsp/documents/orient_guide_080718_cornell.pdf";
	public static final String CAMPUS_MAP = "https://www.cornell.edu/about/maps/cornell-campus-map-2015.pdf";
	public static final String NEW_STUDENTS_WEBPAGE = "https://newstudents.cornell.edu/fall-2018/first-year/orientation";
	public static final String DTI = "http://cornelldti.org/";
	public static final String IMAGE_DIRECTORY = "OweekImages";
	private static final String TAG = Internet.class.getSimpleName();

	//suppress default constructor
	private Internet(){}

	/**
	 * Downloads all events and categories to update the app to the database's newest version.
	 * The {@link Callback} provided will be executed when the data has been processed. The parameter for {@link Callback#execute(Object)} will be the new version (int).
	 * A toast is sent out with titles of changed events that the user had selected.
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
	public static void getUpdatesForVersion(int version, final Context context, final Callback<Integer> onCompletion)
	{
		get(DATABASE + "version/" + version, new Callback<String>()
		{
			@Override
			public void execute(String msg)
			{
				if (msg.isEmpty())
				{
					onCompletion.execute(0);
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
					SparseArray<String> changedEventsTitleAndPk = new SparseArray<>();
					//update/remove notifications
					boolean remindersOn = Settings.getReceiveReminders(context);
					//update events
					for (int i = 0; i < changedEvents.length(); i++)
					{
						JSONObject eventJSON = changedEvents.getJSONObject(i);
						Event event = new Event(eventJSON);
						changedEventsTitleAndPk.put(event.pk, event.title);
						UserData.removeFromAllEvents(event);
						UserData.appendToAllEvents(event);

						//reschedule the event
						if (UserData.selectedEventsContains(event))
						{
							UserData.removeFromSelectedEvents(event);
							UserData.insertToSelectedEvents(event);
							if (remindersOn)
								Notifications.scheduleForEvent(event, context);
						}
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
								changedEventsTitleAndPk.put(event.pk, event.title);
								eventsIterator.remove();

								//remove the event from notification
								if (remindersOn)
									Notifications.unscheduleForEvent(event, context);
							}
						}
					}

					onCompletion.execute(newestVersion);
					NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());

					//send a toast to alert the user that their events were updated
					if (changedEventsTitleAndPk.size() != 0)
					{
						Set<String> pkStrings = Settings.getSelectedEventsPks(context);

						//show toast only for changed events that you HAD selected
						List<String> selectedChangedEventsTitles = new ArrayList<>();
						for (String pkString : pkStrings)
						{
							int pk = Integer.parseInt(pkString);
							String selectedChangedEventTitle = changedEventsTitleAndPk.get(pk);
							if (selectedChangedEventTitle != null)
								selectedChangedEventsTitles.add(selectedChangedEventTitle);
						}
						if (!selectedChangedEventsTitles.isEmpty())
						{
							String toastText = context.getString(R.string.toast_events_changed, Joiner.on(", ").join(selectedChangedEventsTitles));
							Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
						}
					}
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
				+ File.separator + IMAGE_DIRECTORY + File.separator + event.pk + ".jpg");
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
	 * Connects to the website given, then calls {@link Callback#execute(Object)} with the output
	 * received from the website as the String parameter.
	 * Identical to a GET request.
	 *
	 * @param urlString Link to the website
	 * @param callback Contains method to execute once the website responds
	 */
	private static void get(final String urlString, final Callback<String> callback)
	{
		new GET(urlString, callback).execute();
	}

	/**
	 * Retrieves data from {@link #URL_STRING} and calls {@link #CALLBACK} with it.
	 * Must be private static class to prevent memory leaks from inner classes.
	 * Extends AsyncTask since going on internet may lag app & should be done in bg.
	 *
	 * @see #get(String, Callback)
	 * @see <a href="https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur">StackOverFlow</a>
	 */
	private static class GET extends AsyncTask<Void, Void, String>
	{
		private final String URL_STRING;
		private final Callback<String> CALLBACK;

		private GET(String urlString, Callback<String> callback)
		{
			URL_STRING = urlString;
			CALLBACK = callback;
		}
		@Override
		@WorkerThread
		protected String doInBackground(Void... params)
		{
			try
			{
				URL url = new URL(URL_STRING);
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
			CALLBACK.execute(body);
		}
	}
}
