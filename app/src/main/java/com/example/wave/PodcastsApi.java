package com.example.wave;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface PodcastsApi {
    @GET("search")
    Call<PodcastSearchResponse> searchPodcasts(
            @Query("q") String query,
            @Query("type") String type,
            @Query("len_min") int minLength,
            @Query("len_max") int maxLength,
            @Query("safe_mode") int safeMode, // Already included
            @Query("sort_by_date") int sortByDate // New parameter added
    );
}

