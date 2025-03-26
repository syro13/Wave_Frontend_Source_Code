package com.example.wave;

import static androidx.test.InstrumentationRegistry.getContext;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.cardview.widget.CardView;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

public class WellnessActivity extends BaseActivity{
    private NetworkReceiver networkReceiver;
    private static final String PREFS_NAME = "WellnessPrefs";
    private static final String KEY_QUOTE = "quoteText";
    private static final String KEY_AUTHOR = "quoteAuthor";
    private static final String KEY_LAST_FETCH = "lastFetchDate";
    private static final String KEY_PLAN_TEXT = "planText";
    private static final String KEY_PLAN_DATE = "planDate";
    private static final String PREFS_BLOGS = "PrefsBlogs";
    private static final String PREFS_PODCASTS = "PrefsPodcasts";
    private static final int MAX_BLOGS = 4;
    private static final int MAX_PODCASTS = 4;
    private static final String TAG = "API_RESPONSE";

    private ImageView quoteImage;
    private TextView quoteText, quoteAuthor, noPodcastsText, noBlogsText;
    private RecyclerView blogRecyclerView, podcastRecyclerView, promptsRecyclerView;
    private BlogAdapter blogAdapter;
    private PodcastAdapter podcastAdapter;
    private ImageView noBlogsImage, noPodcastsImage;
    private ProgressBar loadingIndicator;
    private View loadingOverlay;
    private Button generatePlanButton;
    private CardView planCard;
    private TextView planContent;

    private List<Blogs_Response> blogs = new ArrayList<>();
    private List<PodcastsResponse> podcasts = new ArrayList<>();
    private int loadingTasksRemaining = 0;
    private final WellnessAPI apiService = RetrofitClient.getClient().create(WellnessAPI.class);
    private AIPromptsAdapter promptsAdapter;
    private List<String> displayPromptsList;
    private List<String> actualPromptsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness);

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

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
        generatePlanButton = findViewById(R.id.generatePlanButton);
        planCard           = findViewById(R.id.planCard);
        planContent        = findViewById(R.id.planContent);
        planCard.setVisibility(View.GONE);

        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupPrompts();


        showLoading();

        // Start loading tasks
        updateQuoteImage();

        loadData();
      
        generatePlanButton.setOnClickListener(v -> {
            PlanOptionsBottomSheet sheet = new PlanOptionsBottomSheet();
            sheet.setListener((mood, time, thoughts) -> generateDailyPlan(mood, time, thoughts));
            sheet.show(getSupportFragmentManager(), "PlanOptions");
        });
    }
    @Override
    public void onNetworkConnected() {
        Log.d("WellnessActivity", "Internet restored! Reloading data...");
        runOnUiThread(() -> {
            showLoading();
            loadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (lastFetchDate != todayDate) {
            Log.d(TAG, "Cache expired, fetching new data...");
            loadData();  // Only load new data if cache is outdated
        } else {
            Log.d(TAG, "Using cached data...");
            loadCachedBlogs();
            loadCachedPodcasts();
            fetchTodaysQuoteWithCache();
            // If cache is empty, fetch new data
            if (blogs.isEmpty()) {
                Log.d(TAG, "Cached blogs empty, fetching new ones...");
                fetchBlogsFromApi();
            }
            if (podcasts.isEmpty()) {
                Log.d(TAG, "Cached podcasts empty, fetching new ones...");
                fetchPodcastsFromApi();
            }
            loadCachedPlan();

        }
    }
    private void loadCachedPlan() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedDate = prefs.getInt(KEY_PLAN_DATE, -1);
        String cachedPlan = prefs.getString(KEY_PLAN_TEXT, null);
        if (savedDate == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) && cachedPlan != null) {
            planContent.setText(cachedPlan);
            planCard.setVisibility(View.VISIBLE);
        }
    }

    private void generateDailyPlan(String mood, String time, String thoughts) {
        showLoading();

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You're a helpful and creative wellbeing coach. ");
        promptBuilder.append("Create a unique, personalized wellness plan for a student feeling '")
                .append(mood)
                .append("' who has ")
                .append(time)
                .append(" available today.\n\n");

        if (!thoughts.isEmpty()) {
            promptBuilder.append("The student shared the following thoughts to guide you: \"")
                    .append(thoughts)
                    .append("\".\n\n");
        }

        promptBuilder.append("Avoid generic suggestions. Offer specific, refreshing and practical ideas. ");
        promptBuilder.append("Include a mix of mental, physical, and emotional wellbeing tips. ");
        promptBuilder.append("Make the plan realistic and engaging. Use an encouraging tone. ");
        promptBuilder.append("Make it different each time — think creatively.\n");

        AIPromptRequest request = new AIPromptRequest(promptBuilder.toString());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://updatedservice-621971573276.us-central1.run.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AIService service = retrofit.create(AIService.class);
        service.getAIResponse(request).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    String plan = response.body().getResponse();
                    planContent.setText(plan);
                    planCard.setVisibility(View.VISIBLE);
                    savePlanToCache(plan);
                } else {
                    Toast.makeText(WellnessActivity.this, "Failed to generate plan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                hideLoading();
                Toast.makeText(WellnessActivity.this, "Error generating plan", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void savePlanToCache(String plan) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_PLAN_TEXT, plan)
                .putInt(KEY_PLAN_DATE, Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
                .apply();
    }

    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
        loadingOverlay.setVisibility(View.GONE);
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        boolean useCache = (lastFetchDate == todayDate);

        // Reset the task counter
        loadingTasksRemaining = 0;

        // Load cached or fetch new
        if (useCache) {
            loadingTasksRemaining += 3;
            loadCachedBlogs();
            loadCachedPodcasts();
            fetchTodaysQuoteWithCache();
        } else {
            loadingTasksRemaining += 3;
            fetchBlogsFromApi();
            fetchPodcastsFromApi();
            fetchTodaysQuoteFromApi(prefs, todayDate);
        }
    }

    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getCurrentMenuItemId() {
        return -1; // No selection
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
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedQuote != null && cachedAuthor != null) {
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
                    hideNoPodcastsAvailable();
                } else {
                    // Show placeholders if the response is empty or invalid
                    showNoPodcastsAvailable();
                }
                taskCompleted();
            }

            @Override
            public void onFailure(Call<List<PodcastsResponse>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                showNoPodcastsAvailable();
                taskCompleted();
            }
        });
    }
    private void hideNoPodcastsAvailable() {
        noPodcastsImage.setVisibility(View.GONE);
        noPodcastsText.setVisibility(View.GONE);
    }
    private void hideNoBlogsAvailable() {
        noBlogsImage.setVisibility(View.GONE);
        noBlogsText.setVisibility(View.GONE);
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
                    preloadBlogImages(blogs);
                    setupBlogsRecyclerView(blogs);
                    hideNoBlogsAvailable();
                } else {
                    Log.e(TAG, "Failed to get blogs");
                    showNoBlogsAvailable();
                }
                taskCompleted();
            }

            @Override
            public void onFailure(Call<List<Blogs_Response>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                showNoBlogsAvailable();
                taskCompleted();
            }
        });
    }


    private void showNoPodcastsAvailable() {
        if (podcastAdapter == null || podcastAdapter.getItemCount() == 0) {
            noPodcastsImage.setVisibility(View.VISIBLE);
            noPodcastsText.setVisibility(View.VISIBLE);
        }
    }
    private void showNoBlogsAvailable() {
        if (blogAdapter == null || blogAdapter.getItemCount() == 0) {
            noBlogsImage.setVisibility(View.VISIBLE);
            noBlogsText.setVisibility(View.VISIBLE);
        }
    }

    private void setupPodcastsRecyclerView(List<PodcastsResponse> podcastsList) {
        if (podcastAdapter == null) {
            podcastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            podcastAdapter = new PodcastAdapter(this, podcastsList);
            podcastRecyclerView.setAdapter(podcastAdapter);
        } else {
            podcastAdapter.updateData(podcastsList);
        }
    }
    private void setupBlogsRecyclerView(List<Blogs_Response> blogsList) {
        if (blogAdapter == null) {
            blogRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            blogAdapter = new BlogAdapter(this, blogsList);
            blogRecyclerView.setAdapter(blogAdapter);
        } else {
            blogAdapter.updateData(blogsList);
        }
    }

    private void loadCachedPodcasts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedPodcastsJson = prefs.getString(PREFS_PODCASTS, null);

        if (cachedPodcastsJson != null) {
            List<PodcastsResponse> cachedPodcasts = PodcastsResponse.fromJsonList(cachedPodcastsJson);

            if (cachedPodcasts.size() > MAX_PODCASTS) {
                cachedPodcasts = cachedPodcasts.subList(0, MAX_PODCASTS);
            }

            Log.d(TAG, "Loaded Podcasts from Cache: " + cachedPodcasts);

            if (!cachedPodcasts.isEmpty()) {
                if (podcastAdapter == null) {
                    setupPodcastsRecyclerView(cachedPodcasts);
                } else {
                    podcastAdapter.updateData(cachedPodcasts);
                }
                hideNoPodcastsAvailable();
            } else {
                showNoPodcastsAvailable();
            }
        } else {
            showNoPodcastsAvailable();
        }
        taskCompleted();
    }




    private void loadCachedBlogs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);


        if (cachedBlogsJson != null) {
            List<Blogs_Response> cachedBlogs = Blogs_Response.fromJsonList(cachedBlogsJson);

            if (cachedBlogs.size() > MAX_BLOGS) {
                cachedBlogs = cachedBlogs.subList(0, MAX_BLOGS);
            }

            Log.d(TAG, "Loaded Blogs from Cache: " + cachedBlogs);
            if (!cachedBlogs.isEmpty()) {
                if (blogAdapter == null) {
                    preloadBlogImages(cachedBlogs);
                    setupBlogsRecyclerView(cachedBlogs);
                } else {
                    blogAdapter.updateData(cachedBlogs);
                }
                hideNoBlogsAvailable();
            } else {
                showNoBlogsAvailable();
            }
        } else {
            showNoBlogsAvailable();
        }
        taskCompleted();
    }
    private void preloadBlogImages(List<Blogs_Response> blogList) {
        for (Blogs_Response blog : blogList) {
            Glide.with(this)
                    .load(blog.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
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
    private void saveBlogsToCache(List<Blogs_Response> blogs) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String blogsJson = Blogs_Response.toJsonList(blogs);
        editor.putString(PREFS_BLOGS, blogsJson);
        editor.putInt(KEY_LAST_FETCH, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        editor.apply();
        Log.d(TAG, "Saved Blogs to Cache: " + blogsJson);
    }

    private synchronized void taskCompleted() {
        loadingTasksRemaining--;
        if (loadingTasksRemaining <= 0) {
            loadingIndicator.setVisibility(View.GONE);
            loadingOverlay.setVisibility(View.GONE);

            if (blogs.isEmpty()) showNoBlogsAvailable(); else hideNoBlogsAvailable();
            if (podcasts.isEmpty()) showNoPodcastsAvailable(); else hideNoPodcastsAvailable();
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
