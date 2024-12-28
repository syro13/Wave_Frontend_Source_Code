package com.example.wave;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WellnessActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WellnessPrefs";
    private static final String KEY_QUOTE = "quoteText";
    private static final String KEY_AUTHOR = "quoteAuthor";
    private static final String PREFS_BLOGS = "PrefsBlogs";
    private static final String PREFS_PODCASTS = "PrefsPodcasts";
    private static final String KEY_LAST_FETCH = "lastFetchDate";
    private static final int MAX_BLOGS = 3;
    private static final int MAX_PODCASTS = 3;

    private ImageView quoteImage;
    private TextView quoteText, quoteAuthor,noPodcastsText,noBlogsText;
    private RecyclerView blogRecyclerView, podcastRecyclerView;
    private BlogAdapter blogAdapter;
    private PodcastAdapter podcastAdapter;
    private ImageView noBlogsImage, noPodcastsImage;
    private ProgressBar loadingIndicator;

    private final List<BlogResponse> blogs = new ArrayList<>();
    private final List<Podcast> podcasts = new ArrayList<>();
    private int loadingTasksRemaining = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);

        // Initialize views
        quoteImage = findViewById(R.id.quoteImage);
        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);
        blogRecyclerView = findViewById(R.id.blogRecyclerView);
        podcastRecyclerView = findViewById(R.id.podcastRecyclerView);
        noPodcastsImage = findViewById(R.id.noPodcastsImage);
        noPodcastsText = findViewById(R.id.noPodcastsText);
        noBlogsImage = findViewById(R.id.noBlogsImage);
        noBlogsText = findViewById(R.id.noBlogsText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Start with progress bar visible
        loadingIndicator.setVisibility(View.VISIBLE);

        // Increment the loading task counter for each task
        loadingTasksRemaining = 3;

        // Start loading tasks
        updateQuoteImage();
        fetchTodaysQuoteWithCache();
        setupBlogsRecyclerView();
        setupPodcastsRecyclerView();
        loadCachedBlogs();
        loadCachedPodcasts();
    }

    private void updateQuoteImage() {
        int[] images = {
                R.drawable.quote1,
                R.drawable.quote2,
                R.drawable.quote3,
                R.drawable.quote4,
                R.drawable.quote5,
                R.drawable.quote6
        };

        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % images.length;

        quoteImage.setImageResource(images[index]);
    }

    private void fetchTodaysQuoteWithCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedQuote = prefs.getString(KEY_QUOTE, null);
        String cachedAuthor = prefs.getString(KEY_AUTHOR, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);

        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedQuote != null && cachedAuthor != null && lastFetchDate == todayDate) {
            quoteText.setText(cachedQuote);
            quoteAuthor.setText(cachedAuthor);
            taskCompleted();
        } else {
            fetchTodaysQuoteFromApi(prefs, todayDate);
        }
    }

    private void fetchTodaysQuoteFromApi(SharedPreferences prefs, int todayDate) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://zenquotes.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuotesApi api = retrofit.create(QuotesApi.class);

        api.getTodaysQuote().enqueue(new Callback<List<QuoteResponse>>() {
            @Override
            public void onResponse(Call<List<QuoteResponse>> call, Response<List<QuoteResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    QuoteResponse quote = response.body().get(0);
                    String formattedQuote = "\"" + quote.getText() + "\"";
                    quoteText.setText(formattedQuote);
                    quoteAuthor.setText(quote.getAuthor());

                    prefs.edit()
                            .putString(KEY_QUOTE, formattedQuote)
                            .putString(KEY_AUTHOR, quote.getAuthor())
                            .putInt(KEY_LAST_FETCH, todayDate)
                            .apply();
                } else {
                    quoteText.setText("Unable to load today's quote.");
                    quoteAuthor.setText("");
                }
                taskCompleted();
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                quoteText.setText("Unable to load today's quote.");
                quoteAuthor.setText("");
                taskCompleted();
            }
        });
    }

    private void setupPodcastsRecyclerView() {
        podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        podcastAdapter = new PodcastAdapter(this, podcasts);
        podcastRecyclerView.setAdapter(podcastAdapter);
    }

    private void loadCachedPodcasts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String cachedPodcastsJson = prefs.getString(PREFS_PODCASTS, null);
//        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
//
//        if (cachedPodcastsJson != null && lastFetchDate == todayDate) {
//            podcasts.clear();
//            podcasts.addAll(PodcastSearchResponse.fromJsonList(cachedPodcastsJson));
//
//            if (podcasts.size() > MAX_PODCASTS) {
//                podcasts.subList(MAX_PODCASTS, podcasts.size()).clear();
//            }
//
//            podcastAdapter.notifyDataSetChanged();
//           noPodcastsImage.setVisibility(View.GONE);
//           noPodcastsText.setVisibility(View.GONE);
//            taskCompleted();
//        } else {
//            fetchPodcastsFromApi(prefs, todayDate);
//        }
        fetchPodcastsFromApi(prefs, todayDate);
    }

    private void fetchPodcastsFromApi(SharedPreferences prefs, int todayDate) {
        PodcastsApi api = RetrofitClient.getRetrofitInstance(
                this,
                "https://listen-api.listennotes.com/api/v2/",
                "X-ListenAPI-Key",
                getResources().getString(R.string.podcast_api_key)
        ).create(PodcastsApi.class);

        api.searchPodcasts("wellness", "episode", 10, 30, 1, 1).enqueue(new Callback<PodcastSearchResponse>() {
            @Override
            public void onResponse(Call<PodcastSearchResponse> call, Response<PodcastSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    podcasts.clear();
                    podcasts.addAll(response.body().getPodcasts());

                    if (podcasts.size() > MAX_PODCASTS) {
                        podcasts.subList(MAX_PODCASTS, podcasts.size()).clear();
                    }

                    podcastAdapter.notifyDataSetChanged();
                    savePodcastsToCache(podcasts, todayDate);

                    // Hide placeholders when data is fetched
                    noPodcastsImage.setVisibility(View.GONE);
                    noPodcastsText.setVisibility(View.GONE);
                } else {
                    // Show placeholders if the response is empty or invalid
                    showNoPodcastsAvailable();
                }
                taskCompleted();
            }

            @Override
            public void onFailure(Call<PodcastSearchResponse> call, Throwable t) {
                Log.e("WellnessActivity", "Error fetching podcasts", t);
                showNoPodcastsAvailable();
                taskCompleted();
            }
        });
    }



    private void savePodcastsToCache(List<Podcast> podcasts, int todayDate) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_PODCASTS, PodcastSearchResponse.toJsonList(podcasts));
        editor.putInt(KEY_LAST_FETCH, todayDate);
        editor.apply();
    }

    private void showNoPodcastsAvailable() {
        if (podcasts.isEmpty()) {
            noPodcastsImage.setVisibility(View.VISIBLE);
            noPodcastsText.setVisibility(View.VISIBLE);
            taskCompleted();
        }
    }

    private void setupBlogsRecyclerView() {
        blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        blogAdapter = new BlogAdapter(this, blogs);
        blogRecyclerView.setAdapter(blogAdapter);
    }

    private void loadCachedBlogs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
//        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

//        if (cachedBlogsJson != null && lastFetchDate == todayDate) {
//            blogs.clear();
//            blogs.addAll(BlogResponse.fromJsonList(cachedBlogsJson));
//
//            if (blogs.size() > MAX_BLOGS) {
//                blogs.subList(MAX_BLOGS, blogs.size()).clear();
//            }
//
//            blogAdapter.notifyDataSetChanged();
//            noBlogsImage.setVisibility(View.GONE);
//            taskCompleted();
//        } else {
//            fetchBlogsFromApi(prefs, todayDate);
//        }
        fetchBlogsFromApi(prefs, todayDate);
    }

    private void fetchBlogsFromApi(SharedPreferences prefs, int todayDate) {
        BlogsApi api = RetrofitClient.getRetrofitInstance(
                this,
                "https://medium2.p.rapidapi.com/",
                "x-rapidapi-key",
                getResources().getString(R.string.blog_api_key)
        ).create(BlogsApi.class);

        blogs.clear(); // Clear previous blog data

        String[] tags = {"self-improvement", "meditation", "wellness"};
        for (String tag : tags) {
            if (blogs.size() >= MAX_BLOGS) break; // Stop if enough blogs are fetched

            api.getRecommendedFeed(tag, 1).enqueue(new Callback<RecommendedFeedResponse>() {
                @Override
                public void onResponse(Call<RecommendedFeedResponse> call, Response<RecommendedFeedResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> recommendedFeed = response.body().getRecommendedFeed();

                        if (!recommendedFeed.isEmpty()) {
                            String firstArticleId = recommendedFeed.get(0);
                            fetchArticleDetails(api, firstArticleId, tag, prefs, todayDate);
                        } else {
                            // Show placeholders if no articles are found for the tag
                            showNoBlogsAvailable();
                        }
                    } else {
                        showNoBlogsAvailable();
                    }
                }

                @Override
                public void onFailure(Call<RecommendedFeedResponse> call, Throwable t) {
                    showNoBlogsAvailable();
                }
            });
        }
    }


    private void fetchArticleDetails(BlogsApi api, String articleId, String tag, SharedPreferences prefs, int todayDate) {
        if (blogs.size() >= MAX_BLOGS) return;

        api.getArticleInfo(articleId).enqueue(new Callback<ArticleDetails>() {
            @Override
            public void onResponse(Call<ArticleDetails> call, Response<ArticleDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArticleDetails article = response.body();
                    fetchUserInfo(api, article.getAuthor(), userFullName -> {
                        addArticleToRecyclerView(article, tag, userFullName);

                        if (blogs.size() == MAX_BLOGS) {
                            saveBlogsToCache(blogs, todayDate);
                            taskCompleted();
                        }
                    });
                } else {
                    showNoBlogsAvailable();
                }
            }

            @Override
            public void onFailure(Call<ArticleDetails> call, Throwable t) {
                showNoBlogsAvailable();
            }
        });
    }

    private void fetchUserInfo(BlogsApi api, String userId, OnUserFullNameFetched callback) {
        api.getUserInfo(userId).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String fullName = response.body().getFullName();
                    callback.onFetched(fullName != null ? fullName : "Unknown Author");
                } else {
                    callback.onFetched("Unknown Author");
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                callback.onFetched("Unknown Author");
            }
        });
    }

    private void addArticleToRecyclerView(ArticleDetails article, String tag, String authorFullName) {
        if (blogs.size() >= MAX_BLOGS) return;

        BlogResponse blog = new BlogResponse(
                article.getTitle(),
                authorFullName,
                tag,
                article.getUrl(),
                article.getImageUrl()
        );

        blogs.add(blog);
        blogAdapter.notifyDataSetChanged();
        noBlogsImage.setVisibility(View.GONE);
    }

    private void saveBlogsToCache(List<BlogResponse> blogs, int todayDate) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_BLOGS, BlogResponse.toJsonList(blogs));
        editor.putInt(KEY_LAST_FETCH, todayDate);
        editor.apply();
    }

    private void showNoBlogsAvailable() {
        if (blogs.isEmpty()) {
            noBlogsImage.setVisibility(View.VISIBLE);
            noBlogsText.setVisibility(View.VISIBLE);
            taskCompleted();
        }
    }

    private synchronized void taskCompleted() {
        loadingTasksRemaining--;
        if (loadingTasksRemaining <= 0) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }
}
