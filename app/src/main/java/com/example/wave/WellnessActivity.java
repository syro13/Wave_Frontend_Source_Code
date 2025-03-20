package com.example.wave;

import static androidx.test.InstrumentationRegistry.getContext;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WellnessActivity extends BaseActivity implements NetworkReceiver.NetworkChangeListener {
    private NetworkReceiver networkReceiver;
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
    private RecyclerView blogRecyclerView, podcastRecyclerView, promptsRecyclerView;
    private BlogAdapter blogAdapter;
    private PodcastAdapter podcastAdapter;
    private ImageView noBlogsImage, noPodcastsImage;
    private ProgressBar loadingIndicator;
    private View loadingOverlay;

    private List<Blogs_Response> blogs = new ArrayList<>();
    private List<PodcastsResponse> podcasts = new ArrayList<>();
    private int loadingTasksRemaining = 0;
    private static final String TAG = "API_RESPONSE";
    private final WellnessAPI apiService = RetrofitClient.getClient().create(WellnessAPI.class);
    private AIPromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);




        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

// Clear SharedPreferences for testing
 //       getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
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
        loadingOverlay = findViewById(R.id.loadingOverlay);
        promptsRecyclerView = findViewById(R.id.promptsRecyclerView);

        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupPrompts();


        // Start with progress bar visible
        loadingIndicator.setVisibility(View.VISIBLE);

        // Increment the loading task counter for each task
        loadingTasksRemaining = 3;

        // Start loading tasks
        updateQuoteImage();
        fetchTodaysQuoteWithCache();
        loadCachedBlogs();
        loadCachedPodcasts();

        // Register the network receiver
        networkReceiver = new NetworkReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
        findViewById(R.id.profileIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Budget Activity
                Intent intent = new Intent(WellnessActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
    }


    @Override
    public void onNetworkRestored() {
        Log.d("WellnessActivity", "Network restored. Checking data...");
        reloadDataIfNeeded();  // Reload only if data is not already cached
    }

    private void reloadDataIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        boolean needsReload = false;

        loadingIndicator.setVisibility(View.VISIBLE);

        // Check if quote data needs to be reloaded
        String cachedQuote = prefs.getString(KEY_QUOTE, null);
        String cachedAuthor = prefs.getString(KEY_AUTHOR, null);
        if (cachedQuote == null || cachedAuthor == null || lastFetchDate != todayDate) {
            Log.d("WellnessActivity", "Reloading Quotes...");
            fetchTodaysQuoteWithCache();
            needsReload = true;
        } else {
            Log.d("WellnessActivity", "Quotes are already cached.");
        }

        // Check if blog data needs to be reloaded
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        if (cachedBlogsJson == null || lastFetchDate != todayDate) {
            Log.d("WellnessActivity", "Reloading Blogs...");
            loadCachedBlogs();
            needsReload = true;
        } else {
            Log.d("WellnessActivity", "Blogs are already cached.");
        }

        // Check if podcast data needs to be reloaded
        String cachedPodcastsJson = prefs.getString(PREFS_PODCASTS, null);
        if (cachedPodcastsJson == null || lastFetchDate != todayDate) {
            Log.d("WellnessActivity", "Reloading Podcasts...");
            loadCachedPodcasts();
            needsReload = true;
        } else {
            Log.d("WellnessActivity", "Podcasts are already cached.");
        }

        // Hide placeholders if reload is required
        if (needsReload) {
            Toast.makeText(this, "Internet restored. Reloading data...", Toast.LENGTH_SHORT).show();
            noBlogsImage.setVisibility(View.GONE);
            noPodcastsImage.setVisibility(View.GONE);
            noBlogsText.setVisibility(View.GONE);
            noPodcastsText.setVisibility(View.GONE);
        } else {
            Log.d("WellnessActivity", "No reload required. Data is up-to-date.");
            loadingIndicator.setVisibility(View.GONE); // Hide the loading indicator
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
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
    private void fetchPodcastsFromApi() {
        Call<List<PodcastsResponse>> podcastsCall = apiService.getPodcastsData();
        podcastsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<PodcastsResponse>> call, Response<List<PodcastsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    podcasts = new ArrayList<>(response.body());
                    if (podcasts.size() > 4) {
                        podcasts = podcasts.subList(0, 4);
                    }
                    Log.d(TAG, "Podcast: " + podcasts);
                    savePodcastsToCache(podcasts);
                    setupPodcastsRecyclerView(podcasts);
                    // Hide placeholders when data is fetched
                    noPodcastsImage.setVisibility(View.GONE);
                    noPodcastsText.setVisibility(View.GONE);
                } else {
                    // Show placeholders if the response is empty or invalid
                    showNoPodcastsAvailable();
                }
            }

            @Override
            public void onFailure(Call<List<PodcastsResponse>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }
    private void setupPodcastsRecyclerView(List<PodcastsResponse> podcastsList) {
        if (podcastAdapter == null) {
            podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            podcastAdapter = new PodcastAdapter(this, podcastsList);
            podcastRecyclerView.setAdapter(podcastAdapter);
        } else {
            podcastAdapter.notifyDataSetChanged();
        }
    }

    private void fetchBlogsFromApi(){
        Call<List<Blogs_Response>> blogsCall = apiService.getBlogsData();
        blogsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Blogs_Response>> call, Response<List<Blogs_Response>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    blogs = new ArrayList<>(response.body());
                    if (blogs.size() > 4) {
                        blogs = blogs.subList(0, 4);
                    }
                    Log.d(TAG, "Blogs: " + blogs);
                    saveBlogsToCache(blogs);
                    setupBlogsRecyclerView(blogs);
                } else {
                    Log.e(TAG, "Failed to get blogs");
                    showNoBlogsAvailable();
                }
            }

            @Override
            public void onFailure(Call<List<Blogs_Response>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }
    private void loadCachedPodcasts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedPodcastsJson = prefs.getString(PREFS_PODCASTS, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedPodcastsJson != null && lastFetchDate == todayDate) {
            List<PodcastsResponse> cachedPodcasts = PodcastsResponse.fromJsonList(cachedPodcastsJson);

            if (cachedPodcasts.size() > MAX_PODCASTS) {
                cachedPodcasts = cachedPodcasts.subList(0, MAX_PODCASTS);
            }

            Log.d(TAG, "Loaded Podcasts from Cache: " + cachedPodcasts);

            podcasts.clear();
            podcasts.addAll(cachedPodcasts);

            setupPodcastsRecyclerView(podcasts);
        } else {
            fetchPodcastsFromApi();
        }
    }


    private void savePodcastsToCache(List<PodcastsResponse> podcasts) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String podcastsJson = PodcastsResponse.toJsonList(podcasts);
        editor.putString(PREFS_PODCASTS, podcastsJson);
        editor.putInt(KEY_LAST_FETCH, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        editor.apply();
        Log.d(TAG, "Saved Podcasts to Cache: " + podcastsJson);
    }


    private void showNoPodcastsAvailable() {
        if (podcastAdapter == null || podcastAdapter.getItemCount() == 0) {
            noPodcastsImage.setVisibility(View.VISIBLE);
            noPodcastsText.setVisibility(View.VISIBLE);
        }
    }

    private void setupBlogsRecyclerView(List<Blogs_Response> blogsList) {
        if (blogAdapter == null) {
            blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            blogAdapter = new BlogAdapter(this, blogsList);
            blogRecyclerView.setAdapter(blogAdapter);
        } else {
            blogAdapter.notifyDataSetChanged();
        }
    }


    private void loadCachedBlogs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedBlogsJson != null && lastFetchDate == todayDate) {
            List<Blogs_Response> cachedBlogs = Blogs_Response.fromJsonList(cachedBlogsJson);

            if (cachedBlogs.size() > MAX_BLOGS) {
                cachedBlogs = cachedBlogs.subList(0, MAX_BLOGS);
            }

            Log.d(TAG, "Loaded Blogs from Cache: " + cachedBlogs);
            blogs.clear();
            blogs.addAll(cachedBlogs);

            setupBlogsRecyclerView(blogs);
        } else {
            fetchBlogsFromApi();
        }
    }


    private void saveBlogsToCache(List<Blogs_Response> blogs) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String blogsJson = Blogs_Response.toJsonList(blogs);
        editor.putString(PREFS_BLOGS, blogsJson);
        editor.putInt(KEY_LAST_FETCH, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        editor.apply();
        Log.d(TAG, "Saved Blogs to Cache: " + blogsJson);
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
    private void setupPrompts() {
        displayPromptsList = Arrays.asList(
                "How can I manage stress effectively?",
                "Give me tips for staying motivated in my studies",
                "Suggest a quick mindfulness exercise",
                "How do I improve my sleep habits?",
                "Recommend a healthy daily routine for students"
        );

        actualPromptsList = Arrays.asList(
                "Provide simple and effective stress management techniques that students can use daily to stay mentally healthy.",
                "Give motivation strategies and study techniques that can help students stay consistent and avoid burnout.",
                "Suggest a short mindfulness or breathing exercise that students can do to relax and refocus their minds.",
                "Explain practical steps to improve sleep quality, especially for students struggling with irregular sleep patterns.",
                "Describe an ideal daily routine for students that balances academics, self-care, and physical well-being."
        );

        promptsAdapter = new AIPromptsAdapter(displayPromptsList, actualPromptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);
    }

    private void showPopup(String displayPrompt, String actualPrompt) {
        // Disable prompt clicking to prevent multiple selections
        promptsRecyclerView.setEnabled(false);
        loadingOverlay.setVisibility(View.VISIBLE);
        // Show ProgressBar when fetching AI response
        loadingIndicator.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AIService aiService = retrofit.create(AIService.class);
        AIPromptRequest request = new AIPromptRequest(actualPrompt);

        aiService.getAIResponse(request).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {

                // Hide ProgressBar once response is received
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE); // Re-enable interactions
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponse = response.body().getResponse();
                    AIContentDialog dialog = AIContentDialog.newInstance(displayPrompt, aiResponse);
                    // Show dialog and re-enable clicks only after it's dismissed
                    dialog.show(getSupportFragmentManager(), "AIContentDialog");
                    getSupportFragmentManager().executePendingTransactions();

                    dialog.getDialog().setOnDismissListener(dialogInterface -> {
                        promptsRecyclerView.setEnabled(true); // Re-enable clicks after dialog closes
                    });
                } else {
                    // Hide ProgressBar in case of failure
                    loadingIndicator.setVisibility(View.GONE);
                    loadingOverlay.setVisibility(View.GONE);
                    promptsRecyclerView.setEnabled(true);
                    Toast.makeText(WellnessActivity.this, "Failed to get AI response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                // Hide ProgressBar in case of failure
                loadingIndicator.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE);
                promptsRecyclerView.setEnabled(true);
                Toast.makeText(WellnessActivity.this, "Error connecting to AI server", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
