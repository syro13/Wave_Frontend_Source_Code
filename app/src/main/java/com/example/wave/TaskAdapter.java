package com.example.wave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

        // Set task title, time, and category
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getTime());
        holder.categoryTag.setText(task.getCategory());

        // Handle overdue tasks (red border and visibility)
        if (task.isOverdue()) {
            holder.taskCard.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.taskCard.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.red)); // Red border
            holder.taskCard.setStrokeWidth(4); // Set stroke width
            holder.overdueTagContainer.setVisibility(View.VISIBLE); // Show overdue tag
        } else {
            holder.taskCard.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.light_gray)); // Default border
            holder.taskCard.setStrokeWidth(0); // No stroke
            holder.overdueTagContainer.setVisibility(View.GONE); // Hide overdue tag
        }

        // Handle high-priority flag visibility
        if (task.isHighPriority()) {
            holder.priorityFlag.setVisibility(View.VISIBLE);
        } else {
            holder.priorityFlag.setVisibility(View.GONE);
        }

        // Handle delete icon click
        holder.deleteTask.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position); // Notify the listener
            }
        });
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

    // ViewHolder class
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView taskCard; // Reference to CardView
        TextView taskTitle, taskTime, categoryTag;
        LinearLayout overdueTagContainer, categoryTagContainer;
        ImageView deleteTask, priorityFlag;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link the views
            taskCard = itemView.findViewById(R.id.taskCard);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            categoryTag = itemView.findViewById(R.id.categoryTag);
            overdueTagContainer = itemView.findViewById(R.id.overdueTagContainer);
            categoryTagContainer = itemView.findViewById(R.id.categoryTagContainer);
            deleteTask = itemView.findViewById(R.id.deleteTask);
            priorityFlag = itemView.findViewById(R.id.priorityFlag);
        }
    }

    // Interface for delete click
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
