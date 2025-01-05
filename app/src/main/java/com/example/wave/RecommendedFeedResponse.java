package com.example.wave;

import java.util.List;

public class RecommendedFeedResponse {
    private List<String> recommended_feed;
    private int count;
    private String tag;
    private int page;

    public List<String> getRecommendedFeed() {
        return recommended_feed;
    }

    public int getCount() {
        return count;
    }

    public String getTag() {
        return tag;
    }

    public int getPage() {
        return page;
    }
}
