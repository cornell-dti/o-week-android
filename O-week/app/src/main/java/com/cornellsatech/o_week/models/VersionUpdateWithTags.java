package com.cornellsatech.o_week.models;

import android.util.JsonReader;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;

import lombok.Value;

@Value
public class VersionUpdateWithTags {
    private static final String TAG = VersionUpdateWithTags.class.getSimpleName();
    private static final Gson GSON = new Gson();
    private final VersionUpdate.EventUpdate events;
    private final VersionUpdate.CategoryUpdate categories;
    private final long timestamp;

    public VersionUpdateWithTags(VersionUpdate.EventUpdate e, VersionUpdate.CategoryUpdate c){
        events = e;
        categories = c;
        timestamp = System.currentTimeMillis();
    }

    public static VersionUpdate fromJSON(String jsonStr) {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
            JSONArray eventsArr = json.getJSONArray("EVENTS");
            List<Event> events = new ArrayList<>();
            Set<String> categoryStrs = new HashSet<>();
            for(int i = 0; i < eventsArr.length(); i++){

                JSONObject eventJson = eventsArr.getJSONObject(i);
                String pk = eventJson.getString("EVENT_ID");
                String name = eventJson.getString("EVENT_TITLE");
                String description = eventJson.getString("EVENT_DESCRIPTION");
                String url = eventJson.getString("EVENT_EXTERNAL_URL");

                String img = eventJson.getString("EVENT_IMG");
                String add = eventJson.getString("EVENT_ADDITIONAL_FEE");
                String location = eventJson.getString("EVENT_LOCATION");
                double lat = eventJson.getDouble("EVENT_LOCATION_LAT");
                double lon = eventJson.getDouble("EVENT_LOCATION_LON");
                JSONObject timeObj = eventJson.getJSONObject("EVENT_TIMES");
                String startTimeStr = timeObj.getString("TIME_START");
                // Time parsing
                DateTimeFormatter sdf = DateTimeFormat.forPattern("MMM, dd YYYY HH:mm:ss");
                DateTime startDate = sdf.parseDateTime(startTimeStr);
                long startDateLong = startDate.getMillis();
                String endTimeStr = timeObj.getString("TIME_END");
                DateTime endDate = sdf.parseDateTime(endTimeStr);
                long endDateLong = endDate.getMillis();
                JSONArray categoriesJson = eventJson.getJSONArray("EVENT_TAGS");
                List<String> categories = new ArrayList<>();
                for(int z = 0; z < categoriesJson.length(); z++){
                    String categoryStr = categoriesJson.getString(z);
                    categoryStrs.add(categoryStr);
                    categories.add(categoryStr);
                }
                boolean firstYearReq = false;
                boolean transferReq = true;

                Event e = new Event(pk, name, description, url, img, add, location, lon, lat, startDateLong, endDateLong, categories, firstYearReq, transferReq);
                events.add(e);
            }
            List<Category> updatedCategories = new ArrayList<>();
            List<String> categoriesStr = new ArrayList<>(categoryStrs);
            Collections.sort(categoriesStr);
            for(int i = 0; i < categoriesStr.size(); i++){
                updatedCategories.add(new Category(String.valueOf(i), categoriesStr.get(i)));
            }
            VersionUpdate.EventUpdate eu = new VersionUpdate.EventUpdate(events, new ArrayList<String>());
            VersionUpdate.CategoryUpdate cu = new VersionUpdate.CategoryUpdate(updatedCategories, new ArrayList<String>());
            return new VersionUpdate(eu, cu, System.currentTimeMillis());


        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
}
