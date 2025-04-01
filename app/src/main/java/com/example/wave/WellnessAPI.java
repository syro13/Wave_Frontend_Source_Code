package com.example.wave;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface WellnessAPI {
    @GET("/blogs")
    Call<List<Blogs_Response>> getBlogsData(@Header("X-API-Key") String apiKey);
    @GET("/podcasts")
    Call<List<PodcastsResponse>> getPodcastsData(@Header("X-API-Key") String apiKey);
}
