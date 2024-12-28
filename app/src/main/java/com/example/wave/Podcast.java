package com.example.wave;

import com.google.gson.annotations.SerializedName;

public class Podcast {

    @SerializedName("title_original")
    private String title;

    @SerializedName("audio_length_sec")
    private int lengthSeconds;

    @SerializedName("link")
    private String link;

    public String getTitle() {
        return title;
    }

    public int getLengthMinutes() {
        return lengthSeconds / 60; // Convert seconds to minutes
    }

    public String getLink() {
        return link; // External link to the podcast episode
    }
}
