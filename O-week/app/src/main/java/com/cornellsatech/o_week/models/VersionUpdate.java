package com.cornellsatech.o_week.models;

import com.google.gson.Gson;

import java.util.List;

import lombok.Value;

@Value
public class VersionUpdate {
    private static final Gson GSON = new Gson();
    private final EventUpdate events;
    private final CategoryUpdate categories;
    private final long timestamp;

    public static VersionUpdate fromJSON(String json) {
        return GSON.fromJson(json, VersionUpdate.class);
    }

    @Value
    public static class EventUpdate {
        private final List<Event> changed;
        private final List<String> deleted;
    }

    @Value
    public static class CategoryUpdate {
        private final List<Category> changed;
        private final List<String> deleted;
    }
}
