package com.example.wave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final Context context;
    private final OnTaskDeletedListener onTaskDeletedListener;
    private final OnTaskEditedListener onTaskEditedListener; // NEW: Listener for editing tasks

    public TaskAdapter(List<Task> taskList, Context context, OnTaskDeletedListener onTaskDeletedListener, OnTaskEditedListener onTaskEditedListener) {
        this.taskList = taskList;
        this.context = context;
        this.onTaskDeletedListener = onTaskDeletedListener;
        this.onTaskEditedListener = onTaskEditedListener; // Assign the edit listener
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getTime());

        // Handle category styling
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

        // Priority icon handling
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

        // Open Edit Task Screen when clicked
        holder.taskCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTasksActivity.class);
            intent.putExtra("task", task); // FIX: Pass the full task object
            ((MainActivity) context).startActivityForResult(intent, 1); // FIX: Start activity for result
        });

        // Delete task on icon click
        holder.deleteTask.setOnClickListener(v -> showDeleteConfirmationDialog(task, position));
    }

    public void updateTasks(List<Task> newTasks) {
        taskList.clear();  // Clear the old list
        taskList.addAll(newTasks);  // Add updated tasks
        notifyDataSetChanged();  // Notify RecyclerView to refresh
    }

    private void showDeleteConfirmationDialog(Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?\n\nTask Title: " + task.getTitle());

        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteTask(task, position);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteTask(Task task, int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.remove(position);
            notifyItemRemoved(position);

            if (onTaskDeletedListener != null) {
                onTaskDeletedListener.onTaskDeleted(task);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // NEW: Method to update a task when it is edited
    public void updateTask(Task updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTitle().equals(updatedTask.getTitle())) { // Check by title (or ID if available)
                taskList.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView taskCard;
        TextView taskTitle, taskTime, categoryTag;
        LinearLayout categoryTagContainer;
        ImageView deleteTask, priorityFlag, categoryIcon;

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
        }
    }

    // NEW: Interface for handling task edits
    public interface OnTaskEditedListener {
        void onTaskEdited(Task updatedTask);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }
}
