package com.example.wave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final Context context;
    private final OnTaskDeletedListener onTaskDeletedListener;
    private final OnTaskEditedListener onTaskEditedListener;
    private final OnTaskCompletedListener onTaskCompletedListener; // ✅ Added completion listener
    private final ActivityResultLauncher<Intent> editTaskLauncher;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TaskAdapter(List<Task> taskList, Context context,
                       OnTaskDeletedListener onTaskDeletedListener,
                       OnTaskEditedListener onTaskEditedListener,
                       OnTaskCompletedListener onTaskCompletedListener, // ✅ Pass the completion listener
                       ActivityResultLauncher<Intent> editTaskLauncher) {
        this.taskList = taskList;
        this.context = context;
        this.onTaskDeletedListener = onTaskDeletedListener;
        this.onTaskEditedListener = onTaskEditedListener;
        this.onTaskCompletedListener = onTaskCompletedListener; // ✅ Assign it
        this.editTaskLauncher = editTaskLauncher;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item_card, parent, false);
        return new TaskViewHolder(view);
    }

    private int getMonthIndex(String month) {
        switch(month.toLowerCase()){
            case "january":   return 1;
            case "february":  return 2;
            case "march":     return 3;
            case "april":     return 4;
            case "may":       return 5;
            case "june":      return 6;
            case "july":      return 7;
            case "august":    return 8;
            case "september": return 9;
            case "october":   return 10;
            case "november":  return 11;
            case "december":  return 12;
            default:          return 0;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Reset overdue state for recycled views
        holder.overdueTagContainer.setVisibility(View.GONE);
        holder.taskCard.setStrokeColor(ContextCompat.getColor(context, android.R.color.transparent));
        holder.taskCard.setStrokeWidth(0);

        // Set basic task info
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getTime());

        // Overdue check for non-completed tasks
        // Overdue check for non-completed tasks
        if (!task.isCompleted()) {
            try {
                int day = Integer.parseInt(task.getDate());
                int month = getMonthIndex(task.getMonth());
                int year = task.getYear();

                // Switch to 24-hour parsing
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                java.time.LocalTime dueTime = java.time.LocalTime.parse(task.getTime(), timeFormatter);
                java.time.LocalDate dueDate = java.time.LocalDate.of(year, month, day);
                java.time.LocalDateTime dueDateTime = java.time.LocalDateTime.of(dueDate, dueTime);

                // Mark overdue if it's before the current time
                if (dueDateTime.isBefore(java.time.LocalDateTime.now())) {
                    holder.overdueTagContainer.setVisibility(View.VISIBLE);
                    holder.taskCard.setStrokeColor(ContextCompat.getColor(context, R.color.red));
                    holder.taskCard.setStrokeWidth(4);
                }
            } catch (Exception e) {
                Log.e("TaskAdapter", "Error parsing date/time for task: " + task.getTitle(), e);
                holder.overdueTagContainer.setVisibility(View.GONE);
            }
        } else {
            holder.overdueTagContainer.setVisibility(View.GONE);
        }


        // Category styling
        holder.categoryTag.setText(task.getCategory());
        switch (task.getCategory().toLowerCase()) {
            case "school":
                holder.categoryTagContainer.setBackgroundResource(R.drawable.rounded_green_background);
                holder.categoryIcon.setImageResource(R.drawable.ic_school);
                break;
            case "home":
                holder.categoryTagContainer.setBackgroundResource(R.drawable.rounded_yellow_background);
                holder.categoryIcon.setImageResource(R.drawable.ic_home);
                break;
        }

        // Priority icon
        switch (task.getPriority()) {
            case "High":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_high_priority);
                break;
            case "Medium":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_medium_priority);
                break;
            case "Low":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_low_priority);
                break;
            default:
                holder.priorityFlag.setVisibility(View.GONE);
                break;
        }

        // Set the click listener for editing
        holder.taskCard.setOnClickListener(v -> {
            Intent editIntent = new Intent(context, EditTasksActivity.class);
            editIntent.putExtra("task", task);
            editTaskLauncher.launch(editIntent);
        });

        // Set the task completion state
        if (task.isCompleted()) {
            holder.taskCheckCircle.setImageResource(R.drawable.ic_task_completed);
        } else {
            holder.taskCheckCircle.setImageResource(R.drawable.ic_task_uncompleted);
        }

        // Delete icon click listener
        holder.deleteTask.setOnClickListener(v -> onTaskDeletedListener.onTaskDeleted(task));

        // Toggle task completion on click of the check circle
        holder.taskCheckCircle.setOnClickListener(v -> toggleTaskCompletion(task, holder.getAdapterPosition(), holder.taskCheckCircle));
    }


    // Add this to TaskAdapter
    public void updateTasks(List<Task> newTasks) {
        Log.d("TaskAdapter", "Updating adapter with " + newTasks.size() + " tasks.");

        taskList.clear();
        for (Task task : newTasks) {
            if (!task.isCompleted()) { // ✅ Ignore completed tasks
                taskList.add(task);
            }
        }

        notifyDataSetChanged(); // Refresh UI
    }


    public void removeTask(Task task) {
        int position = -1;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(task.getId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());

            // ✅ Remove task from Firestore when it's marked as completed
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;

            if (userId != null) {
                String taskCollection = "Home".equals(task.getCategory()) ? "housetasks" : "schooltasks";
                db.collection("users")
                        .document(userId)
                        .collection(taskCollection)
                        .document(task.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d("TaskAdapter", "Task deleted from Firestore: " + task.getTitle()))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
            }
        }
    }




    private void toggleTaskCompletion(Task task, int position, ImageView taskCheckCircle) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("Firestore", "User not logged in, cannot update task completion");
            return;
        }

        boolean newStatus = !task.isCompleted(); // ✅ Toggle completion status
        String taskCollection = "Home".equals(task.getCategory()) ? "housetasks" : "schooltasks";

        db.collection("users")
                .document(userId)
                .collection(taskCollection)
                .document(task.getId())
                .update("completed", newStatus)
                .addOnSuccessListener(aVoid -> {
                    task.setCompleted(newStatus);

                    if (newStatus) {
                        removeTask(task); // ✅ Remove completed task from UI
                    } else {
                        notifyItemChanged(position); // ✅ Refresh UI if task is marked as incomplete again
                    }

                    if (onTaskCompletedListener != null) {
                        onTaskCompletedListener.onTaskCompleted(task);
                    }

                    Log.d("Firestore", "Task completion updated: " + task.getTitle());
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task completion", e));
    }




    // Interface for task completion listener
    public interface OnTaskCompletedListener {
        void onTaskCompleted(Task task);
    }

    public interface OnTaskEditedListener {
        void onTaskEdited(Task updatedTask, int position);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView taskCard;
        TextView taskTitle, taskTime, categoryTag;
        LinearLayout categoryTagContainer;
        ImageView deleteTask, priorityFlag, categoryIcon;
        ImageView taskCheckCircle;
        LinearLayout overdueTagContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCard = itemView.findViewById(R.id.taskCard);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            categoryTag = itemView.findViewById(R.id.categoryTag);
            categoryTagContainer = itemView.findViewById(R.id.categoryTagContainer);
            deleteTask = itemView.findViewById(R.id.deleteTask);
            priorityFlag = itemView.findViewById(R.id.priorityFlag);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            overdueTagContainer = itemView.findViewById(R.id.overdueTagContainer);
            taskCheckCircle = itemView.findViewById(R.id.taskCheckCircle); // ✅ Ensure this ID exists in XML
        }
    }
}
