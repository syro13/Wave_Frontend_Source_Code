package com.example.wave;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city, // City name
            @Query("appid") String apiKey, // Your API key
            @Query("units") String units // Metric units
    );
}

