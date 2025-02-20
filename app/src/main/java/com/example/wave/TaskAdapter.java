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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final Context context;
    private final OnTaskDeletedListener onTaskDeletedListener;
    private final OnTaskEditedListener onTaskEditedListener;
    private final ActivityResultLauncher<Intent> editTaskLauncher;

    public TaskAdapter(List<Task> taskList, Context context,
                       OnTaskDeletedListener onTaskDeletedListener,
                       OnTaskEditedListener onTaskEditedListener,
                       ActivityResultLauncher<Intent> editTaskLauncher) {
        this.taskList = taskList;
        this.context = context;
        this.onTaskDeletedListener = onTaskDeletedListener;
        this.onTaskEditedListener = onTaskEditedListener;
        this.editTaskLauncher = editTaskLauncher;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getTime());

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

        // Edit Task on click
        holder.taskCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTasksActivity.class);
            intent.putExtra("task", task);
            intent.putExtra("position", holder.getAdapterPosition());
            if (editTaskLauncher != null) {
                editTaskLauncher.launch(intent);
            } else {
                Log.e("TaskAdapter", "editTaskLauncher is null, cannot launch edit task.");
            }
        });

        // Delete task on icon click
        holder.deleteTask.setOnClickListener(v -> showDeleteConfirmationDialog(task, position));
    }

    private void showDeleteConfirmationDialog(Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?\n\nTask Title: " + task.getTitle())
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTask(task, position);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
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

    // Updating the entire list
    public void updateTasks(List<Task> newTasks) {
        Log.d("TaskAdapter", "Updating adapter with " + newTasks.size() + " tasks.");
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    // Updating a single task by position
    public void updateTask(Task updatedTask, int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.set(position, updatedTask);
            notifyItemChanged(position);
            Log.d("TaskAdapter", "Updated task at position " + position + ": " + updatedTask.getTitle());
        } else {
            Log.e("TaskAdapter", "Invalid position for task update.");
        }
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

    public interface OnTaskEditedListener {
        void onTaskEdited(Task updatedTask, int position);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }
}
