package com.example.wave;

public class Task {
    private String title;
    private String time;
    private String category;
    private boolean isOverdue;
    private boolean isHighPriority;

    // Constructor
    public Task(String title, String time, String category, boolean isOverdue, boolean isHighPriority) {
        this.title = title;
        this.time = time;
        this.category = category;
        this.isOverdue = isOverdue;
        this.isHighPriority = isHighPriority;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getCategory() { return category; }
    public boolean isOverdue() { return isOverdue; }
    public boolean isHighPriority() { return isHighPriority; }
}
