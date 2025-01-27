package com.example.wave;

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
    private final OnDeleteClickListener deleteClickListener;

    // Constructor
    public TaskAdapter(List<Task> taskList, OnDeleteClickListener deleteClickListener) {
        this.taskList = taskList;
        this.deleteClickListener = deleteClickListener;
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

        // Set task title and time
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getTime());

        // Handle category tag text, background, and icon
        holder.categoryTag.setText(task.getCategory());
        switch (task.getCategory().toLowerCase()) {
            case "school":
                holder.categoryTagContainer.setBackgroundResource(R.drawable.rounded_green_background); // Green background for School
                holder.categoryIcon.setImageResource(R.drawable.ic_school); // School icon
                break;
            case "home":
                holder.categoryTagContainer.setBackgroundResource(R.drawable.rounded_yellow_background); // Yellow background for Home
                holder.categoryIcon.setImageResource(R.drawable.ic_home); // Home icon
                break;
        }

        // Handle priority flag visibility and icon
        switch (task.getPriority()) {
            case "High":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_high_priority); // Red flag for High priority
                break;
            case "Medium":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_medium_priority); // Yellow flag for Medium priority
                break;
            case "Low":
                holder.priorityFlag.setVisibility(View.VISIBLE);
                holder.priorityFlag.setImageResource(R.drawable.ic_low_priority); // Blue flag for Low priority
                break;
            default:
                holder.priorityFlag.setVisibility(View.GONE); // No flag for unspecified priority
                break;
        }

        // Handle delete icon click
        holder.deleteTask.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position); // Notify the listener
            }
        });
    }

    public void removeTask(int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.remove(position); // Remove the task from the list
            notifyItemRemoved(position); // Notify the adapter about the removal
        }
    }

    public void updateTasks(List<Task> newTasks) {
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public List<Task> getTaskList() {
        return taskList;
    }


    // ViewHolder class
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView taskCard; // Reference to CardView
        TextView taskTitle, taskTime, categoryTag;
        LinearLayout categoryTagContainer;
        ImageView deleteTask, priorityFlag, categoryIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link the views
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

    // Interface for delete click
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
