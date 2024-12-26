package com.example.wave;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class BlogResponse {
    private String title;
    private String author;
    private String tag;
    private String link;
    private String imageUrl;

    // Constructor
    public BlogResponse(String title, String author, String tag, String link, String imageUrl) {
        this.title = title;
        this.author = author;
        this.tag = tag;
        this.link = link;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getTag() {
        return tag;
    }

    public String getLink() {
        return link;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Convert List<BlogResponse> to JSON String
    public static String toJsonList(List<BlogResponse> blogs) {
        Gson gson = new Gson();
        return gson.toJson(blogs);
    }

    // Convert JSON String to List<BlogResponse>
    public static List<BlogResponse> fromJsonList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<BlogResponse>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
