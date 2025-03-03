package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends BaseActivity implements TaskAdapter.OnTaskDeletedListener, TaskAdapter.OnTaskEditedListener, TaskAdapter.OnTaskCompletedListener {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this, this, this, this, editTaskLauncher);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        taskRecyclerView.setAdapter(taskAdapter);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(taskRecyclerView);

        // Load additional tasks
        loadCurrentDate();
        loadWeatherIcon();
        loadDashboardTasks();

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


    @Override
    public void onTaskDeleted(Task task) {
        // Handle UI updates after deletion if needed
    }

    @Override
    public void onTaskCompleted(Task task) {
        // Handle UI updates after completion if needed
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_index; // The menu item ID for the Home tab
    }

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
    // NEW: Loads daily tasks from both School and Home collections for today and shows/hides the empty state image.
    private void loadDashboardTasks() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        List<Task> dashboardTasks = new ArrayList<>();

        // Query School tasks
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Task task = doc.toObject(Task.class);
                        if (isTaskForToday(task)) {
                            dashboardTasks.add(task);
                        }
                    }
                    // Query Home tasks
                    db.collection("users")
                            .document(userId)
                            .collection("housetasks")
                            .whereEqualTo("completed", false)
                            .get()
                            .addOnSuccessListener(querySnapshot2 -> {
                                for (QueryDocumentSnapshot doc : querySnapshot2) {
                                    Task task = doc.toObject(Task.class);
                                    if (isTaskForToday(task)) {
                                        dashboardTasks.add(task);
                                    }
                                }
                                // Optionally sort dashboardTasks here if needed

                                // Update adapter with the fetched tasks
                                taskAdapter.updateTasks(dashboardTasks);
                                taskAdapter.notifyDataSetChanged();

                                // Show or hide the empty state image based on whether any tasks exist
                                ImageView emptyTasksImage = findViewById(R.id.emptyTasksImage);
                                if (emptyTasksImage != null) {
                                    emptyTasksImage.setVisibility(dashboardTasks.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching Home tasks", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching School tasks", e));
    }

    // Helper method to determine if a task is scheduled for today.
    private boolean isTaskForToday(Task task) {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        int currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
        int currentYear = today.get(Calendar.YEAR);
        try {
            int taskDay = Integer.parseInt(task.getDate());
            int taskMonth = getMonthIndex(task.getMonth()) + 1;
            int taskYear = task.getYear();
            return (taskDay == currentDay && taskMonth == currentMonth && taskYear == currentYear);
        } catch (NumberFormatException e) {
            Log.e("Dashboard", "Error parsing task date", e);
            return false;
        }
    }

    // Helper method: get the 0-based index of a month name.
    private int getMonthIndex(String month) {
        List<String> months = getMonthYearList();
        return months.indexOf(month);
    }

    // Helper method: return a list of month names.
    private List<String> getMonthYearList() {
        return List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
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
