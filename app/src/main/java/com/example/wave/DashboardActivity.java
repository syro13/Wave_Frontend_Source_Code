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
import com.example.wave.utils.UserUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setNoInternetOverlay(findViewById(R.id.noInternetOverlay));
        configureNoInternetOverlay();

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

        // Set up click listeners for other dashboard cards.
        findViewById(R.id.homeTasksCard).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SchoolHomeTasksActivity.class);
            intent.putExtra("showHomeTasks", true); // Flag to indicate home tasks should load
            startActivity(intent);
        });
        findViewById(R.id.schoolTasksCard).setOnClickListener(v ->
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
    public void onTaskEdited(Task task, int position) {
        Intent intent = new Intent(this, EditTasksActivity.class);
        intent.putExtra("task", task);
        intent.putExtra("position", position);
        editTaskLauncher.launch(intent);
    }

    @Override
    public void onTaskDeleted(Task task) {
        // Show a confirmation dialog before actually deleting
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If user confirms, call the actual delete method
                    deleteTask(task);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Deletes the given task from Firestore after archiving it in the 'cancelled' collection.
     */
    private void deleteTask(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot delete task");
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map the task's category to the Firestore collection
        // "School" or "Both" -> "schooltasks"
        // "Home" -> "housetasks"
        String category = task.getCategory();
        String taskCollection;
        String archiveCollection;

        if ("School".equals(category) || "Both".equals(category)) {
            taskCollection = "schooltasks";
            archiveCollection = "cancelledSchoolTasks";
        } else {
            // e.g. "Home"
            taskCollection = "housetasks";
            archiveCollection = "cancelledHomeTasks";
        }

        // First, archive the task by copying it to the cancelled collection
        db.collection("users")
                .document(userId)
                .collection(archiveCollection)
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    // Then delete from the active collection
                    db.collection("users")
                            .document(userId)
                            .collection(taskCollection)
                            .document(task.getId())
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("Firestore", "Task archived and deleted: " + task.getTitle());

                                // If you want to refresh the UI after deletion:
                                loadDashboardTasks();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error deleting task", e);
                                Toast.makeText(DashboardActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error archiving task", e);
                    Toast.makeText(DashboardActivity.this, "Error archiving task", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onTaskCompleted(Task task) {
        loadDashboardTasks();
    }

    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Optionally, retrieve the updated Task
                    Task updatedTask = result.getData().getParcelableExtra("updatedTask");
                    if (updatedTask != null) {
                        Log.d("DashboardActivity", "Received updatedTask: " + updatedTask.getTitle());
                    }

                    // Then refresh your list from Firestore
                    loadDashboardTasks();
                }
            });


    private void updateTaskInFirestore(Task updatedTask, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot update task");
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String collection = updatedTask.getCategory().equals("Home") ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(collection)
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated: " + updatedTask.getTitle());
                    loadDashboardTasks(); // Refresh task list
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
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
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) return;

        List<Task> dashboardTasks = new ArrayList<>();
        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    dashboardTasks.clear();
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
                                // Update adapter and refresh UI
                                taskList.clear();
                                taskList.addAll(dashboardTasks);
                                taskAdapter.notifyDataSetChanged();

                                // Handle empty UI state
                                TextView tasksDueTodayTitle = findViewById(R.id.tasksDueTodayTitle);
                                tasksDueTodayTitle.setText(dashboardTasks.isEmpty() ? "No Tasks for Today" : "Tasks for Today");

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
