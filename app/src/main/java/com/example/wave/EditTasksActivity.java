package com.example.wave;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class EditTasksActivity extends AppCompatActivity {

    private EditText taskTitleInput, selectDate, selectTime;
    private MaterialButton schoolTaskButton, homeTaskButton;
    private MaterialButton highPriorityButton, mediumPriorityButton, lowPriorityButton;
    private SwitchMaterial remindSwitch;
    private Button editTaskButton;
    private Spinner repeatSpinner;
    private String selectedTaskType;
    private String selectedPriority;
    private Task task; // Task object being edited

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
        repeatSpinner = findViewById(R.id.repeatSpinner);

        // Task Type Buttons
        schoolTaskButton = findViewById(R.id.schoolTaskButtonInput);
        homeTaskButton = findViewById(R.id.homeTaskButtonInput);

        // Priority Buttons
        highPriorityButton = findViewById(R.id.highPriorityButton);
        mediumPriorityButton = findViewById(R.id.mediumPriorityButton);
        lowPriorityButton = findViewById(R.id.lowPriorityButton);

        // Retrieve the task passed from the previous activity
        if (getIntent().hasExtra("task")) {
            task = getIntent().getParcelableExtra("task");

            if (task != null) {
                Log.d("EditTasksActivity", "Received Task: " + task.getTitle());
                populateTaskData(task);
            } else {
                Log.e("EditTasksActivity", "Received Task is NULL");
            }
        } else {
            Log.e("EditTasksActivity", "No Task received in Intent");
        }

        // Set click listeners
        setupTaskTypeSelection();
        setupPrioritySelection();

        // Handle task edit button click
        editTaskButton.setOnClickListener(v -> saveEditedTask());
    }

    private void populateTaskData(Task task) {
        Log.d("EditTasksActivity", "Populating task data...");

        // Set text fields
        taskTitleInput.setText(task.getTitle());
        selectDate.setText(task.getFullDate());
        selectTime.setText(task.getTime());
        remindSwitch.setChecked(task.isRemind());

        // Assign task values to selected variables
        selectedTaskType = task.getCategory();
        selectedPriority = task.getPriority();

        // Set the selected category & priority
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

        schoolTaskButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        homeTaskButton.setStrokeColor(ColorStateList.valueOf(transparentColor));

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

        highPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        mediumPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));
        lowPriorityButton.setStrokeColor(ColorStateList.valueOf(transparentColor));

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

    private void saveEditedTask() {
        String updatedTitle = taskTitleInput.getText().toString();
        String updatedDate = selectDate.getText().toString();
        String updatedTime = selectTime.getText().toString();
        boolean updatedRemind = remindSwitch.isChecked();

        if (updatedDate == null || updatedDate.isEmpty()) {
            Log.e("EditTasksActivity", "updatedDate is null or empty!");
            return;
        }

        // Split the date safely
        String[] dateParts = updatedDate.split("/");
        if (dateParts.length == 3) {
            String day = dateParts[0];
            String month = dateParts[1];
            int year = Integer.parseInt(dateParts[2]);

            // Create updated task object
            Task updatedTask = new Task(
                    updatedTitle,
                    updatedTime,
                    day,
                    month,
                    selectedPriority,
                    selectedTaskType,
                    updatedRemind,
                    year
            );

            // Send updated task back to fragment
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedTask", updatedTask);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Log.e("EditTasksActivity", "Invalid date format: " + updatedDate);
        }
    }
}
