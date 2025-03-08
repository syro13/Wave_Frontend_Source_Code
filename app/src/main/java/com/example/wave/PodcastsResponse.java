package com.example.wave;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class PodcastsResponse {
    @SerializedName("Title")
    private String title;
    @SerializedName("Duration")
    private String duration;
    @SerializedName("Link")
    private String link;

    public static List<PodcastsResponse> fromJsonList(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PodcastsResponse>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public static String toJsonList(List<PodcastsResponse> podcasts) {
        Gson gson = new Gson();
        return gson.toJson(podcasts);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public PodcastsResponse getPodcasts(){
        return this;
    }
}

