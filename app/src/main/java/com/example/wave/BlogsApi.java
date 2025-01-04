package com.example.wave;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BlogsApi {

        @GET("recommended_feed/{tag}")
        Call<RecommendedFeedResponse> getRecommendedFeed(
                @Path("tag") String tag,
                @Query("page") int page
        );

        @GET("article/{article_id}")
        Call<ArticleDetails> getArticleInfo(
                @Path("article_id") String articleId
        );

        @GET("user/{user_id}")
        Call<UserInfo> getUserInfo(
                @Path("user_id") String userId
        );
    }
