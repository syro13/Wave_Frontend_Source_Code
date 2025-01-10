package com.example.wave;

import static android.content.Context.MODE_PRIVATE;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class HomeTasksFragment extends Fragment implements NetworkReceiver.NetworkChangeListener {
    private NetworkReceiver networkReceiver;
    private static final String PREFS_BLOGS = "HomeTasksBlogs";
    private static final String KEY_LAST_FETCH = "lastFetchDateHomeTasks";
    private static final int MAX_BLOGS = 4;
    private static final String PREFS_NAME = "HomeTasksPrefs";
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
        View view = inflater.inflate(R.layout.fragment_house_tasks, container, false);

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
                "Suggest a quick cleaning tip",
                "How can I stay organized in my space?",
                "Give me advice on balancing chores with studying",
                "How to make cleaning less stressful?",
                "Share a tip for keeping my home clean and tidy"
        );
        promptsAdapter = new PromptsAdapter(promptsList, this::showPopup);
        promptsRecyclerView.setAdapter(promptsAdapter);

        // Set initial active state for buttons
        setActiveButton(homeTasksButton, schoolTasksButton);

        // Handle School Tasks button click
        schoolTasksButton.setOnClickListener(v -> {
            setActiveButton(schoolTasksButton, homeTasksButton);
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showSchoolTasksFragment();
            }
        });

        return view;
    }

    @Override
    public void onNetworkRestored() {
        Log.d("HomeTasksFragment", "Network restored. Checking data...");
        reloadDataIfNeeded();  // Reload only if data is not already cached
    }

    private void reloadDataIfNeeded() {
        if (!isAdded()) {
            Log.w("HomeTasksFragment", "Fragment not attached, skipping reloadDataIfNeeded");
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (lastFetchDate != todayDate) {
            Log.d("HomeTasksFragment", "Reloading Blogs...");
            fetchBlogsWithFallback();
        } else {
            Log.d("HomeTasksFragment", "Blogs are up-to-date.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(networkReceiver);
    }

    private void loadCachedBlogs() {
        if (!isAdded()) {
            Log.w("HomeTasksFragment", "Fragment not attached, skipping loadCachedBlogs");
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedBlogsJson = prefs.getString(PREFS_BLOGS, null);
        int lastFetchDate = prefs.getInt(KEY_LAST_FETCH, -1);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (cachedBlogsJson != null && lastFetchDate == todayDate) {
            blogs.clear();
            blogs.addAll(BlogResponse.fromJsonList(cachedBlogsJson));

            if (blogs.size() > MAX_BLOGS) {
                blogs.subList(MAX_BLOGS, blogs.size()).clear();
            }

            blogAdapter.notifyDataSetChanged();
            noBlogsImage.setVisibility(View.GONE);
            noBlogsText.setVisibility(View.GONE);

            Log.d("HomeTasksFragment", "Loaded blogs from cache.");
        } else {
            Log.d("HomeTasksFragment", "No valid cache found. Fetching from API...");
            fetchBlogsWithFallback();
        }
    }

    private void fetchBlogsWithFallback() {
        if (!isAdded()) {
            Log.w("HomeTasksFragment", "Fragment not attached, skipping fetchBlogsWithFallback");
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int todayDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        blogs.clear();
        blogAdapter.notifyDataSetChanged();
        loadingIndicator.setVisibility(View.VISIBLE);

        fetchBlogsFromApi(prefs, todayDate, success -> {
            if (!success) {
                Log.d("HomeTasksFragment", "API fetch failed. Falling back to cached blogs...");
                loadCachedBlogs();
            }
        });
    }

    private void fetchBlogsFromApi(SharedPreferences prefs, int todayDate, OnFetchCompleteListener listener) {
        BlogsApi api = RetrofitClient.getRetrofitInstance(
                requireContext(),
                "https://medium2.p.rapidapi.com/",
                "x-rapidapi-key",
                getResources().getString(R.string.school_api_key)
        ).create(BlogsApi.class);

        String[] tags = {"home", "cleaning", "decorating"};
        loadingTasksRemaining = tags.length;

        for (String tag : tags) {
            api.getRecommendedFeed(tag, 1).enqueue(new Callback<RecommendedFeedResponse>() {
                @Override
                public void onResponse(Call<RecommendedFeedResponse> call, Response<RecommendedFeedResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> recommendedFeed = response.body().getRecommendedFeed();

                        for (String articleId : recommendedFeed) {
                            if (blogs.size() >= MAX_BLOGS) break;
                            fetchArticleDetails(api, articleId, prefs, todayDate);
                        }
                        listener.onFetchComplete(true);
                    } else {
                        listener.onFetchComplete(false);
                    }
                    taskCompleted();
                }

                @Override
                public void onFailure(Call<RecommendedFeedResponse> call, Throwable t) {
                    taskCompleted();
                    listener.onFetchComplete(false);
                }
            });
        }
    }

    private void fetchArticleDetails(BlogsApi api, String articleId, SharedPreferences prefs, int todayDate) {
        if (blogs.size() >= MAX_BLOGS || !isAdded()) {
            taskCompleted();
            return;
        }

        api.getArticleInfo(articleId).enqueue(new Callback<ArticleDetails>() {
            @Override
            public void onResponse(Call<ArticleDetails> call, Response<ArticleDetails> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ArticleDetails article = response.body();

                    synchronized (blogs) {
                        if (blogs.size() < MAX_BLOGS && !isDuplicateArticle(article)) {
                            BlogResponse blog = new BlogResponse(
                                    article.getTitle(),
                                    article.getAuthor(),
                                    "home-tasks",
                                    article.getUrl(),
                                    article.getImageUrl()
                            );
                            blogs.add(blog);
                            blogAdapter.notifyDataSetChanged();
                            saveBlogsToCache(prefs, blogs, todayDate);
                        }
                    }
                }
                taskCompleted();
            }

            @Override
            public void onFailure(Call<ArticleDetails> call, Throwable t) {
                taskCompleted();
            }
        });
    }

    private boolean isDuplicateArticle(ArticleDetails article) {
        for (BlogResponse existingBlog : blogs) {
            if (existingBlog.getTitle().equals(article.getTitle()) &&
                    existingBlog.getAuthor().equals(article.getAuthor())) {
                return true;
            }
        }
        return false;
    }

    private void saveBlogsToCache(SharedPreferences prefs, List<BlogResponse> blogs, int todayDate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_BLOGS, BlogResponse.toJsonList(blogs));
        editor.putInt(KEY_LAST_FETCH, todayDate);
        editor.apply();
    }

    private void setupBlogsRecyclerView() {
        articleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        blogAdapter = new SchoolTasksBlogAdapter(requireContext(), blogs);
        articleRecyclerView.setAdapter(blogAdapter);

        // Limit the adapter size to MAX_BLOGS
        blogAdapter.notifyDataSetChanged();
    }

    private synchronized void taskCompleted() {
        loadingTasksRemaining--;
        if (loadingTasksRemaining <= 0) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showPopup(String prompt) {
        SchoolTasksFragment.AIContentDialog dialog = SchoolTasksFragment.AIContentDialog.newInstance(prompt, "This is placeholder content for AI response.");
        dialog.show(getParentFragmentManager(), "AIContentDialog");
    }

    // Prompts RecyclerView Adapter
    public static class PromptsAdapter extends RecyclerView.Adapter<HomeTasksFragment.PromptsAdapter.ViewHolder> {
        private final List<String> prompts;
        private final SchoolTasksFragment.PromptsAdapter.OnPromptClickListener listener;

        public PromptsAdapter(List<String> prompts, SchoolTasksFragment.PromptsAdapter.OnPromptClickListener listener) {
            this.prompts = prompts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public HomeTasksFragment.PromptsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_prompt, parent, false);
            return new HomeTasksFragment.PromptsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeTasksFragment.PromptsAdapter.ViewHolder holder, int position) {
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

        public static HomeTasksFragment.AIContentDialog newInstance(String title, String content) {
            HomeTasksFragment.AIContentDialog dialog = new HomeTasksFragment.AIContentDialog();
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


    public interface OnFetchCompleteListener {
        void onFetchComplete(boolean success);
    }
    private void setActiveButton(TextView activeButton, TextView inactiveButton) {
        // Apply active button styles
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        // Apply inactive button styles
        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue));
    }
}
