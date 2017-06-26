package com.cornellsatech.o_week;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.WorkerThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.ImageView;

import com.google.common.io.CharStreams;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Internet
{
	public static final String DATABASE = "https://oweekapp.herokuapp.com/flow/";
	private static final String TAG = Internet.class.getSimpleName();

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
	public static void getImageForEvent(final Event event, final ImageView imageView, final CoordinatorLayout layout, final boolean saveImage)
	{
		final File imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ event.pk + Bitmap.CompressFormat.JPEG.name().toLowerCase());
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
				Log.e(TAG, "onError: " + error.getErrorCode());
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
