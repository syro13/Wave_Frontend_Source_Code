package com.example.wave;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface QuotesApi {
    @GET("today")
    Call<List<QuoteResponse>> getTodaysQuote();
}

