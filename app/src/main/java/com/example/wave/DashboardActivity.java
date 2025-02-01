package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends BaseActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        // Greeting TextView for User
        TextView greetingTextView = findViewById(R.id.greetingText);
        CardView schoolTasksCard = findViewById(R.id.schoolTasksCard);

        schoolTasksCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, SchoolTasksFragment.class);
                startActivity(intent);
            }
        });
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

        // Load the weather icon
        loadWeatherIcon();

        findViewById(R.id.homeTasksCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SchoolHomeTasksActivity
                Intent intent = new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.schoolTasksCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SchoolHomeTasksActivity
                Intent intent = new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.wellnessTasksCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Wellness Activity
                Intent intent = new Intent(DashboardActivity.this, WellnessActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.budgetTasksCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Budget Activity
                Intent intent = new Intent(DashboardActivity.this, BudgetPlannerActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_index; // The menu item ID for the Home tab
    }

    private void loadDummyTasks() {
        taskList.add(new Task("Wireframes for Websites", "8:00 AM", "18", "January", "High", "School", true, 2025));
        taskList.add(new Task("Clean Kitchen", "9:00 AM", "19", "January", "Low", "Home", false, 2025));
        taskList.add(new Task("Do Groceries", "10:00 AM", "20", "January", "High", "Personal", true, 2025));
        taskList.add(new Task("Math Assignments", "11:00 AM", "18", "January", "Medium", "School", false, 2025));



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
    private void loadWeatherIcon() {
        ImageView weatherIcon = findViewById(R.id.weatherIcon);

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        // Make API request
        Call<WeatherResponse> call = weatherApi.getCurrentWeather("London", "42035aa61a1c72229ac148f2a197c138", "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get weather icon code
                    String iconCode = response.body().weather[0].icon;

                    // Build icon URL (OpenWeatherMap provides icons)
                    String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                    // Load icon into ImageView using Glide
                    Glide.with(DashboardActivity.this)
                            .load(iconUrl)
                            .into(weatherIcon);
                } else {
                    // Fallback icon for errors
                    weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Fallback icon for errors
                weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
            }
        });
    }


}
;