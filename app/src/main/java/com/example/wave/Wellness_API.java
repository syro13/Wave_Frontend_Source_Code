package com.example.wave;

import java.util.ArrayList;
import java.util.List;

public class Wellness_API {
    private List<PodcastsResponse> podcasts;
    private List<Blogs_Response> blogs;

    public Wellness_API() {
    }

    public Wellness_API(List<PodcastsResponse> podcasts, List<Blogs_Response> blogs) {
        this.podcasts = podcasts;
        this.blogs = blogs;
    }

    public List<PodcastsResponse> getPodcasts() {
        return podcasts;
    }

    public void setPodcasts(List<PodcastsResponse> podcasts) {
        this.podcasts = podcasts;
    }

    public List<Blogs_Response> getBlogs() {
        return blogs;
    }

    public void setBlogs(List<Blogs_Response> blogs) {
        this.blogs = blogs;
    }

    public void clear() {
        this.podcasts.clear();
    }

    public int size() {
        return podcasts.size();
    }

    public List<PodcastsResponse> subList(int maxPodcasts, int size) {
        return podcasts.subList(maxPodcasts, size);
    }

    public boolean isEmpty() {
        if (podcasts.isEmpty()){
            return true;
        }else {
            return false;
        }
    }
}
