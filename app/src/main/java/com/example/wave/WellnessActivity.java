package com.example.wave;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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

    // SharedPreferences keys for caching
    private static final String PREFS_NAME = "WellnessPrefs";
    private static final String KEY_QUOTE = "quoteText";
    private static final String KEY_AUTHOR = "quoteAuthor";
    private static final String KEY_LAST_FETCH = "lastFetchDate";

    // UI Elements
    private ImageView quoteImage;
    private TextView quoteText, quoteAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);

        // Initialize views
        quoteImage = findViewById(R.id.quoteImage);
        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);

        // Update the image of the day
        updateQuoteImage();

        // Fetch the quote of the day, with caching
        fetchTodaysQuoteWithCache();

        // Set up RecyclerViews
        setupPodcastsRecyclerView();
        setupBlogsRecyclerView();
    }

    /**
     * Updates the quote image based on the day of the year.
     */
    private void updateQuoteImage() {
        int[] images = {
                R.drawable.quote1,
                R.drawable.quote2,
                R.drawable.quote3,
                R.drawable.quote4,
                R.drawable.quote5,
                R.drawable.quote6
        };

        // Calculate the image index based on the day of the year
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % images.length;

        // Set the appropriate image
        quoteImage.setImageResource(images[index]);
    }

    /**
     * Fetches the quote of the day, using cached data if available.
     */
    private void fetchTodaysQuoteWithCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedQuote = prefs.getString(KEY_QUOTE, null);
        String cachedAuthor = prefs.getString(KEY_AUTHOR, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);

        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedQuote != null && cachedAuthor != null && lastFetchDate == todayDate) {
            // Use cached data
            quoteText.setText(cachedQuote);
            quoteAuthor.setText(cachedAuthor);
        } else {
            // Fetch new data and cache it
            fetchTodaysQuoteFromApi(prefs, todayDate);
        }
    }

    /**
     * Fetches the quote of the day from the ZenQuotes API and updates the cache.
     */
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

                    // Add double quotes to the quote text
                    String formattedQuote = "\"" + quote.getText() + "\"";

                    // Display the quote
                    quoteText.setText(formattedQuote);
                    quoteAuthor.setText(quote.getAuthor());

                    // Cache the quote
                    prefs.edit()
                            .putString(KEY_QUOTE, formattedQuote)
                            .putString(KEY_AUTHOR, quote.getAuthor())
                            .putInt(KEY_LAST_FETCH, todayDate)
                            .apply();
                } else {
                    Log.e("WellnessActivity", "Response was empty or unsuccessful.");
                    quoteText.setText("Failed to fetch quote.");
                    quoteAuthor.setText("");
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                Log.e("WellnessActivity", "Failed to fetch quote: " + t.getMessage());
                quoteText.setText("Failed to fetch quote.");
                quoteAuthor.setText("");
            }
        });
    }

    /**
     * Sets up the RecyclerView for podcasts.
     */
    private void setupPodcastsRecyclerView() {
        RecyclerView podcastRecyclerView = findViewById(R.id.podcastRecyclerView);
        podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Podcast> podcasts = new ArrayList<>();
        podcasts.add(new Podcast("Stress day relaxation", "15 min"));
        podcasts.add(new Podcast("Evening meditation to relax", "12 min"));

        PodcastAdapter podcastAdapter = new PodcastAdapter(podcasts);
        podcastRecyclerView.setAdapter(podcastAdapter);
    }

    /**
     * Sets up the RecyclerView for blogs.
     */
    private void setupBlogsRecyclerView() {
        RecyclerView blogRecyclerView = findViewById(R.id.blogRecyclerView);
        blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Blog> blogs = new ArrayList<>();
        blogs.add(new Blog("Feeling Overwhelmed? Try the RAIN Meditation", "By Tara Brach"));
        blogs.add(new Blog("Struggling with burnout? 10 recovery tips", "By Dr. Chris"));

        BlogAdapter blogAdapter = new BlogAdapter(blogs);
        blogRecyclerView.setAdapter(blogAdapter);
    }
}
