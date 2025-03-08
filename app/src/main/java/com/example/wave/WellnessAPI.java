package com.example.wave;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WellnessAPI {
    @GET("/blogs")
    Call<List<Blogs_Response>> getBlogsData();
    @GET("/podcasts")
    Call<List<PodcastsResponse>> getPodcastsData();
}
