package com.example.wave;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;

public class SchoolHomeCalendarActivity extends BaseActivity {
    private enum ActiveFragment { SCHOOL, HOME, COMBINED }
    private ActiveFragment activeFragment = ActiveFragment.COMBINED;
    private boolean isSchoolCalendarFragmentActive = true;

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
            loadFragment(new SchoolCalendarFragment());
            activeFragment = ActiveFragment.SCHOOL;
        }
    }

    public void showHomeCalendarFragment() {
        if (activeFragment != ActiveFragment.HOME) {
            loadFragment(new HomeCalendarFragment());
            activeFragment = ActiveFragment.HOME;
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
        SwitchMaterial remindSwitch = dialogView.findViewById(R.id.remindSwitch);
        View createTaskButton = dialogView.findViewById(R.id.createTaskButton);

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

        // Handle task type selection
        schoolButton.setOnClickListener(v -> {
            taskType[0] = "School";
            schoolButton.setBackgroundResource(R.drawable.rounded_green_background);
        });

        homeButton.setOnClickListener(v -> {
            taskType[0] = "Home";
            homeButton.setBackgroundResource(R.drawable.rounded_yellow_background);
        });

        // Handle task priority selection
        highPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "High";
            highPriorityButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_high_priority, 0, 0, 0);
            mediumPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            lowPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        });

        mediumPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Medium";
            mediumPriorityButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_medium_priority, 0, 0, 0);
            highPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            lowPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        });

        lowPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Low";
            lowPriorityButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_low_priority, 0, 0, 0);
            highPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mediumPriorityButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        });

        // Handle task creation
        createTaskButton.setOnClickListener(v -> {
            String taskTitle = taskTitleInput.getText().toString();
            String taskDate = selectDateInput.getText().toString();
            String taskTime = selectTimeInput.getText().toString();
            boolean remind = remindSwitch.isChecked();

            if (validateInputs(taskTitle, taskDate, taskTime, taskPriority[0], taskType[0])) {
                if (isSchoolCalendarFragmentActive) {
                    SchoolCalendarFragment fragment = (SchoolCalendarFragment) getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);
                    if (fragment != null) {
                        fragment.addTaskToCalendar(taskTitle, taskPriority[0], taskDate, taskTime, remind, taskType[0]);
                    }
                } else {
                    HomeCalendarFragment fragment = (HomeCalendarFragment) getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);
                    if (fragment != null) {
                        fragment.addTaskToCalendar(taskTitle, taskPriority[0], taskDate, taskTime, remind, taskType[0]);
                    }
                }
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private boolean validateInputs(String title, String date, String time, String priority, String type) {
        return !title.isEmpty() && !date.isEmpty() && !time.isEmpty() && !priority.isEmpty() && !type.isEmpty();
    }


    private boolean validateInputs(String title, String date, String time, String priority) {
        return !title.isEmpty() && !date.isEmpty() && !time.isEmpty() && !priority.isEmpty();
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
