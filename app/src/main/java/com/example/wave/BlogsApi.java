package com.example.wave;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BlogsApi {

    @Headers({
            "x-rapidapi-key: 29917425cdmsh96de0dd42300380p175d46jsn3a36125902cf", // Replace with your API key
            "x-rapidapi-host: medium2.p.rapidapi.com"
    })
    @GET("recommended_feed/{tag}")
    Call<RecommendedFeedResponse> getRecommendedFeed(
            @Path("tag") String tag,
            @Query("page") int page
    );
    @Headers({
            "x-rapidapi-key: 29917425cdmsh96de0dd42300380p175d46jsn3a36125902cf", // Replace with your API key
            "x-rapidapi-host: medium2.p.rapidapi.com"
    })
    @GET("article/{article_id}")
    Call<ArticleDetails> getArticleInfo(
            @Path("article_id") String articleId
    );
}
