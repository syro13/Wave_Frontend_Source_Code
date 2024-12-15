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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SchoolTasks extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_tasks); // The parent layout XML

        // RecyclerView setup
        recyclerView = findViewById(R.id.articlesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data
        taskList = new ArrayList<>();
        loadTasks(); // Add sample data

        // Set up adapter
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);
    }

    private void loadTasks() {
        // Sample task data (image, title, rating)
        taskList.add(new Task("Time Management Strategies", R.drawable.placeholder_image, 4));
        taskList.add(new Task("Effective Study Techniques", R.drawable.placeholder_image, 5));
        taskList.add(new Task("The Pomodoro Technique", R.drawable.placeholder_image, 3));
        taskList.add(new Task("Avoiding Distractions", R.drawable.placeholder_image, 2));
    }

    // Task model
    public static class Task {
        String title;
        int imageRes;
        int rating;

        Task(String title, int imageRes, int rating) {
            this.title = title;
            this.imageRes = imageRes;
            this.rating = rating;
        }
    }

    public static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private final List<Task> tasks;

        // Constructor to initialize the task list
        public TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the card layout for each task
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            // Bind data to the card view
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

        // ViewHolder to hold references to the views in the card layout
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
}
