package com.cornellsatech.o_week.models;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections

import lombok.Value;

@Value
public class VersionUpdateWithTags {
    private static final String TAG = VersionUpdateWithTags.class.getSimpleName();
    private static final Gson GSON = new Gson();
    private final EventUpdate events;
    private final CategoryUpdate categories;
    private final long timestamp;

    public VersionUpdateWithTags(EventUpdate e, CategoryUpdate c){
        events = e;
        categories = c;
    }

    public static VersionUpdateWithTags fromJSON(String json) {
        List<Category> updatedCategories = new ArrayList<>();
        List<String> allCategories = new ArrayList<>();
     List<Event> events = GSON.fromJson(json, new TypeToken<List<Event>>() {}.getType());
     for(Event e: events){
            List<String> categories = e.getCategories();
            for(String c: categories) {
                allCategories.add(c);
            }
        }
     Collections.sort(allCategories);
       return new VersionUpdateWithTags(events, updatedCategories);

    }


    @Value
    public static class EventUpdate {
        private final List<Event> changed;
        private final List<String> deleted;

        public EventUpdate( List<Event> c, List<String> d){
            changed = c;
            deleted = d;
        }
    }

    @Value
    public static class CategoryUpdate {
        private final List<Category> changed;
        private final List<String> deleted;

        public CategoryUpdate( List<Category> c, List<String> d){
            changed = c;
            deleted = d;
        }
    }
}
