package com.example.wave;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class Blogs_Response {
    @SerializedName("Title")
    private String title;
    @SerializedName("Tag")
    private String tag;
    @SerializedName("Link")
    private String link;
    @SerializedName("Image")
    private String image;

    public static List<Blogs_Response> fromJsonList(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Blogs_Response>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public static String toJsonList(List<Blogs_Response> blogs) {
        Gson gson = new Gson();
        return gson.toJson(blogs);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
