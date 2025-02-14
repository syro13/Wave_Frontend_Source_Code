package com.example.wave;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

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



        // Handle task priority selection
        highPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "High";

            // Apply blue border
            highPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            highPriorityButton.setStrokeWidth(4);
            highPriorityButton.invalidate(); // Ensure UI updates immediately

            // Reset other buttons (removes blue border)
            mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
            lowPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
        });

        mediumPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Medium";

            // Apply blue border
            mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            mediumPriorityButton.setStrokeWidth(4);
            mediumPriorityButton.invalidate();

            // Reset other buttons
            highPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
            lowPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
        });

        lowPriorityButton.setOnClickListener(v -> {
            taskPriority[0] = "Low";

            // Apply blue border
            lowPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            lowPriorityButton.setStrokeWidth(4);
            lowPriorityButton.invalidate();

            // Reset other buttons
            highPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
            mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.default_stroke)));
        });

        // Handle task creation
        createTaskButton.setOnClickListener(v -> {
            String taskTitle = taskTitleInput.getText().toString();
            String taskDate = selectDateInput.getText().toString();
            String taskTime = selectTimeInput.getText().toString();
            boolean remind = remindSwitch.isChecked();

            if (validateInputs(taskTitle, taskDate, taskTime, taskPriority[0], taskType[0])) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_school_calendar_container);

                if (currentFragment instanceof SchoolCalendarFragment) {
                    ((SchoolCalendarFragment) currentFragment).addTaskToCalendar(taskTitle, taskPriority[0], taskDate, taskTime, remind, taskType[0]);
                } else if (currentFragment instanceof HomeCalendarFragment) {
                    ((HomeCalendarFragment) currentFragment).addTaskToCalendar(taskTitle, taskPriority[0], taskDate, taskTime, remind, taskType[0]);
                } else if (currentFragment instanceof CombinedCalendarFragment) {
                    ((CombinedCalendarFragment) currentFragment).addTaskToCalendar(taskTitle, taskPriority[0], taskDate, taskTime, remind, taskType[0]);
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
