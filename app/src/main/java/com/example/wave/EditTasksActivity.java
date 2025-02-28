package com.example.wave;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
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
    private Task previousTask;

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
        repeatSpinner = findViewById(R.id.repeatSpinner); // If used

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
                populateTaskData(task); // Populate fields with existing data
            } else {
                Log.e("EditTasksActivity", "Received Task is NULL");
                finish();
            }
        } else {
            Log.e("EditTasksActivity", "No Task received in Intent");
            finish();
        }

        // Date Picker Dialog
        selectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditTasksActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        selectDate.setText(formattedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Time Picker Dialog
        selectTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    EditTasksActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        selectTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Set up category & priority button clicks
        setupTaskTypeSelection();
        setupPrioritySelection();

        // Handle the "Save" (edit) button
        editTaskButton.setOnClickListener(v -> saveEditedTask());
    }

    private void populateTaskData(Task task) {
        taskTitleInput.setText(task.getTitle());
        selectDate.setText(task.getFullDate()); // Use full date
        selectTime.setText(task.getTime());
        remindSwitch.setChecked(task.isRemind());

        // Restore category selection
        if ("School".equals(task.getCategory())) {
            schoolTaskButton.performClick();
        } else {
            homeTaskButton.performClick();
        }

        // Restore priority selection
        switch (task.getPriority()) {
            case "High":
                highPriorityButton.performClick();
                break;
            case "Medium":
                mediumPriorityButton.performClick();
                break;
            case "Low":
                lowPriorityButton.performClick();
                break;
        }

        // Set up repeat spinner (if used)
        if (repeatSpinner != null) {
            // TODO: Implement repeat logic
        }
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
        String updatedTime = selectTime.getText().toString().trim();
        String updatedDate = selectDate.getText().toString().trim();
        boolean updatedRemind = remindSwitch.isChecked();

        if (updatedTitle.isEmpty() || updatedDate.isEmpty() || updatedTime.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the date
        String[] dateParts = updatedDate.split("/");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        // Create the updated task
        Task updatedTask = new Task(
                task.getId(), // Keep the same task ID
                updatedTitle,
                updatedTime,
                String.valueOf(day),
                getMonthYearList().get(month - 1),
                selectedPriority,
                selectedTaskType,
                updatedRemind,
                year,
                task.getStability(), // Preserve existing stability
                System.currentTimeMillis(),  // Update timestamp for last modification
                updatedDate // Full date
        );

        // Get current user ID safely
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot update task");
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine correct collection based on task type
        String collectionName = "School".equals(updatedTask.getCategory()) ? "schooltasks" : "housetasks";

        // Update Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection(collectionName)
                .document(task.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated!");
                    Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating task", e);
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

}
