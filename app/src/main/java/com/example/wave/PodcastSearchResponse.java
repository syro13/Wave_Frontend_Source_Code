package com.example.wave;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


// Root Response Model for Podcast Search
public class PodcastSearchResponse {
    @SerializedName("results")
    private List<Podcast> podcasts;

    public List<Podcast> getPodcasts() {
        return podcasts;
    }
    public static List<Podcast> fromJsonList(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Podcast>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    public static String toJsonList(List<Podcast> podcasts) {
        Gson gson = new Gson();
        return gson.toJson(podcasts);
    }

}

