package com.example.wave;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Greeting TextView for User
        TextView greetingTextView = findViewById(R.id.greetingText);

        // Fetch the user's display name from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                greetingTextView.setText("Hello " + displayName + "!");
            } else {
                greetingTextView.setText("Hello User!"); // Default fallback
            }
        }

        // Initialize RecyclerView
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); // Set to horizontal layout

        // Initialize Task List and Adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, position -> {
            // Remove task from the list when delete icon is clicked
            if (position >= 0 && position < taskList.size()) {
                taskList.remove(position);
                taskAdapter.notifyItemRemoved(position);
                taskAdapter.notifyItemRangeChanged(position, taskList.size()); // Update the list
            }
        });

        taskRecyclerView.setAdapter(taskAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(taskRecyclerView);

        // Load Dummy Tasks for Testing
        loadDummyTasks();
        loadCurrentDate();
    }

    private void loadDummyTasks() {
        // Adding some dummy tasks
        taskList.add(new Task("Wireframes for Websites", "8:00 AM", "School", true, true));
        taskList.add(new Task("Clean Kitchen", "8:30 AM", "Home", false, false));
        taskList.add(new Task("Do Groceries", "9:00 AM", "Personal", false, true));
        taskList.add(new Task("Math Assignments", "10:00 AM", "School", false, false));

        // Notify adapter about data changes
        taskAdapter.notifyDataSetChanged();
    }
    private void loadCurrentDate() {
        TextView currentDate = findViewById(R.id.currentDate);

        // Format the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());

        // Set the formatted date to the TextView
        currentDate.setText(formattedDate);
    }

}
;