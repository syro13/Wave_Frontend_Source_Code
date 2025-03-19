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

import java.util.Arrays;
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

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_screen);

        taskTitleInput = findViewById(R.id.taskTitleInput);
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        remindSwitch = findViewById(R.id.remindSwitch);
        editTaskButton = findViewById(R.id.editTaskButton);
        repeatSpinner = findViewById(R.id.repeatSpinner);

        schoolTaskButton = findViewById(R.id.schoolTaskButtonInput);
        homeTaskButton = findViewById(R.id.homeTaskButtonInput);
        highPriorityButton = findViewById(R.id.highPriorityButton);
        mediumPriorityButton = findViewById(R.id.mediumPriorityButton);
        lowPriorityButton = findViewById(R.id.lowPriorityButton);

        task = getIntent().getParcelableExtra("task");
        if (task == null) {
            Log.e("EditTasksActivity", "Received Task is NULL");
            Toast.makeText(this, "Error: Task data missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateTaskData(task);

        selectDate.setOnClickListener(v -> showDatePicker());
        selectTime.setOnClickListener(v -> showTimePicker());

        setupTaskTypeSelection();
        setupPrioritySelection();

        editTaskButton.setOnClickListener(v -> saveEditedTask());
    }

    private void populateTaskData(Task task) {
        taskTitleInput.setText(task.getTitle());
        selectDate.setText(task.getFullDate());
        selectTime.setText(task.getTime());
        remindSwitch.setChecked(task.isRemind());
        selectedTaskType = task.getCategory();
        selectedPriority = task.getPriority();

        if ("School".equals(task.getCategory())) {
            schoolTaskButton.performClick();
        } else {
            homeTaskButton.performClick();
        }

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

        // Set Repeat Spinner
        setSpinnerSelection(task.getRepeatOption());
    }

    /**
     * Sets the spinner to the correct repeat option when editing a task.
     */
    private void setSpinnerSelection(Task.RepeatOption repeatOption) {
        if (repeatSpinner == null) return;

        String repeatText = getRepeatOptionString(repeatOption);
        for (int i = 0; i < repeatSpinner.getCount(); i++) {
            if (repeatSpinner.getItemAtPosition(i).toString().equals(repeatText)) {
                repeatSpinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Converts RepeatOption enum to user-friendly string.
     */
    private String getRepeatOptionString(Task.RepeatOption repeatOption) {
        switch (repeatOption) {
            case REPEAT_EVERY_MONDAY:
                return "Repeat every Monday";
            case REPEAT_EVERY_TUESDAY:
                return "Repeat every Tuesday";
            case REPEAT_EVERY_WEDNESDAY:
                return "Repeat every Wednesday";
            case REPEAT_EVERY_THURSDAY:
                return "Repeat every Thursday";
            case REPEAT_EVERY_FRIDAY:
                return "Repeat every Friday";
            case REPEAT_EVERY_SATURDAY:
                return "Repeat every Saturday";
            case REPEAT_EVERY_SUNDAY:
                return "Repeat every Sunday";
            default:
                return "Does not repeat";
        }
    }


    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                selectDate.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) ->
                selectTime.setText(String.format("%02d:%02d", hour, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
                .show();
    }

    private void setupTaskTypeSelection() {
        schoolTaskButton.setOnClickListener(v -> {
            selectedTaskType = "School";
            highlightSelectedCategory(schoolTaskButton, homeTaskButton);
        });
        homeTaskButton.setOnClickListener(v -> {
            selectedTaskType = "Home";
            highlightSelectedCategory(homeTaskButton, schoolTaskButton);
        });
    }

    private void setupPrioritySelection() {
        highPriorityButton.setOnClickListener(v -> {
            selectedPriority = "High";
            highlightSelectedPriority(highPriorityButton, mediumPriorityButton, lowPriorityButton);
        });
        mediumPriorityButton.setOnClickListener(v -> {
            selectedPriority = "Medium";
            highlightSelectedPriority(mediumPriorityButton, highPriorityButton, lowPriorityButton);
        });
        lowPriorityButton.setOnClickListener(v -> {
            selectedPriority = "Low";
            highlightSelectedPriority(lowPriorityButton, highPriorityButton, mediumPriorityButton);
        });
    }

    private void highlightSelectedCategory(MaterialButton selected, MaterialButton unselected) {
        selected.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        selected.setStrokeWidth(4);
        unselected.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
        unselected.setStrokeWidth(2);
    }

    private void highlightSelectedPriority(MaterialButton selected, MaterialButton... others) {
        selected.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        selected.setStrokeWidth(4);
        for (MaterialButton other : others) {
            other.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
            other.setStrokeWidth(2);
        }
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

        // Parse date values
        String[] dateParts = updatedDate.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        String previousCategory = task.getCategory(); // Get the previous category
        String newCategory = selectedTaskType; // Get the new category

        // Only remove from Firestore if the category has changed
        if (!previousCategory.equals(newCategory)) {
            deleteTaskFromOldCategory(task.getId(), previousCategory);
        }

        // Update Task Object
        Task updatedTask = new Task(
                task.getId(), updatedTitle, updatedTime, dateParts[0], getMonthYearList().get(Integer.parseInt(dateParts[1]) - 1),
                selectedPriority, newCategory, updatedRemind, Integer.parseInt(dateParts[2]), task.getStability(),
                System.currentTimeMillis(), updatedDate, false, Task.RepeatOption.DOES_NOT_REPEAT
        );

        updateTaskInFirestore(updatedTask);
    }

    /**
     * Deletes the task from the old category collection.
     */
    private void deleteTaskFromOldCategory(String taskId, String oldCategory) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;

        String collectionName = "Home".equals(oldCategory) ? "housetasks" : "schooltasks";

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection(collectionName)
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Old task deleted successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to delete old task", e));
    }

    /**
     * Updates task in Firestore without duplicating it.
     */
    private void updateTaskInFirestore(Task updatedTask) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String collectionName = "School".equals(updatedTask.getCategory()) ? "schooltasks" : "housetasks";

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection(collectionName)
                .document(updatedTask.getId())
                .set(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedTask", updatedTask);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating task", e);
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }


    private List<String> getMonthYearList() {
        return Arrays.asList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
    }
}
