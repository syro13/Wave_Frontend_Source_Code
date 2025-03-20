package com.example.wave;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class SchoolHomeCalendarActivity extends BaseActivity implements TaskCompletionListener {

    private enum ActiveFragment { SCHOOL, HOME, COMBINED }
    private ActiveFragment activeFragment = ActiveFragment.COMBINED;
    private boolean isSchoolCalendarFragmentActive = true;
    private HomeTasksFragment homeTasksFragment;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_school_calendar);

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation(bottomNavigationView);

        // Load the SchoolCalendarFragment by default
        if (savedInstanceState == null) {
            loadFragment(new CombinedCalendarFragment()); // Load Combined View
        }

        // Set up Add Task button
        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        homeTasksFragment = new HomeTasksFragment();
    }


    @Override
    public void onTaskCompletedUpdate() {
        if (homeTasksFragment != null) {
            homeTasksFragment.onTaskCompletedUpdate();
        }
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_calendar;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_school_calendar_container, fragment);
        transaction.commit();
    }

    public void showSchoolCalendarFragment() {
        if (activeFragment != ActiveFragment.SCHOOL) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            SchoolCalendarFragment schoolCalendarFragment =
                    (SchoolCalendarFragment) getSupportFragmentManager().findFragmentByTag("SCHOOL_FRAGMENT");

            if (schoolCalendarFragment == null) {
                schoolCalendarFragment = new SchoolCalendarFragment();
                transaction.addToBackStack(null);
            }

            transaction.replace(R.id.home_school_calendar_container, schoolCalendarFragment, "SCHOOL_FRAGMENT");
            transaction.commit();

            activeFragment = ActiveFragment.SCHOOL;

            refreshTasks(); // ✅ Always refresh tasks when switching
        }
    }

    public void showHomeCalendarFragment() {
        if (activeFragment != ActiveFragment.HOME) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            HomeCalendarFragment homeCalendarFragment =
                    (HomeCalendarFragment) getSupportFragmentManager().findFragmentByTag("HOME_FRAGMENT");

            if (homeCalendarFragment == null) {
                homeCalendarFragment = new HomeCalendarFragment();
                transaction.addToBackStack(null);
            }

            transaction.replace(R.id.home_school_calendar_container, homeCalendarFragment, "HOME_FRAGMENT");
            transaction.commit();

            activeFragment = ActiveFragment.HOME;

            refreshTasks(); // ✅ Always refresh tasks when switching
        }
    }



    public void loadSchoolTasksFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot fetch school tasks");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("schooltasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> schoolTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (!task.isCompleted()) {
                            schoolTasks.add(task);
                        }
                    }

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);
                    if (currentFragment instanceof SchoolCalendarFragment) {
                        ((SchoolCalendarFragment) currentFragment).updateTasks(schoolTasks);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching school tasks", e));
    }

    public void loadHomeTasksFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot fetch home tasks");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("housetasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> homeTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (!task.isCompleted()) {
                            homeTasks.add(task);
                        }
                    }

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);
                    if (currentFragment instanceof HomeCalendarFragment) {
                        ((HomeCalendarFragment) currentFragment).updateTasks(homeTasks);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching home tasks", e));
    }


    public void refreshTasks() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);

        if (activeFragment == ActiveFragment.SCHOOL && currentFragment instanceof SchoolCalendarFragment) {
            ((SchoolCalendarFragment) currentFragment).updateTasksForToday(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            loadSchoolTasksFromFirestore(); // ✅ Force fresh load
        } else if (activeFragment == ActiveFragment.HOME && currentFragment instanceof HomeCalendarFragment) {
            ((HomeCalendarFragment) currentFragment).updateTasksForToday(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            loadHomeTasksFromFirestore(); // ✅ Force fresh load
        }
    }


    public void showCombinedCalendarFragment() {
        if (activeFragment != ActiveFragment.COMBINED) {
            loadFragment(new CombinedCalendarFragment());
            activeFragment = ActiveFragment.COMBINED;
        }
    }
    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.task_input_screen, null);

        // Initialize inputs
        EditText taskTitleInput = dialogView.findViewById(R.id.taskTitleInput);
        MaterialButton schoolButton = dialogView.findViewById(R.id.schoolTaskButtonInput);
        MaterialButton homeButton = dialogView.findViewById(R.id.homeTaskButtonInput);
        MaterialButton highPriorityButton = dialogView.findViewById(R.id.highPriorityButton);
        MaterialButton mediumPriorityButton = dialogView.findViewById(R.id.mediumPriorityButton);
        MaterialButton lowPriorityButton = dialogView.findViewById(R.id.lowPriorityButton);
        EditText selectDateInput = dialogView.findViewById(R.id.selectDate);
        EditText selectTimeInput = dialogView.findViewById(R.id.selectTime);
        View createTaskButton = dialogView.findViewById(R.id.createTaskButton);

        // Initialize the Spinner from the dialog layout
        Spinner repeatSpinner = dialogView.findViewById(R.id.repeatSpinner); // Use dialogView

        // Populate the Spinner with repeat options
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                this, // Context
                R.array.repeat_options, // Reference to the string array
                android.R.layout.simple_spinner_item // Default layout for Spinner items
        );
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        // Set the default selection to the first item ("Does not repeat")
        repeatSpinner.setSelection(0);

        // Configure dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Set up date picker
        selectDateInput.setOnClickListener(v -> showDatePicker(selectDateInput));

        // Set up time picker
        selectTimeInput.setOnClickListener(v -> showTimePicker(selectTimeInput));

        // Task priority and type selection
        final String[] taskPriority = {""};
        final String[] taskType = {""};

        schoolButton.setOnClickListener(v -> {
            taskType[0] = "School";

            // Apply blue border
            schoolButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            schoolButton.setStrokeWidth(4);

            // Reset home button (removes blue border)
            homeButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        });

        homeButton.setOnClickListener(v -> {
            taskType[0] = "Home";

            // Apply blue border
            homeButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            homeButton.setStrokeWidth(4);

            // Reset school button
            schoolButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        });

        highPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "High";

            // Apply blue border and force redraw
            highPriorityButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            highPriorityButton.setStrokeWidth(4);
            highPriorityButton.refreshDrawableState(); // ✅ Forces UI to refresh

            // Reset other buttons
            resetButtonStroke(mediumPriorityButton);
            resetButtonStroke(lowPriorityButton);
        });

        mediumPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Medium";

            // Apply blue border and force redraw
            mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            mediumPriorityButton.setStrokeWidth(4);
            mediumPriorityButton.refreshDrawableState(); // ✅ Forces UI to refresh

            // Reset other buttons
            resetButtonStroke(highPriorityButton);
            resetButtonStroke(lowPriorityButton);
        });

        lowPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Low";

            // Apply blue border and force redraw
            lowPriorityButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            lowPriorityButton.setStrokeWidth(4);
            lowPriorityButton.refreshDrawableState(); // ✅ Forces UI to refresh

            // Reset other buttons
            resetButtonStroke(highPriorityButton);
            resetButtonStroke(mediumPriorityButton);
        });




        // Handle task creation
        createTaskButton.setOnClickListener(v -> {
            String taskTitle = taskTitleInput.getText().toString();
            String taskDate = selectDateInput.getText().toString();
            String taskTime = selectTimeInput.getText().toString();

            // Get the selected repeat option from the Spinner
            String repeatOptionString = repeatSpinner.getSelectedItem().toString();

            if (validateInputs(taskTitle, taskDate, taskTime, taskPriority[0], taskType[0])) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);

                if (currentFragment instanceof SchoolCalendarFragment) {
                    ((SchoolCalendarFragment) currentFragment).addTaskToCalendar(
                            taskTitle,
                            taskPriority[0],
                            taskDate,
                            taskTime,
                            taskType[0],
                            repeatOptionString // Pass the selected repeat option
                    );
                } else if (currentFragment instanceof HomeCalendarFragment) {
                    ((HomeCalendarFragment) currentFragment).addTaskToCalendar(
                            taskTitle,
                            taskPriority[0],
                            taskDate,
                            taskTime,
                            taskType[0],
                            repeatOptionString // Pass the selected repeat option
                    );
                } else if (currentFragment instanceof CombinedCalendarFragment) {
                    ((CombinedCalendarFragment) currentFragment).addTaskToCalendar(
                            taskTitle,
                            taskPriority[0],
                            taskDate,
                            taskTime,
                            taskType[0],
                            repeatOptionString // Pass the selected repeat option
                    );
                } else {
                    Toast.makeText(this, "No active calendar fragment to add the task", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void resetButtonStroke(MaterialButton button) {
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.transparent))); // Remove border
        button.setStrokeWidth(2); // Keep minimal stroke to allow dynamic change
        button.refreshDrawableState(); // ✅ Forces UI to refresh
    }


    private boolean validateInputs(String title, String date, String time, String priority, String type) {
        // Check if any field is empty
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priority.isEmpty()) {
            Toast.makeText(this, "Please select a priority", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (type.isEmpty()) {
            Toast.makeText(this, "Please select a task type (School or Home)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate date format (expected: "day/month/year")
        if (!isValidDate(date)) {
            Toast.makeText(this, "Invalid date format. Use day/month/year", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate time format (expected: "HH:mm")
        if (!isValidTime(time)) {
            Toast.makeText(this, "Invalid time format. Use HH:mm", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidDate(String date) {
        // Check if the date matches the format "day/month/year"
        String[] dateParts = date.split("/");
        if (dateParts.length != 3) {
            return false;
        }

        try {
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            // Basic validation for day, month, and year
            if (day < 1 || day > 31 || month < 1 || month > 12 || year < 2023) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isValidTime(String time) {
        // Check if the time matches the format "HH:mm"
        String[] timeParts = time.split(":");
        if (timeParts.length != 2) {
            return false;
        }

        try {
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Basic validation for hour and minute
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            dateInput.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(EditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timeInput.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }




}
