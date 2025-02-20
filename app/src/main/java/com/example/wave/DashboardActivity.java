package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends BaseActivity implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    // Use ActivityResultLauncher instead of deprecated startActivityForResult
    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    int position = result.getData().getIntExtra("position", -1);

                    if (updatedTask != null && position != -1 && position < taskList.size()) {
                        taskList.set(position, updatedTask);
                        taskAdapter.notifyItemChanged(position);
                    }
                }
            });

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

        schoolTasksCard.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SchoolTasksFragment.class);
            startActivity(intent);
        });

        // Fetch the user's display name from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            greetingTextView.setText(displayName != null && !displayName.isEmpty() ? "Hello " + displayName + "!" : "Hello User!");
        }

        // Initialize Task List and Adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this, this, this, editTaskLauncher); // Pass the launcher

        // Set up RecyclerView
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        taskRecyclerView.setAdapter(taskAdapter);

        // Load initial tasks
        loadInitialTasks();
        taskRecyclerView.setAdapter(taskAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(taskRecyclerView);

        // Load additional tasks
        loadDummyTasks();
        loadCurrentDate();
        loadWeatherIcon();

        findViewById(R.id.homeTasksCard).setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class)));
        findViewById(R.id.schoolTasksCard).setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class)));
        findViewById(R.id.wellnessTasksCard).setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, WellnessActivity.class)));
        findViewById(R.id.budgetTasksCard).setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, BudgetPlannerActivity.class)));
        findViewById(R.id.profileIcon).setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
    }

    @Override
    public void onTaskEdited(Task updatedTask, int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.set(position, updatedTask);
            taskAdapter.notifyItemChanged(position);
        }
    }

    private void loadInitialTasks() {
        taskList.add(new Task(UUID.randomUUID().toString(),"Math Assignment", "10:00 AM", "7", "February", "High", "School", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(),"Grocery Shopping", "12:00 PM", "8", "February", "Medium", "Home", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(),"Team Meeting", "3:00 PM", "8", "February", "High", "School", false, 2025));

        // Notify the adapter of the new tasks
        taskAdapter.updateTasks(taskList);
    }

    @Override
    public void onTaskDeleted(Task task) {
        // Handle UI updates after deletion if needed
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_index; // The menu item ID for the Home tab
    }

    private void loadDummyTasks() {
        taskList.add(new Task(UUID.randomUUID().toString(),"Wireframes for Websites", "8:00 AM", "18", "January", "High", "School", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(),"Clean Kitchen", "9:00 AM", "19", "January", "Low", "Home", false, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(),"Do Groceries", "10:00 AM", "20", "January", "High", "Personal", true, 2025));
        taskList.add(new Task(UUID.randomUUID().toString(),"Math Assignments", "11:00 AM", "18", "January", "Medium", "School", false, 2025));

        taskAdapter.notifyDataSetChanged();
    }

    private void loadCurrentDate() {
        TextView currentDate = findViewById(R.id.currentDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
        currentDate.setText(dateFormat.format(new Date()));
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
                    String iconCode = response.body().weather[0].icon;
                    String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                    Glide.with(DashboardActivity.this)
                            .load(iconUrl)
                            .into(weatherIcon);
                } else {
                    weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
            }
        });
    }
}
