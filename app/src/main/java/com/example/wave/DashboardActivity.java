package com.example.wave;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends BaseActivity implements
        TaskAdapter.OnTaskDeletedListener,
        TaskAdapter.OnTaskEditedListener,
        TaskAdapter.OnTaskCompletedListener {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

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

        // Retrieve the current user once.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            refreshAuthToken(user);
        }
        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                Log.e("AUTH", "User session expired. Redirecting to Login.");
                Toast.makeText(DashboardActivity.this, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DashboardActivity.this, LoginSignUpActivity.class);
                startActivity(intent);
                finish();
            }
        };
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e("SSL", "Provider installation failed", e);
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(
                        new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                                .build()
                ))
                .build();

        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((okhttp3.Call.Factory) okHttpClient));

        // Set up bottom navigation.
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        // Set up greeting and card click.
        TextView greetingTextView = findViewById(R.id.greetingText);
        CardView schoolTasksCard = findViewById(R.id.schoolTasksCard);
        schoolTasksCard.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SchoolTasksFragment.class);
            startActivity(intent);
        });
        if (user != null) {
            String displayName = user.getDisplayName();
            greetingTextView.setText(displayName != null && !displayName.isEmpty()
                    ? "Hello " + displayName + "!" : "Hello User!");
        }

        // Initialize task list, adapter, and RecyclerView.
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this, this, this, this, editTaskLauncher);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        taskRecyclerView.setAdapter(taskAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(taskRecyclerView);

        // Load dashboard UI components.
        loadCurrentDate();
        loadWeatherIcon();

        // Set up click listeners for other dashboard cards.
        findViewById(R.id.homeTasksCard).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class)));
        findViewById(R.id.wellnessTasksCard).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, WellnessActivity.class)));
        findViewById(R.id.budgetTasksCard).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, BudgetPlannerActivity.class)));
        findViewById(R.id.profileIcon).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
        loadDashboardTasks();
    }
    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_index;
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
        // Handle any UI updates needed after task deletion.
    }

    @Override
    public void onTaskCompleted(Task task) {
        // Handle any UI updates needed after task completion.
    }

    private void loadWeatherIcon() {
        ImageView weatherIcon = findViewById(R.id.weatherIcon);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        // Replace "YOUR_API_KEY_HERE" with your actual API key.
        Call<WeatherResponse> call = weatherApi.getCurrentWeather("Dublin", "42035aa61a1c72229ac148f2a197c138", "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().weather != null
                        && response.body().weather.length > 0) {
                    // Access the icon code from the weather array
                    String iconCode = response.body().weather[0].icon;
                    String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                    Glide.with(DashboardActivity.this)
                            .load(iconUrl)
                            .placeholder(R.drawable.ic_placeholder_weather)
                            .into(weatherIcon);
                } else {
                    weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("Weather", "Failed to fetch weather data", t);
                weatherIcon.setImageResource(R.drawable.ic_placeholder_weather);
            }
        });
    }

    /**
     * Refreshes the Firebase Auth Token.
     */
    private void refreshAuthToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newToken = task.getResult().getToken();
                Log.d("SESSION", "New Token: " + newToken);
            } else {
                Log.e("SESSION", "Failed to refresh token", task.getException());
            }
        });
    }

    private void loadCurrentDate() {
        TextView currentDate = findViewById(R.id.currentDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
        currentDate.setText(dateFormat.format(new Date()));
    }


    private void loadDashboardTasks() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        List<Task> dashboardTasks = new ArrayList<>();
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
                                taskAdapter.updateTasks(dashboardTasks);
                                taskAdapter.notifyDataSetChanged();
                                TextView tasksDueTodayTitle = findViewById(R.id.tasksDueTodayTitle);
                                if (dashboardTasks.isEmpty()) {
                                    tasksDueTodayTitle.setText("No Tasks for Today");
                                } else {
                                    // Optionally set the title to something else when tasks are available.
                                    tasksDueTodayTitle.setText("Tasks for Today");
                                }
                                ImageView emptyTasksImage = findViewById(R.id.emptyTasksImage);
                                if (emptyTasksImage != null) {
                                    emptyTasksImage.setVisibility(dashboardTasks.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching Home tasks", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching School tasks", e));
    }

    private boolean isTaskForToday(Task task) {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        int currentMonth = today.get(Calendar.MONTH) + 1;
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

    private int getMonthIndex(String month) {
        List<String> months = getMonthYearList();
        return months.indexOf(month);
    }

    private List<String> getMonthYearList() {
        return List.of("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
    }
}
