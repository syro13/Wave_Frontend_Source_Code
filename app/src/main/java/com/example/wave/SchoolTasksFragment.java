package com.example.wave;


import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SchoolTasksFragment extends Fragment implements NetworkReceiver.NetworkChangeListener{
    private NetworkReceiver networkReceiver;
    private static final String PREFS_BLOGS = "PrefsBlogs";
    private static final String KEY_LAST_FETCH = "lastFetchDate";
    private static final int MAX_BLOGS = 4;
    private static final String PREFS_NAME = "SchoolTasksPrefs";
    private RecyclerView articleRecyclerView, promptsRecyclerView;
    private TextView noBlogsText;
    private SchoolTasksBlogAdapter blogAdapter;
    private ImageView noBlogsImage;
    private ProgressBar loadingIndicator;

    private PromptsAdapter promptsAdapter;
    private List<String> promptsList;

    private final List<BlogResponse> blogs = new ArrayList<>();
    private int loadingTasksRemaining = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);

        // RecyclerView setup for articles
        articleRecyclerView = view.findViewById(R.id.articlesRecyclerView);
        noBlogsImage = view.findViewById(R.id.noBlogsImage);
        noBlogsText = view.findViewById(R.id.noBlogsText);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        setupBlogsRecyclerView();

        // Fetch fresh blogs first; fallback to cached blogs if needed
        fetchBlogsWithFallback();

        networkReceiver = new NetworkReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkReceiver, filter);

        // RecyclerView setup for AI prompts
        promptsRecyclerView = view.findViewById(R.id.promptsRecyclerView);
        promptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        promptsList = Arrays.asList(
                "Suggest a quick study tip",
                "How can I stay organized in my studies?",
                "Give me advice on balancing chores with studying",
                "How to make studying less stressful?",
                "Share a tip for keeping my work clean and tidy"
        );
        promptsAdapter = new PromptsAdapter(promptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);

        // Set initial active state for buttons
        setActiveButton(schoolTasksButton, homeTasksButton);

        // School Tasks Button Click
        schoolTasksButton.setOnClickListener(v -> {
            setActiveButton(schoolTasksButton, homeTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showSchoolTasksFragment();
            }
        });

// Home Tasks Button Click
        homeTasksButton.setOnClickListener(v -> {
            setActiveButton(homeTasksButton, schoolTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showHomeTasksFragment();
            }
        });
        ImageView profileIcon = view.findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onNetworkRestored() {
        Log.d("SchoolTasksActivity", "Network restored. Checking data...");
        reloadDataIfNeeded();  // Reload only if data is not already cached
    }



    private void reloadDataIfNeeded() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        boolean needsReload = false;

        // Show the loading indicator while reloading data
        loadingIndicator.setVisibility(View.VISIBLE);

        // Check if blog data needs to be reloaded
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        if (cachedBlogsJson == null || lastFetchDate != todayDate) {
            Log.d("SchoolTasksFragment", "Reloading Blogs...");
            loadCachedBlogs();
            needsReload = true;
        } else {
            Log.d("SchoolTasksFragment", "Blogs are already cached.");
        }

        // Hide placeholders if reload is required
        if (needsReload) {
            Toast.makeText(requireContext(), "Internet restored. Reloading data...", Toast.LENGTH_SHORT).show();
            noBlogsImage.setVisibility(View.GONE);
            noBlogsText.setVisibility(View.GONE);
        } else {
            Log.d("SchoolTasksFragment", "No reload required. Data is up-to-date.");
            loadingIndicator.setVisibility(View.GONE); // Hide the loading indicator
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(networkReceiver);
    }

    private void loadCachedBlogs() {
        if (!isAdded()) return; // Ensure Fragment is attached

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedBlogsJson != null && lastFetchDate == todayDate) {
            blogs.clear();
            blogs.addAll(BlogResponse.fromJsonList(cachedBlogsJson));
            if (blogs.size() > MAX_BLOGS) {
                blogs.subList(MAX_BLOGS, blogs.size()).clear(); // Limit the list to MAX_BLOGS
            }
            blogAdapter.notifyDataSetChanged();
            noBlogsImage.setVisibility(View.GONE);
            noBlogsText.setVisibility(View.GONE);
        } else {
            blogs.clear();
            prefs.edit().remove(PREFS_BLOGS).apply();
            fetchBlogsFromApi(prefs, todayDate, success -> {
                if (!success && isAdded()) { // Ensure Fragment is attached
                    Log.d("Blog Fetch", "Falling back to cached blogs.");
                    loadCachedBlogs();
                }
            });
        }
    }


    private void fetchBlogsWithFallback() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        // Fetch blogs from the API
        fetchBlogsFromApi(prefs, todayDate, success -> {
            if (!success) {
                // Fallback to cached blogs if API fails
                Log.d("Blog Fetch", "Falling back to cached blogs.");
                loadCachedBlogs();
            }
        });
    }

    interface OnFetchCompleteListener {
        void onFetchComplete(boolean success);
    }


    private void fetchBlogsFromApi(SharedPreferences prefs, int todayDate, OnFetchCompleteListener listener) {
        if (!isAdded()) return; // Ensure Fragment is attached

        BlogsApi api = RetrofitClient.getRetrofitInstance(
                requireContext(),
                "https://medium2.p.rapidapi.com/",
                "x-rapidapi-key",
                getResources().getString(Integer.parseInt("123")) // Make sure to remove
        ).create(BlogsApi.class);

        String[] tags = {"education", "study tips", "studying for exams"}; // Example tags
        blogs.clear(); // Clear old data to avoid duplicates
        prefs.edit().remove(PREFS_BLOGS).apply(); // Clear cache

        for (String tag : tags) {
            if (blogs.size() >= MAX_BLOGS) break;

            loadingTasksRemaining++; // Increment for each tag

            api.getRecommendedFeed(tag, 1).enqueue(new Callback<RecommendedFeedResponse>() {
                @Override
                public void onResponse(Call<RecommendedFeedResponse> call, Response<RecommendedFeedResponse> response) {
                    if (!isAdded()) return; // Ensure Fragment is attached

                    if (response.isSuccessful() && response.body() != null) {
                        List<String> recommendedFeed = response.body().getRecommendedFeed();
                        for (String articleId : recommendedFeed) {
                            if (blogs.size() >= MAX_BLOGS) break;
                            fetchArticleDetails(api, articleId, prefs, todayDate);
                        }
                    }
                    taskCompleted();
                    listener.onFetchComplete(true);
                }

                @Override
                public void onFailure(Call<RecommendedFeedResponse> call, Throwable t) {
                    if (!isAdded()) return; // Ensure Fragment is attached

                    Log.e("API Error", "Failed to fetch blogs", t);
                    taskCompleted();
                    listener.onFetchComplete(false);
                }
            });
        }
    }

    private void fetchArticleDetails(BlogsApi api, String articleId, SharedPreferences prefs, int todayDate) {
        if (blogs.size() >= MAX_BLOGS) {
            taskCompleted(); // Ensure task is marked complete if we exceed MAX_BLOGS
            return;
        }

        api.getArticleInfo(articleId).enqueue(new Callback<ArticleDetails>() {
            @Override
            public void onResponse(Call<ArticleDetails> call, Response<ArticleDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArticleDetails article = response.body();

                    synchronized (blogs) { // Synchronize access to blogs
                        if (blogs.size() < MAX_BLOGS && !isDuplicateArticle(article)) {
                            BlogResponse blog = new BlogResponse(
                                    article.getTitle(),
                                    article.getAuthor(),
                                    "school-tasks",
                                    article.getUrl(),
                                    article.getImageUrl()
                            );
                            blogs.add(blog);
                            blogAdapter.notifyDataSetChanged();
                            saveBlogsToCache(prefs, blogs, todayDate);
                        }
                    }
                }
                taskCompleted(); // Mark task as complete after fetch
            }

            @Override
            public void onFailure(Call<ArticleDetails> call, Throwable t) {
                Log.e("Article Fetch", "Failed to fetch article details", t);
                taskCompleted(); // Mark task as complete even on failure
            }
        });
    }

    private boolean isDuplicateArticle(ArticleDetails article) {
        for (BlogResponse existingBlog : blogs) {
            if (existingBlog.getTitle().equals(article.getTitle()) &&
                    existingBlog.getAuthor().equals(article.getAuthor())) {
                return true; // Found duplicate
            }
        }
        return false;
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
                taskCompleted();
            }
            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                callback.onFetched("Unknown Author");
                taskCompleted();
            }
        });
    }


    private void setupBlogsRecyclerView() {
        articleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        blogAdapter = new SchoolTasksBlogAdapter(requireContext(), blogs);
        articleRecyclerView.setAdapter(blogAdapter);

        // Limit the adapter size to MAX_BLOGS
        blogAdapter.notifyDataSetChanged();
    }

    private void addArticleToRecyclerView(ArticleDetails article, String tag, String authorFullName) {
        if (blogs.size() >= MAX_BLOGS) return;
        // Check if the blog already exists in the list
        for (BlogResponse existingBlog : blogs) {
            if (existingBlog.getTitle().equals(article.getTitle()) &&
                    existingBlog.getAuthor().equals(authorFullName)) {
                return; // Don't add duplicates
            }
        }
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
    private void saveBlogsToCache(SharedPreferences prefs, List<BlogResponse> blogs, int todayDate) {
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

    private void showPopup(String prompt) {
        AIContentDialog dialog = AIContentDialog.newInstance(prompt, "This is placeholder content for AI response.");
        dialog.show(getParentFragmentManager(), "AIContentDialog");
    }

    // Prompts RecyclerView Adapter
    public static class PromptsAdapter extends RecyclerView.Adapter<PromptsAdapter.ViewHolder> {
        private final List<String> prompts;
        private final OnPromptClickListener listener;

        public PromptsAdapter(List<String> prompts, OnPromptClickListener listener) {
            this.prompts = prompts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_prompt, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String prompt = prompts.get(position);
            holder.promptText.setText(prompt);
            holder.itemView.setOnClickListener(v -> listener.onClick(prompt));
        }

        @Override
        public int getItemCount() {
            return prompts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView promptText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                promptText = itemView.findViewById(R.id.promptText);
            }
        }

        public interface OnPromptClickListener {
            void onClick(String prompt);
        }
    }

    // AIContentDialog Fragment
    public static class AIContentDialog extends DialogFragment {
        private static final String TITLE_KEY = "title";
        private static final String CONTENT_KEY = "content";

        public static AIContentDialog newInstance(String title, String content) {
            AIContentDialog dialog = new AIContentDialog();
            Bundle args = new Bundle();
            args.putString(TITLE_KEY, title);
            args.putString(CONTENT_KEY, content);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_ai_content, container, false);

            TextView titleView = view.findViewById(R.id.dialogTitle);
            TextView contentView = view.findViewById(R.id.dialogContent);
            View closeButton = view.findViewById(R.id.dialogCloseButton);

            assert getArguments() != null;
            titleView.setText(getArguments().getString(TITLE_KEY));
            contentView.setText(getArguments().getString(CONTENT_KEY));

            closeButton.setOnClickListener(v -> dismiss());
            return view;
        }
    }

    /**
     * Updates the styles of the toggle buttons to show which one is active.
     *
     * @param activeButton   The button to mark as active
     * @param inactiveButton The button to mark as inactive
     */
    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Apply active button styles
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Apply inactive button styles
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue));
    }

}

