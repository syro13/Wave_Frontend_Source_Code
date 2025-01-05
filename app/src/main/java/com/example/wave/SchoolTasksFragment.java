package com.example.wave;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchoolTasksFragment extends Fragment {

    private RecyclerView recyclerView, promptsRecyclerView;
    private TaskAdapter taskAdapter;
    private PromptsAdapter promptsAdapter;
    private List<Task> taskList;
    private List<String> promptsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_tasks, container, false);

        TextView schoolTasksButton = view.findViewById(R.id.SchoolTasksButton);
        TextView homeTasksButton = view.findViewById(R.id.homeTasksButton);

        // RecyclerView setup for articles
        recyclerView = view.findViewById(R.id.articlesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        taskList = new ArrayList<>();
        loadTasks(); // Load placeholder content for now

        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);

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

        // Handle School Tasks button click
        schoolTasksButton.setOnClickListener(v -> {
            setActiveButton(schoolTasksButton, homeTasksButton);
        });

        // Handle Home Tasks button click
        homeTasksButton.setOnClickListener(v -> {
            if (getActivity() instanceof SchoolHomeTasksActivity) {
                ((SchoolHomeTasksActivity) getActivity()).showHomeTasksFragment();
            }
        });
        return view;
    }

    private void loadTasks() {
        taskList.add(new Task("Study hacks", R.drawable.placeholder_image, 4));
        taskList.add(new Task("Student study on a budget", R.drawable.placeholder_image, 5));
        taskList.add(new Task("Budget friendly meal prep", R.drawable.placeholder_image, 3));
        taskList.add(new Task("How to make studying less stressful?", R.drawable.placeholder_image, 2));
    }

    private void showPopup(String prompt) {
        AIContentDialog dialog = AIContentDialog.newInstance(prompt, "This is placeholder content for AI response.");
        dialog.show(getParentFragmentManager(), "AIContentDialog");
    }


    // Task model
    public static class Task {
        String title;
        int imageRes;
        float rating;

        Task(String title, int imageRes, float rating) {
            this.title = title;
            this.imageRes = imageRes;
            this.rating = rating;
        }
    }

    // Task RecyclerView Adapter
    public static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private final List<Task> tasks;

        public TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.title.setText(task.title);
            holder.image.setImageResource(task.imageRes);
            holder.ratingBar.setRating(task.rating);

            holder.bookmarkIcon.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Bookmarked: " + task.title, Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        static class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView image, bookmarkIcon;
            RatingBar ratingBar;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.articleTitle);
                image = itemView.findViewById(R.id.articleImage);
                ratingBar = itemView.findViewById(R.id.articleRatingBar);
                bookmarkIcon = itemView.findViewById(R.id.bookmarkIcon);
            }
        }
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
        activeButton.setBackgroundResource(R.drawable.toggle_button_selected);
        activeButton.setTextColor(getResources().getColor(android.R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.toggle_button_unselected);
        inactiveButton.setTextColor(getResources().getColor(R.color.dark_blue));
    }
}

