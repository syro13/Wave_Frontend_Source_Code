package com.example.wave;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
                    quoteText.setText("Failed to fetch quote.");
                    quoteAuthor.setText("");
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                quoteText.setText("Failed to fetch quote.");
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
        } else {
            fetchBlogsFromApi(prefs, todayDate);
        }
    }

    private void fetchBlogsFromApi(SharedPreferences prefs, int todayDate) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://medium2.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BlogsApi api = retrofit.create(BlogsApi.class);

        blogs.clear();
        blogAdapter.notifyDataSetChanged();

        String[] tags = {"self-improvement", "meditation", "wellness"};
        for (String tag : tags) {
            if (blogs.size() >= MAX_BLOGS) break;

            api.getRecommendedFeed(tag, 1).enqueue(new Callback<RecommendedFeedResponse>() {
                @Override
                public void onResponse(Call<RecommendedFeedResponse> call, Response<RecommendedFeedResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        fetchArticleDetails(api, Collections.singletonList(response.body().getRecommendedFeed().get(0)), tag, prefs, todayDate);
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

    private void fetchArticleDetails(BlogsApi api, List<String> articleIds, String tag, SharedPreferences prefs, int todayDate) {
        for (String articleId : articleIds) {
            if (blogs.size() >= MAX_BLOGS) return;

            api.getArticleInfo(articleId).enqueue(new Callback<ArticleDetails>() {
                @Override
                public void onResponse(Call<ArticleDetails> call, Response<ArticleDetails> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ArticleDetails article = response.body();
                        addArticleToRecyclerView(article, tag);

                        if (blogs.size() == MAX_BLOGS) {
                            saveBlogsToCache(blogs, todayDate);
                        }
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
    }

    private void addArticleToRecyclerView(ArticleDetails article, String tag) {
        if (blogs.size() >= MAX_BLOGS) return;

        BlogResponse blog = new BlogResponse(
                article.getTitle(),
                article.getAuthor(),
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
        }
    }
}
