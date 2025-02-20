package com.example.wave;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class EditTasksActivity extends AppCompatActivity {

    private EditText taskTitleInput, selectDate, selectTime;
    private MaterialButton schoolTaskButton, homeTaskButton;
    private MaterialButton highPriorityButton, mediumPriorityButton, lowPriorityButton;
    private SwitchMaterial remindSwitch;
    private Button editTaskButton;
    private Spinner repeatSpinner;

    private String selectedTaskType = "School";
    private String selectedPriority = "Medium";

    // The Task being edited
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_screen);

        // Initialize UI elements
        taskTitleInput = findViewById(R.id.taskTitleInput);
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        remindSwitch = findViewById(R.id.remindSwitch);
        editTaskButton = findViewById(R.id.editTaskButton);
        repeatSpinner = findViewById(R.id.repeatSpinner); // if used

        // Category Buttons
        schoolTaskButton = findViewById(R.id.schoolTaskButtonInput);
        homeTaskButton = findViewById(R.id.homeTaskButtonInput);

        // Priority Buttons
        highPriorityButton = findViewById(R.id.highPriorityButton);
        mediumPriorityButton = findViewById(R.id.mediumPriorityButton);
        lowPriorityButton = findViewById(R.id.lowPriorityButton);

        // Retrieve the Task passed from the previous activity
        if (getIntent().hasExtra("task")) {
            task = getIntent().getParcelableExtra("task");
            if (task != null) {
                Log.d("EditTasksActivity", "Received Task: " + task.getTitle());
                populateTaskData(task);
            } else {
                Log.e("EditTasksActivity", "Received Task is NULL");
                finish();
            }
        } else {
            Log.e("EditTasksActivity", "No Task received in Intent");
            finish();
        }

        // Set up category & priority button clicks
        setupTaskTypeSelection();
        setupPrioritySelection();

        // Handle the "Save" (edit) button
        editTaskButton.setOnClickListener(v -> saveEditedTask());
    }

    private void populateTaskData(Task task) {
        Log.d("EditTasksActivity", "Populating task data...");
        taskTitleInput.setText(task.getTitle());
        selectDate.setText(task.getFullDate()); // e.g., "4/2/2025"
        selectTime.setText(task.getTime());
        remindSwitch.setChecked(task.isRemind());

        selectedTaskType = task.getCategory();
        selectedPriority = task.getPriority();

        highlightSelectedCategory(selectedTaskType);
        highlightSelectedPriority(selectedPriority);
    }

    private void setupTaskTypeSelection() {
        schoolTaskButton.setOnClickListener(v -> {
            selectedTaskType = "School";
            highlightSelectedCategory(selectedTaskType);
        });
        homeTaskButton.setOnClickListener(v -> {
            selectedTaskType = "Home";
            highlightSelectedCategory(selectedTaskType);
        });
    }

    private void setupPrioritySelection() {
        highPriorityButton.setOnClickListener(v -> {
            selectedPriority = "High";
            highlightSelectedPriority(selectedPriority);
        });
        mediumPriorityButton.setOnClickListener(v -> {
            selectedPriority = "Medium";
            highlightSelectedPriority(selectedPriority);
        });
        lowPriorityButton.setOnClickListener(v -> {
            selectedPriority = "Low";
            highlightSelectedPriority(selectedPriority);
        });
    }

    private void highlightSelectedCategory(String category) {
        int blueColor = getResources().getColor(R.color.blue);
        int transparentColor = getResources().getColor(R.color.transparent);

        // Reset both
        schoolTaskButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        homeTaskButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        schoolTaskButton.setStrokeWidth(2);
        homeTaskButton.setStrokeWidth(2);

        if ("School".equals(category)) {
            schoolTaskButton.setStrokeColor(ColorStateList.valueOf(blueColor));
            schoolTaskButton.setStrokeWidth(4);
        } else if ("Home".equals(category)) {
            homeTaskButton.setStrokeColor(ColorStateList.valueOf(blueColor));
            homeTaskButton.setStrokeWidth(4);
        }
    }

    private void highlightSelectedPriority(String priority) {
        int blueColor = getResources().getColor(R.color.blue);
        int transparentColor = getResources().getColor(R.color.transparent);

        // Reset all
        highPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        lowPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        highPriorityButton.setStrokeWidth(2);
        mediumPriorityButton.setStrokeWidth(2);
        lowPriorityButton.setStrokeWidth(2);

        switch (priority) {
            case "High":
                highPriorityButton.setStrokeColor(ColorStateList.valueOf(blueColor));
                highPriorityButton.setStrokeWidth(4);
                break;
            case "Medium":
                mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(blueColor));
                mediumPriorityButton.setStrokeWidth(4);
                break;
            case "Low":
                lowPriorityButton.setStrokeColor(ColorStateList.valueOf(blueColor));
                lowPriorityButton.setStrokeWidth(4);
                break;
        }
    }

    private List<String> getMonthYearList() {
        return List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
    }

    private void saveEditedTask() {
        String updatedTitle = taskTitleInput.getText().toString().trim();
        String updatedDate = selectDate.getText().toString().trim();
        String updatedTime = selectTime.getText().toString().trim();
        boolean updatedRemind = remindSwitch.isChecked();

        // Basic validation
        if (updatedTitle.isEmpty()) {
            Log.e("EditTasksActivity", "Title is empty!");
            taskTitleInput.setError("Please enter a title");
            return;
        }
        if (updatedDate.isEmpty()) {
            Log.e("EditTasksActivity", "Date is empty!");
            selectDate.setError("Please enter a valid date (DD/MM/YYYY)");
            return;
        }

        String[] dateParts = updatedDate.split("/");
        if (dateParts.length != 3) {
            Log.e("EditTasksActivity", "Invalid date format: " + updatedDate);
            selectDate.setError("Please enter a valid date (DD/MM/YYYY)");
            return;
        }

        // Extract day, month, year
        String dayStr = dateParts[0];
        String monthStr = dateParts[1];
        String yearStr = dateParts[2];

        int day, month, year;
        try {
            day = Integer.parseInt(dayStr);
            month = Integer.parseInt(monthStr);
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Log.e("EditTasksActivity", "Error parsing date: " + e.getMessage());
            selectDate.setError("Day, month, and year must be numbers");
            return;
        }

        // Make sure day and month are in normal range
        if (month < 1 || month > 12) {
            selectDate.setError("Month must be between 1 and 12");
            return;
        }
        if (day < 1 || day > 31) {
            selectDate.setError("Day must be between 1 and 31");
            return;
        }

        // Ensure we have a valid task object
        if (task == null) {
            Log.e("EditTasksActivity", "No existing task to update!");
            finish();
            return;
        }

        // Preserve the original ID
        String taskId = task.getId();

        // Build the updated task
        Task updatedTask = new Task(
                taskId, // <-- Keep the same ID
                updatedTitle,
                updatedTime,
                String.valueOf(day),
                getMonthYearList().get(month - 1),
                selectedPriority,
                selectedTaskType,
                updatedRemind,
                year
        );

        // Pass the updated task back
        int position = getIntent().getIntExtra("position", -1);
        Log.d("EditTasksActivity", "Returning updated task: " + updatedTask.getTitle()
                + ", ID: " + updatedTask.getId());

        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedTask", updatedTask);
        resultIntent.putExtra("position", position);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
