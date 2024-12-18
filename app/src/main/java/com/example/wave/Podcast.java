package com.example.wave;

public class Podcast {
    private String title;
    private String duration;

    public Podcast(String title, String duration) {
        this.title = title;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }
}
