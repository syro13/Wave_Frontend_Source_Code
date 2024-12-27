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
import java.util.Collections;
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
    private static final String KEY_LAST_FETCH = "lastFetchDate";
    private static final int MAX_BLOGS = 3;

    private ImageView quoteImage;
    private TextView quoteText, quoteAuthor;
    private RecyclerView blogRecyclerView;
    private BlogAdapter blogAdapter;
    private ImageView noBlogsImage;
    private ProgressBar loadingIndicator; // Progress bar for loading
    private final List<BlogResponse> blogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);

        // Initialize views
        quoteImage = findViewById(R.id.quoteImage);
        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);
        blogRecyclerView = findViewById(R.id.blogRecyclerView);
        noBlogsImage = findViewById(R.id.noBlogsImage);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Update the image of the day
        updateQuoteImage();

        // Fetch the quote of the day, with caching
        fetchTodaysQuoteWithCache();

        // Set up RecyclerViews
        setupPodcastsRecyclerView();
        setupBlogsRecyclerView();

        loadCachedBlogs(); // Load cached blogs or fetch new ones
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
                    quoteText.setText("Unable to load today's quote. Please check your connection.");
                    quoteAuthor.setText("");
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                quoteText.setText("Unable to load today's quote. Please check your connection.");
                quoteAuthor.setText("");
            }
        });
    }

    private void setupPodcastsRecyclerView() {
        RecyclerView podcastRecyclerView = findViewById(R.id.podcastRecyclerView);
        podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Podcast> podcasts = new ArrayList<>();
        podcasts.add(new Podcast("Stress day relaxation", "15 min"));
        podcasts.add(new Podcast("Evening meditation to relax", "12 min"));

        PodcastAdapter podcastAdapter = new PodcastAdapter(podcasts);
        podcastRecyclerView.setAdapter(podcastAdapter);
    }

    private void setupBlogsRecyclerView() {
        blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        blogAdapter = new BlogAdapter(this, blogs);
        blogRecyclerView.setAdapter(blogAdapter);
    }

    private void loadCachedBlogs() {
        // Show the loading indicator before processing
        loadingIndicator.setVisibility(View.VISIBLE);
        blogRecyclerView.setVisibility(View.GONE);
        noBlogsImage.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedBlogsJson != null && lastFetchDate == todayDate) {
            blogs.clear();
            blogs.addAll(BlogResponse.fromJsonList(cachedBlogsJson));

            // Limit to MAX_BLOGS
            if (blogs.size() > MAX_BLOGS) {
                blogs.subList(MAX_BLOGS, blogs.size()).clear();
            }

            blogAdapter.notifyDataSetChanged();
            noBlogsImage.setVisibility(View.GONE);
            blogRecyclerView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE); // Hide loading after caching
        } else {
            fetchBlogsFromApi(prefs, todayDate);
        }

    }

    private void fetchBlogsFromApi(SharedPreferences prefs, int todayDate) {
        BlogsApi api = RetrofitClient.getRetrofitInstance(this).create(BlogsApi.class);

        blogs.clear(); // Clear previous blog data
        blogAdapter.notifyDataSetChanged();

        String[] tags = {"self-improvement", "meditation", "wellness"};

        // Iterate over each tag
        for (String tag : tags) {
            if (blogs.size() >= MAX_BLOGS) break; // Stop if MAX_BLOGS is already reached

            // Fetch the recommended feed for each tag
            api.getRecommendedFeed(tag, 1).enqueue(new Callback<RecommendedFeedResponse>() {
                @Override
                public void onResponse(Call<RecommendedFeedResponse> call, Response<RecommendedFeedResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> recommendedFeed = response.body().getRecommendedFeed();

                        if (!recommendedFeed.isEmpty()) {
                            // Use only the first article from the recommended feed
                            String firstArticleId = recommendedFeed.get(0);

                            // Fetch the article details using the first article ID
                            fetchArticleDetails(api, firstArticleId, tag, prefs, todayDate);
                        } else {
                            Log.e("WellnessActivity", "No articles found for tag: " + tag);
                            showNoBlogsAvailable();
                        }
                    } else {
                        Log.e("WellnessActivity", "Failed to fetch recommended feed for tag " + tag + ": " + response.message());
                        showNoBlogsAvailable();
                    }
                }

                @Override
                public void onFailure(Call<RecommendedFeedResponse> call, Throwable t) {
                    Log.e("WellnessActivity", "Error fetching recommended feed for tag " + tag, t);
                    showNoBlogsAvailable();
                }
            });
        }
    }

    // Fetch the details of the article using its ID
    private void fetchArticleDetails(BlogsApi api, String articleId, String tag, SharedPreferences prefs, int todayDate) {
        if (blogs.size() >= MAX_BLOGS) return;

        api.getArticleInfo(articleId).enqueue(new Callback<ArticleDetails>() {
            @Override
            public void onResponse(Call<ArticleDetails> call, Response<ArticleDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArticleDetails article = response.body();

                    // Fetch user info using the user_id from the article details
                    fetchUserInfo(api, article.getAuthor(), userFullName -> {
                        addArticleToRecyclerView(article, tag, userFullName);

                        if (blogs.size() == MAX_BLOGS) {
                            saveBlogsToCache(blogs, todayDate);
                            loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                            blogRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                        }
                    });
                } else {
                    Log.e("WellnessActivity", "Failed to fetch article details: " + response.message());
                    showNoBlogsAvailable();
                }
            }

            @Override
            public void onFailure(Call<ArticleDetails> call, Throwable t) {
                Log.e("WellnessActivity", "Error fetching article details", t);
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
                    Log.e("WellnessActivity", "Failed to fetch user info: " + response.message());
                    callback.onFetched("Unknown Author");
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("WellnessActivity", "Error fetching user info", t);
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
            loadingIndicator.setVisibility(View.GONE);
            noBlogsImage.setVisibility(View.VISIBLE);
        }
    }
}
